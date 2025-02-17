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
package todo.microservice.events;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;

@KafkaClient
public interface ToDoProducer {

	@Topic(ToDoTopicFactory.TOPIC_LISTS)
	void listChanged(@KafkaKey long id, ListChangeEvent list);

	@Topic(ToDoTopicFactory.TOPIC_ITEMS)
	void itemChanged(@KafkaKey long id, ItemChangeEvent item);

	default void listDeleted(ToDoList list) {
		listChanged(list.getId(), new ListChangeEvent(ChangeType.DELETED, list));
	}

	default void listUpdated(ToDoList newList) {
		listChanged(newList.getId(), new ListChangeEvent(ChangeType.UPDATED, newList));
	}

	default void listCreated(ToDoList list) {
		listChanged(list.getId(), new ListChangeEvent(ChangeType.CREATED, list));
	}

	default void itemDeleted(ToDoItem item) {
		itemChanged(item.getId(), new ItemChangeEvent(ChangeType.DELETED, item));
	}

	default void itemUpdated(ToDoItem item) {
		itemChanged(item.getId(), new ItemChangeEvent(ChangeType.UPDATED, item));
	}

	default void itemCreated(ToDoItem item) {
		itemChanged(item.getId(), new ItemChangeEvent(ChangeType.CREATED, item));
	}

}
