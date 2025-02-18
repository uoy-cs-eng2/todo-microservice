package todo.microservice.edits.repositories;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import todo.microservice.edits.domain.AltEditCount;

import java.util.Optional;

@Repository
public interface AltEditCountRepository extends CrudRepository<AltEditCount, Long> {

  Optional<AltEditCount> findByListId(long id);
}
