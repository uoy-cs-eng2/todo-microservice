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

import java.time.LocalDateTime;

import io.micronaut.serde.annotation.Serdeable;
import todo.microservice.domain.ToDoItem;

/**
 * Represents a request to update a specific {@link ToDoItem}.
 */
@Serdeable
public record ListItemUpdateDTO(Long listId, String title, String body, LocalDateTime timestamp) {

}
