package todo.microservice.edits.consumers;

import io.micronaut.configuration.kafka.ConsumerSeekAware;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.configuration.kafka.seek.KafkaSeekOperation;
import io.micronaut.configuration.kafka.seek.KafkaSeeker;
import org.apache.kafka.common.TopicPartition;
import todo.microservice.edits.events.ItemChangeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@KafkaListener(groupId="in-memory-restarting")
public class RestartingMemoryConsumer implements ConsumerSeekAware {
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
