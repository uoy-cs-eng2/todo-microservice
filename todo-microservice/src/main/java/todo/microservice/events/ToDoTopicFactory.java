package todo.microservice.events;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;

@Requires(bean = AdminClient.class)
@Factory
public class ToDoTopicFactory {

  public static final String
      TOPIC_LISTS = "lists",
      TOPIC_ITEMS = "items";

  @Bean
  CreateTopicsOptions options() {
    return new CreateTopicsOptions().timeoutMs(5000);
  }

  @Bean
  NewTopic listsTopic() {
    return new NewTopic(TOPIC_LISTS, 3, (short) 1);
  }

  @Bean
  NewTopic itemsTopic() {
    return new NewTopic(TOPIC_ITEMS, 3, (short) 1);
  }

}
