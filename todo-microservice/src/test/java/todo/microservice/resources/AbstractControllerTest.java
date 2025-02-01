package todo.microservice.resources;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.repositories.ToDoListRepository;
import todo.microservice.repositories.UsersRepository;

public class AbstractControllerTest {
  @Inject
  protected ToDoItemRepository itemRepository;

  @Inject
  protected UsersRepository usersRepository;

  @Inject
  protected ToDoListRepository listRepository;

  @BeforeEach
  public void setup() {
    itemRepository.deleteAll();
    usersRepository.deleteAll();
    listRepository.deleteAll();
  }
}
