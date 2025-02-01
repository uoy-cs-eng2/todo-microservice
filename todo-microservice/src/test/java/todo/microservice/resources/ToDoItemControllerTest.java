package todo.microservice.resources;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;
import todo.microservice.domain.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class ToDoItemControllerTest extends AbstractControllerTest {

  @Inject
  private ToDoItemClient client;

  protected ToDoList list;
  protected User user;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();

    list = new ToDoList();
    list.setName("test");
    list = listRepository.save(list);

    user = new User();
    user.setUsername("testUser");
    user = usersRepository.save(user);
  }

  @Test
  public void noUsersInItem() {
    ToDoItem item = createItem();
    assertEquals(0, client.getUsers(item.getId(), 0).size());
  }

  @Test
  public void assignUser() {
    ToDoItem item = createItem();
    client.assignUser(item.getId(), user.getId());

    List<User> users = client.getUsers(item.getId(), 0);
    assertEquals(1, users.size());
    assertEquals(user.getId(), users.get(0).getId());
  }

  @Test
  public void unassignUser() {
    ToDoItem item = createItem(user);

    assertEquals(1, client.getUsers(item.getId(), 0).size());
    client.unassignUser(item.getId(), user.getId());
    assertEquals(0, client.getUsers(item.getId(), 0).size());
  }

  @Test
  public void deleteWhileAssigned() {
    ToDoItem item = createItem(user);
    client.delete(item.getId());

    assertFalse(itemRepository.existsById(item.getId()));
    assertTrue(usersRepository.existsById(user.getId()));
  }

  private ToDoItem createItem(User... users) {
    ToDoItem item = new ToDoItem();
    item.setBody("example");
    item.setTitle("my title");
    item.setList(list);
    item.getUsers().addAll(Arrays.asList(users));
    item.setTimestamp(LocalDateTime.now());
    return itemRepository.save(item);
  }

}
