package todo.microservice.resources;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import todo.microservice.ToDoConfiguration;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.User;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.repositories.UsersRepository;

import java.util.Optional;

@Tag(name = "users")
@Controller(UsersController.PREFIX)
public class UsersController {
  public static final String PREFIX = "/users";

  @Inject
  private ToDoItemRepository itemRepository;

  @Inject
  private UsersRepository usersRepository;

  @Inject
  private ToDoConfiguration config;

  @Get("/{?page}")
  public Page<User> list(@QueryValue(defaultValue = "0") @Min(0) int page) {
    return usersRepository.findAll(Pageable.from(page, config.getPageSize()));
  }

  @Get("/{id}")
  public User findById(Long id) {
    return usersRepository.findById(id).orElse(null);
  }

  @Post(consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
  public HttpResponse<String> create(@Body String username) {
    User user = new User();
    user.setUsername(username);
    user = usersRepository.save(user);

    return HttpResponse.created(String.format("Created user with ID %d", user.getId()))
        .header(HttpHeaders.LOCATION, String.format("%s/%d", PREFIX, user.getId()));
  }

  @Transactional
  @Delete(value = "/{id}", produces = MediaType.TEXT_PLAIN)
  public HttpResponse<String> delete(@PathVariable Long id) {
    @NonNull Optional<User> oUser = usersRepository.findById(id);
    if (oUser.isPresent()) {
      User user = oUser.get();

      // Remove the user from any items they may still be in
      for  (ToDoItem item : user.getItems()) {
        item.getUsers().remove(user);
        itemRepository.save(item);
      }

      usersRepository.delete(user);
      return HttpResponse.ok(String.format("Deleted oUser with ID %d", id));
    } else {
      throw new HttpStatusException(HttpStatus.NOT_FOUND,
          String.format("User with ID %d not found", id));
    }
  }

  @Get("/{id}/items{?page}")
  public Page<ToDoItem> listItems(@PathVariable Long id, @QueryValue(defaultValue = "0") @Min(0) int page) {
    @NonNull Optional<User> oUser = usersRepository.findById(id);
    if (oUser.isEmpty()) {
      throw new HttpStatusException(HttpStatus.NOT_FOUND, String.format("User with ID %d not found", id));
    }
    return itemRepository.findByUsersId(id, Pageable.from(page, config.getPageSize()));
  }

}
