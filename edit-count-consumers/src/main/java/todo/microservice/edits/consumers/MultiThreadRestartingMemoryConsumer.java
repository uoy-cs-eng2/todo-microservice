package todo.microservice.edits.consumers;

import io.micronaut.configuration.kafka.ConsumerSeekAware;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.configuration.kafka.seek.KafkaSeekOperation;
import io.micronaut.configuration.kafka.seek.KafkaSeeker;
import io.micronaut.runtime.context.scope.ThreadLocal;
import org.apache.kafka.common.TopicPartition;
import todo.microservice.edits.events.ItemChangeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
 * We use @ThreadLocal to simulate the scenario where we have three different consumers,
 * each with their own internal state (i.e. their own HashMap) covering only part of the
 * events.
 *
 * If we didn't have this @ThreadLocal annotation, every thread would share the same
 * MultiThreadRestartingMemoryConsumer instance and therefore every update would go
 * into the same HashMap. With the appropriate synchronisation we would get correct,
 * totals, but we would still have the performance issue of having to seek to offset
 * 0 every time (and the synchronisation would nullify any speedup we'd have from
 * multithreading).
 */
@ThreadLocal
@KafkaListener(groupId="in-memory-multithread", threads = 3)
public class MultiThreadRestartingMemoryConsumer implements ConsumerSeekAware {
  private final Map<Long, Integer> countsByListId = new HashMap<>();

  @Topic("items")
  public void itemChange(ItemChangeEvent ev) {
    final long listId = ev.item().getList().getId();
    Integer count = countsByListId.get(listId);
    if (count == null) {
      count = 0;
    }
    count++;
    countsByListId.put(listId, count);

    System.out.printf("%s - %s - Edit count for list %d: %d%s",
        getClass().getSimpleName(),
        Thread.currentThread().getName(), listId, count, System.lineSeparator());
  }

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> collection) {
    // nothing to do
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> collection, KafkaSeeker seeker) {
    countsByListId.clear();
    for (TopicPartition partition : collection) {
      // always replay from the beginning
      seeker.perform(KafkaSeekOperation.seek(partition, 0));
    }
  }
}
