package todo.microservice.edits.consumers;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todo.microservice.edits.domain.AltEditCount;
import todo.microservice.edits.events.ChangeType;
import todo.microservice.edits.events.ItemChangeEvent;
import todo.microservice.edits.events.ToDoItem;
import todo.microservice.edits.events.ToDoList;
import todo.microservice.edits.producers.ListItemChangeTopicFactory;
import todo.microservice.edits.repositories.AltEditCountRepository;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;

@MicronautTest(transactional = false)
public class RekeyingPerListTableConsumerTest {

  @Inject
  private RekeyingPerListTableConsumer consumer;

  @Inject
  private AltEditCountRepository repo;

  @Inject
  private AdminClient adminClient;

  private static final long LIST_ID = 234L;

  @BeforeEach
  public void setup() throws Exception {
    // Find out if the topics we need are in the cluster, and how many partitions they have
    DescribeTopicsResult describeResult = adminClient
        .describeTopics(Arrays.asList("items", ListItemChangeTopicFactory.TOPIC));

    /*
     * We build up a request to delete all records from all partitions of the above topics
     * (if they exist in the cluster).
     */
    Map<TopicPartition, RecordsToDelete> request = new HashMap<>();
    for (Map.Entry<String, KafkaFuture<TopicDescription>> entry : describeResult.topicNameValues().entrySet()) {
      TopicDescription topicDescription = entry.getValue().get();
      if (topicDescription != null) {
        for (TopicPartitionInfo part : topicDescription.partitions()) {
          TopicPartition tp = new TopicPartition(entry.getKey(), part.partition());
          request.put(tp, RecordsToDelete.beforeOffset(Long.MAX_VALUE));
        }
      }
    }
    adminClient.deleteRecords(request);

    // Clean the table as well
    repo.deleteAll();
  }

  @Test
  public void listCountIsUpdated() {
    final int nChanges = 20;
    for (int i = 0; i < nChanges; i++) {
      consumer.rekeyItemChanges(new ItemChangeEvent(ChangeType.CREATED, createItem(i)));
    }

    // The correct edit count should be computed in a reasonable amount of time
    await().atMost(Duration.ofSeconds(5)).until(editCountIsEqualTo(LIST_ID, nChanges));
  }

  private Callable<Boolean> editCountIsEqualTo(long listId, int nChanges) {
    return () -> {
        Optional<AltEditCount> oEditCount = repo.findByListId(listId);
        if (oEditCount.isPresent()) {
          return oEditCount.get().getEditCount() == nChanges;
        }
        return false;
    };
  }

  private static ToDoItem createItem(long itemId) {
    ToDoItem item = new ToDoItem();
    item.setId(itemId);
    item.setList(new ToDoList());
    item.getList().setId(LIST_ID);
    return item;
  }
}
