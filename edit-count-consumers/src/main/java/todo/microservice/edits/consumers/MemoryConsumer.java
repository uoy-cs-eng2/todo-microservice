package todo.microservice.edits.consumers;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import todo.microservice.edits.events.ItemChangeEvent;

import java.util.HashMap;
import java.util.Map;

@KafkaListener(groupId="in-memory", offsetReset=OffsetReset.EARLIEST)
public class MemoryConsumer {
  private Map<Long, Integer> countsByListId = new HashMap<>();

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
        Thread.currentThread().getName(),
        listId, count, System.lineSeparator());
  }

}
