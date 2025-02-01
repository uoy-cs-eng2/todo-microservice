package todo.microservice.resources;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;
import todo.microservice.domain.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
public class UsersControllerTest extends AbstractControllerTest {

  @Inject
  private UsersClient usersClient;

  @Inject
  private ToDoItemClient itemClient;

  private ToDoItem item;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();

    ToDoList list = new ToDoList();
    list.setName("test");
    list = listRepository.save(list);

    item = new ToDoItem();
    item.setTitle("test title");
    item.setBody("body");
    item.setList(list);
    item = itemRepository.save(item);
  }

  @Test
  public void noUsers() {
    assertTrue(usersClient.list(0).isEmpty());
  }

  @Test
  public void createAndListUser() {
    String username = "antonio";
    usersClient.create(username);

    List<User> content = usersClient.list(0).getContent();
    assertEquals(1, content.size());
    assertEquals(username, content.get(0).getUsername());
  }

  @Test
  public void noItemsForNewUser() {
    User user = createUser();
    assertTrue(usersClient.listItems(user.getId(), 0).isEmpty());
  }

  @Test
  public void withItems() {
    User user = createUser();
    itemClient.assignUser(item.getId(), user.getId());

    List<ToDoItem> content = usersClient.listItems(user.getId(), 0).getContent();
    assertEquals(1, content.size());
    assertEquals(item.getId(), content.get(0).getId());

    /* If we delete the user, their assignments will be removed but the item will still be there. */
    usersClient.delete(user.getId());
    assertTrue(itemClient.getUsers(item.getId(), 0).isEmpty());
  }

  protected User createUser() {
    User user = new User();
    user.setUsername("testuser");
    user = usersRepository.save(user);
    return user;
  }

}
