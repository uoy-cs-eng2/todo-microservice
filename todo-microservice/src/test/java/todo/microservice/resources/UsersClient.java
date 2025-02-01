package todo.microservice.resources;

import io.micronaut.data.model.Page;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.User;

@Client(UsersController.PREFIX)
public interface UsersClient {
  @Get("/{?page}")
  Page<User> list(@QueryValue int page);

  @Get("/{id}")
  User findById(Long id);

  @Post(consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
  HttpResponse<String> create(@Body String username);

  @Delete(value = "/{id}", consumes = MediaType.TEXT_PLAIN)
  HttpResponse<String> delete(@PathVariable Long id);

  @Get("/{id}/items{?page}")
  Page<ToDoItem> listItems(@PathVariable Long id, @QueryValue int page);
}
