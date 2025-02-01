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
package todo.microservice.dto;

import io.micronaut.serde.annotation.Serdeable;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;

import java.time.LocalDateTime;

/**
 * Represents a request to create a {@link ToDoItem} within a {@link ToDoList}.
 */
@Serdeable
public class ListItemCreateDTO {

	private String title, body;
	private LocalDateTime timestamp;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Creates the item from this request, without saving it to the database.
	 * Saving to the database is left up to the caller.
	 */
	public ToDoItem createItem() {
		ToDoItem item = new ToDoItem();
		item.setTitle(title);
		item.setBody(body);
		item.setTimestamp(timestamp);
		return item;
	}
}
