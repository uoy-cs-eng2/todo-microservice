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
package todo.microservice.repositories;

import java.util.Optional;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;
import todo.microservice.domain.ToDoItem;
import todo.microservice.dto.ListItemDTO;

@Repository
public interface ToDoItemRepository extends PageableRepository<ToDoItem, Long> {
	/*
	 * Join annotation is needed in order to resolve the proxy of the ToDoItem
	 * in one single query (instead of using repeated queries).
	 */
	@Join(value="list", type=Join.Type.FETCH)
	@Override
	Optional<ToDoItem> findById(@NonNull Long id);

	@Join(value="list", type=Join.Type.FETCH)
	@Override
	Page<ToDoItem> findAll(@NonNull Pageable pageable);

	Page<ListItemDTO> findByListId(long id, Pageable pageable);

	boolean existsByListId(long id);

	Page<ToDoItem> findByUsersId(long id, Pageable pageable);

}
