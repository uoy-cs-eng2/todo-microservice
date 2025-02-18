package todo.microservice.edits.producers;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

@Requires(bean= AdminClient.class)
@Factory
public class ListItemChangeTopicFactory {

  public static final String TOPIC = "list-item-change";

  @Bean
  public NewTopic createListItemChangeTopic() {
    return new NewTopic(TOPIC, 3, (short) 1);
  }

}
