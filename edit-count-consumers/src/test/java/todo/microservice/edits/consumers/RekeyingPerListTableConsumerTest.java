package todo.microservice.edits.consumers;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todo.microservice.edits.domain.AltEditCount;
import todo.microservice.edits.events.ChangeType;
import todo.microservice.edits.events.ItemChangeEvent;
import todo.microservice.edits.events.ToDoItem;
import todo.microservice.edits.events.ToDoList;
import todo.microservice.edits.producers.ListItemChangeProducer;
import todo.microservice.edits.repositories.AltEditCountRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest(transactional = false)
public class RekeyingPerListTableConsumerTest {

  @Inject
  private RekeyingPerListTableConsumer consumer;

  @Inject
  private ListItemChangeProducer producer;

  @Inject
  private AltEditCountRepository repo;

  private static final long LIST_ID = 234L;

  @BeforeEach
  public void setup() {
    repo.deleteAll();
  }

  @MockBean(ListItemChangeProducer.class)
  public ListItemChangeProducer getProducer() {
    return mock(ListItemChangeProducer.class);
  }

  @Test
  public void itemCreationIsRekeyed() {
    consumer.rekeyItemChanges(itemCreated(1));
    verify(producer).listItemChanged(eq(LIST_ID), eq(ChangeType.CREATED));
  }

  @Test
  public void rekeyedEventUpdatesCount() {
    assertFalse(repo.existsById(LIST_ID));
    consumer.listItemChanged(LIST_ID);
    AltEditCount editCount = repo.findByListId(LIST_ID).get();
    assertEquals(1L, editCount.getEditCount());
  }

  private static ItemChangeEvent itemCreated(int i) {
    return new ItemChangeEvent(ChangeType.CREATED, createItem(i));
  }

  private static ToDoItem createItem(long itemId) {
    ToDoItem item = new ToDoItem();
    item.setId(itemId);
    item.setList(new ToDoList());
    item.getList().setId(LIST_ID);
    return item;
  }
}
