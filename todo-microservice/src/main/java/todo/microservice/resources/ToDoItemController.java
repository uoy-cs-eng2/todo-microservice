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
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import todo.microservice.ToDoConfiguration;
import todo.microservice.domain.ToDoItem;
import todo.microservice.events.ToDoProducer;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.services.ListItemServices;
import todo.microservice.services.ListItemUpdateRequest;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller(ToDoItemController.PREFIX)
public class ToDoItemController {
	public static final String PREFIX = "/items";

	@Inject
	private ToDoItemRepository repo;

	@Inject
	private ListItemServices itemServices;

	@Inject
	private ToDoConfiguration config;

	@Inject
	private ToDoProducer kafkaClient;

	@Get("/{?page}")
	Page<ToDoItem> list(@QueryValue(defaultValue="0") int page) {
		return repo.findAll(Pageable.from(page, config.getPageSize()));
	}

	@Get("/{id}")
	ToDoItem get(long id) {
		return repo.findById(id).orElse(null);
	}

	@Transactional
	@Put("/{id}")
	HttpResponse<String> update(long id, @Body ListItemUpdateRequest update) {
		Optional<ToDoItem> optItem = repo.findById(id);
		if (optItem.isEmpty()) {
			return null;
		}

		final ToDoItem item = optItem.get();
		return itemServices.update(
			item, update,
			() -> HttpResponse.badRequest(String.format(
					"Could not update item %d: list %d does not exist", id, update.getListId()
				)),
			() -> {
				kafkaClient.itemUpdated(id, item);
				return HttpResponse.ok("Updated item with ID " + id);
			}
		);
	}

	@Transactional
	@Delete("/{id}")
	HttpResponse<String> delete(long id) {
		Optional<ToDoItem> optItem = repo.findById(id);
		if (optItem.isEmpty()) {
			return null;
		}

		final ToDoItem item = optItem.get();
		repo.delete(item);
		kafkaClient.itemDeleted(id, item);
		return HttpResponse.ok(String.format("Item with ID %d was deleted successfully", id));
	}

	public static String generateURL(ToDoItem item) {
		return String.format("%s/%d", PREFIX, item.getId());
	}
}
