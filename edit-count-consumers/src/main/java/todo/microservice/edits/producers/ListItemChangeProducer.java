package todo.microservice.edits.producers;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import todo.microservice.edits.events.ChangeType;

@KafkaClient
public interface ListItemChangeProducer {

  @Topic(ListItemChangeTopicFactory.TOPIC)
  void listItemChanged(@KafkaKey long listId, ChangeType cType);

}
