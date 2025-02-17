package todo.microservice.edits.repositories;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import todo.microservice.edits.domain.EditCountByPartition;

import java.util.Optional;

@Repository
public interface EditCountByPartitionRepository extends CrudRepository<EditCountByPartition, Long> {

  Optional<EditCountByPartition> findByListIdAndPartitionId(long listId, int partitionId);

  long getSumEditCountByListId(long listId);

}
