package todo.microservice.edits.consumers;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todo.microservice.edits.events.ChangeType;
import todo.microservice.edits.events.ItemChangeEvent;
import todo.microservice.edits.events.ToDoItem;
import todo.microservice.edits.events.ToDoList;
import todo.microservice.edits.repositories.EditCountByPartitionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactional = false)
public class PerPartitionTableConsumerTest {
  @Inject
  private EditCountByPartitionRepository repo;

  @Inject
  private PerPartitionTableConsumer consumer;

  private static final long LIST_ID = 456L;

  @BeforeEach
  public void setup() {
    repo.deleteAll();
  }

  @Test
  public void sumIsComputed() {
    consumer.itemChange(itemCreated(1), 0);
    consumer.itemChange(itemCreated(2), 1);
    consumer.itemChange(itemCreated(3), 1);
    consumer.itemChange(itemCreated(4), 2);
    consumer.itemChange(itemCreated(5), 2);
    consumer.itemChange(itemCreated(6), 2);

    assertEquals(1, repo.findByListIdAndPartitionId(LIST_ID, 0).get().getEditCount());
    assertEquals(2, repo.findByListIdAndPartitionId(LIST_ID, 1).get().getEditCount());
    assertEquals(3, repo.findByListIdAndPartitionId(LIST_ID, 2).get().getEditCount());
    assertEquals(6, repo.getSumEditCountByListId(LIST_ID));
  }

  private static ItemChangeEvent itemCreated(int itemId) {
    return new ItemChangeEvent(ChangeType.CREATED, createItem(itemId));
  }

  private static ToDoItem createItem(long itemId) {
    ToDoItem item = new ToDoItem();
    item.setId(itemId);
    item.setList(new ToDoList());
    item.getList().setId(LIST_ID);
    return item;
  }
}
