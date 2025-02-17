package todo.microservice.edits.repositories;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import todo.microservice.edits.domain.EditCount;

import java.util.Optional;

@Repository
public interface EditCountRepository extends CrudRepository<EditCount, Long> {

  Optional<EditCount> findByListId(long id);
}
