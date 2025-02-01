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

import java.util.List;
import java.util.Optional;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import todo.microservice.ToDoConfiguration;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.User;
import todo.microservice.events.ToDoProducer;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.repositories.UsersRepository;
import todo.microservice.services.ListItemServices;
import todo.microservice.dto.ListItemUpdateDTO;

@Tag(name="items")
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller(ToDoItemController.PREFIX)
public class ToDoItemController {
	public static final String PREFIX = "/items";

	@Inject
	private ToDoItemRepository repo;

	@Inject
	private UsersRepository usersRepository;

	@Inject
	private ListItemServices itemServices;

	@Inject
	private ToDoConfiguration config;

	@Inject
	private ToDoProducer kafkaClient;

	@Get("/{?page}")
	public Page<ToDoItem> list(@QueryValue(defaultValue="0") int page) {
		return repo.findAll(Pageable.from(page, config.getPageSize()));
	}

	@Get("/{id}")
	public ToDoItem get(long id) {
		return repo.findById(id).orElse(null);
	}

	@Transactional
	@Put(value = "/{id}", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> update(long id, @Body ListItemUpdateDTO update) {
		Optional<ToDoItem> optItem = repo.findById(id);
		if (optItem.isEmpty()) {
			return null;
		}

		final ToDoItem item = optItem.get();
		return itemServices.update(
			item, update,
			() -> HttpResponse.badRequest(String.format(
					"Could not update item %d: list %d does not exist", id, update.listId()
				)),
			() -> {
				kafkaClient.itemUpdated(id, item);
				return HttpResponse.ok("Updated item with ID " + id);
			}
		);
	}

	@Transactional
	@Delete(value = "/{id}", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> delete(long id) {
		Optional<ToDoItem> optItem = repo.findById(id);
		if (optItem.isEmpty()) {
			return null;
		}

		final ToDoItem item = optItem.get();
		repo.delete(item);
		kafkaClient.itemDeleted(id, item);
		return HttpResponse.ok(String.format("Item with ID %d was deleted successfully", id));
	}

	@Transactional
	@Get("/{id}/users{?page}")
	public List<User> getUsers(@PathVariable long id, @QueryValue(defaultValue = "0") @Min(0) int page) {
		if (!repo.existsById(id)) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, String.format("Item %d does not exist", id));
		}

		/*
		 * Note: we could have obtained the item and then tried to return item.getUsers(),
		 * but that would fail as it's a lazy collection and Micronaut would try to serialize
		 * it to JSON outside of this method. One solution would have been to fetch all of it
		 * in one go by creating a copy, but it's more efficient to just query the user
		 * repo for the information we want directly.
		 */
		return usersRepository.findByItemsId(id, Pageable.from(page, config.getPageSize()));
	}

	@Transactional
	@Put(value = "/{id}/users/{userId}", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> assignUser(@PathVariable long id, @PathVariable long userId) {
		ToDoItem item = findItem(id);
		User user = findUser(userId);
		if (item.getUsers().add(user)) {
			repo.save(item);
			return HttpResponse.created(String.format("User %d has been added to task %d", userId, id));
		} else {
			return HttpResponse.ok(String.format("User %d was already part of task %d", userId, id));
		}
	}

	@Transactional
	@Delete(value = "/{id}/users/{userId}", produces = MediaType.TEXT_PLAIN)
	public HttpResponse<String> unassignUser(@PathVariable long id, @PathVariable long userId) {
		ToDoItem item = findItem(id);
		User user = findUser(userId);
		if (item.getUsers().remove(user)) {
			repo.save(item);
			return HttpResponse.created(String.format("User %d has been removed from task %d", userId, id));
		} else {
			return HttpResponse.ok(String.format("User %d was not part of task %d", userId, id));
		}
	}

	protected User findUser(long userId) {
		@NonNull Optional<User> oUser = usersRepository.findById(userId);
		if (oUser.isEmpty()) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, String.format("User with ID %d does not exist", userId));
		}
    return oUser.get();
	}

	protected ToDoItem findItem(long id) {
		Optional<ToDoItem> item = repo.findById(id);
		if (item.isEmpty()) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, String.format("Item %d not found", id));
		}
		return item.get();
	}

	protected static String generateURL(ToDoItem item) {
		return String.format("%s/%d", PREFIX, item.getId());
	}
}
