package todo.microservice.edits.consumers;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import todo.microservice.edits.domain.AltEditCount;
import todo.microservice.edits.events.ItemChangeEvent;
import todo.microservice.edits.producers.ListItemChangeProducer;
import todo.microservice.edits.producers.ListItemChangeTopicFactory;
import todo.microservice.edits.repositories.AltEditCountRepository;

@SuppressWarnings("unused")
@KafkaListener(groupId = "rekey-per-list-table", threads = 3, offsetReset = OffsetReset.EARLIEST)
public class RekeyingPerListTableConsumer {

  @Inject
  private ListItemChangeProducer listItemChangeProducer;

  @Inject
  private AltEditCountRepository repository;

  @Topic("items")
  public void rekeyItemChanges(ItemChangeEvent event) {
    // Re-keys the event by list ID instead of item ID
    listItemChangeProducer.listItemChanged(event.item().getList().getId(), event.type());
    System.out.printf("%s - %s - Rekeyed change in item %d to change in list %d%s",
        getClass().getSimpleName(),
        Thread.currentThread().getName(),
        event.item().getId(),
        event.item().getList().getId(),
        System.lineSeparator());
  }

  @Transactional
  @Topic(ListItemChangeTopicFactory.TOPIC)
  public void listItemChanged(@KafkaKey long listId) {
    AltEditCount editCount = repository.findByListId(listId).orElse(new AltEditCount(listId));
    editCount.setEditCount(editCount.getEditCount() + 1);
    repository.save(editCount);

    System.out.printf("%s - %s - Edit count for list %d: %d%s",
        getClass().getSimpleName(),
        Thread.currentThread().getName(),
        listId,
        editCount.getEditCount(),
        System.lineSeparator());
  }

}
