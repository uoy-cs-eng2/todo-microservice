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
package todo.microservice.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Entity
public class ToDoItem {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private ToDoList list;

	@Column
	private LocalDateTime timestamp;

	@Column
	private String title;

	@Column
	private String body;

	@JsonIgnore
	@ManyToMany
	private Set<User> users = new HashSet<>();

	public ToDoItem() {
		// nothing to do
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ToDoList getList() {
		return list;
	}

	public void setList(ToDoList list) {
		this.list = list;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

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

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		return "ToDoItem [id=" + id + ", list=" + list + ", timestamp=" + timestamp + ", title=" + title + ", body=" + body + "]";
	}

}
