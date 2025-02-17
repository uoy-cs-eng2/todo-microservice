/*
 * Copyright 2023 University of York
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package todo.microservice.resources;

import java.util.Optional;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import todo.microservice.ToDoConfiguration;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;
import todo.microservice.dto.ListItemCreateDTO;
import todo.microservice.dto.ListItemDTO;
import todo.microservice.events.ToDoProducer;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.repositories.ToDoListRepository;
import todo.microservice.services.ListItemServices;

/**
 * RESTful controller for to-do lists.
 */
@Tag(name="lists")
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller(ToDoListController.PREFIX)
public class ToDoListController {
	public static final String PREFIX = "/lists";

	@Inject
	private ToDoListRepository repo;

	@Inject
	private ToDoItemRepository itemRepo;

	@Inject
	private ToDoConfiguration config;

	@Inject
	private ToDoProducer kafkaClient;

	@Inject
	private ListItemServices itemServices;

	// Note that Micronaut uses its own Nullable annotations: https://youtrack.jetbrains.com/issue/IDEA-263946
	@Get("/{?page}")
	public Page<ToDoList> list(@QueryValue(defaultValue="0") int page) {
		return repo.findAll(Pageable.from(page, config.getPageSize()));
	}

	@Get("/byName/{name}")
	public ToDoList find(String name) {
		Optional<ToDoList> result = repo.findByName(name);
		return result.isEmpty() ? null : result.get();
	}

	@Get("/{id}")
	public ToDoList find(long id) {
		Optional<ToDoList> list = repo.findById(id);

		// NOTE: we will trigger a 404 Not Found response when we return null
		return list.isEmpty() ? null : list.get();
	}

	@Get("/{id}/items{?page}")
	public Page<ListItemDTO> items(long id, @QueryValue(defaultValue = "0") int page) {
		return itemRepo.findByListId(id, Pageable.from(page, config.getPageSize()));
	}

	@Transactional
	@Get("/byName/{name}/items{?page}")
	public Page<ListItemDTO> items(String name, @QueryValue(defaultValue = "0") int page) {
		Optional<ToDoList> list = repo.findByName(name);
		if (list.isEmpty()) {
			return null;
		}

		return itemRepo.findByListId(list.get().getId(), Pageable.from(page, config.getPageSize()));
	}

	@Transactional
	@Delete(value = "/{id}", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> delete(long id) {
		if (itemRepo.existsByListId(id)) {
			return HttpResponse.status(HttpStatus.FORBIDDEN,
				String.format("List with ID %d cannot be deleted as it still has items", id));
		}
		repo.deleteById(id);

		ToDoList fakeList = new ToDoList();
		fakeList.setId(id);
		kafkaClient.listDeleted(fakeList);

		return HttpResponse.ok(String.format("List with ID %d deleted successfully", id));
	}

	@Transactional
	@Delete(value = "/byName/{name}", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> delete(String name) {
		Optional<ToDoList> optList = repo.findByName(name);
		if (optList.isEmpty()) {
			return null;
		}

		final ToDoList list = optList.get();
		final Long listId = list.getId();
		if (itemRepo.existsByListId(listId)) {
			return HttpResponse.status(HttpStatus.FORBIDDEN,
				String.format("List with name %s cannot be deleted as it still has items", name));
		}

		repo.deleteByName(name);
		kafkaClient.listDeleted(list);
		return HttpResponse.ok(String.format("List with name %s deleted successfully", name));
	}

	/* 
	 * The @Transactional annotation is needed here so that the transaction covers
	 * both fetch and update, otherwise JPA will complain about trying to persist
	 * a detached entity as we'd end up doing multiple separate transactions (one
	 * for each call to a repository method).
	 */
	@Transactional
	@Put(value = "/{id}", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> update(long id, @Body String name) {
		if (repo.existsByName(name)) {
			return HttpResponse.status(HttpStatus.FORBIDDEN, 
				String.format("A list with name '%s' already exists", name));
		}

		Optional<ToDoList> list = repo.findById(id);
		if (list.isEmpty()) {
			return HttpResponse.notFound("Could not find list with ID " + id);
		} else {
			ToDoList foundList = list.get();
			foundList.setName(name);
			repo.save(foundList);
			kafkaClient.listUpdated(foundList);
			return HttpResponse.ok(String.format("Renamed list with ID %d to '%s'", id, name));
		}
	}

	@Transactional
	@Put(value = "/byName/{oldName}", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> update(String oldName, @Body String newName) {
		Optional<ToDoList> list = repo.findByName(oldName);
		if (list.isEmpty()) {
			return HttpResponse.notFound(String.format("Could not find list with name '%s'", oldName));
		} else {
			ToDoList foundList = list.get();
			foundList.setName(newName);
			repo.save(foundList);
			kafkaClient.listUpdated(foundList);
			return HttpResponse.ok(String.format("Renamed list with name '%s' to '%s'", oldName, newName));
		}
	}

	@Post(value = "/", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> create(@Body String name) {
		if (repo.existsByName(name)) {
			return HttpResponse.status(HttpStatus.FORBIDDEN, 
				String.format("A list with name '%s' already exists", name));
		}

		ToDoList list = new ToDoList();
		list.setName(name);
		repo.save(list);

		kafkaClient.listCreated(list);
		return HttpResponse.created("List created")
			.header(HttpHeaders.LOCATION, generateURL(list)); 
	}

	@Transactional
	@Post(value = "/{id}/items", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> addItem(long id, @Body ListItemCreateDTO item) {
		Optional<ToDoList> list = repo.findById(id);
		if (list.isEmpty()) {
			return HttpResponse.notFound("Could not find list with ID " + id);
		}
		return addItem(item.createItem(), list.get());
	}

	@Transactional
	@Post(value = "/byName/{name}/items", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> addItem(String name, @Body ListItemCreateDTO item) {
		Optional<ToDoList> list = repo.findByName(name);
		if (list.isEmpty()) {
			return HttpResponse.notFound("Could not find list with name " + name);
		}
		return addItem(item.createItem(), list.get());
	}

	private HttpResponse<String> addItem(ToDoItem item, ToDoList list) {
		item = itemServices.create(list, item);
		kafkaClient.itemCreated(item);

		final String itemURL = ToDoItemController.generateURL(item);
		return HttpResponse.created(String.format(
			"Added item to list %d (%s): available at %s", list.getId(), list.getName(), itemURL)
		).header(HttpHeaders.LOCATION, itemURL);
	}

	public static String generateURL(ToDoList list) {
		return String.format("%s/%d", PREFIX, list.getId());
	}
}