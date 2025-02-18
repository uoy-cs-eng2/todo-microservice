package todo.microservice.edits.consumers;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.KafkaPartition;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import todo.microservice.edits.domain.EditCountByPartition;
import todo.microservice.edits.events.ItemChangeEvent;
import todo.microservice.edits.repositories.EditCountByPartitionRepository;

/**
 * This uses separate rows for each partition. At each point we have at most one
 * consumer per partition, so this should avoid any concurrency problems.
 */
@SuppressWarnings("unused")
@KafkaListener(groupId="per-partition-editcount-table", threads = 3, offsetReset = OffsetReset.EARLIEST)
public class PerPartitionTableConsumer {
  @Inject
  private EditCountByPartitionRepository repository;

  @Transactional
  @Topic("items")
  public void itemChange(@MessageBody ItemChangeEvent ev, @KafkaPartition Integer partition) {
    final long listId = ev.item().getList().getId();

    var editCount = repository.findByListIdAndPartitionId(listId, partition).orElse(new EditCountByPartition(listId, partition));
    editCount.setEditCount(editCount.getEditCount() + 1);
    repository.save(editCount);

    System.out.printf("%s - %s - Edit count for partition %d of list %d: %d - sum is %d%s",
        getClass().getSimpleName(),
        Thread.currentThread().getName(),
        partition, listId,
        editCount.getEditCount(),
        repository.getSumEditCountByListId(listId),
        System.lineSeparator());
  }

}