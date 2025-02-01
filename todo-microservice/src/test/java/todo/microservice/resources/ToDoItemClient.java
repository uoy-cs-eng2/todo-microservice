package todo.microservice.resources;

import io.micronaut.data.model.Page;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.User;
import todo.microservice.dto.ListItemUpdateDTO;

import java.util.List;

@Client(ToDoItemController.PREFIX)
public interface ToDoItemClient {

  @Get("/{?page}")
  Page<ToDoItem> list(@QueryValue int page);

  @Get("/{id}")
  ToDoItem get(long id);

  @Put(value = "/{id}", consumes = MediaType.TEXT_PLAIN)
  HttpResponse<String> update(long id, @Body ListItemUpdateDTO update);

  @Delete(value = "/{id}", consumes = MediaType.TEXT_PLAIN)
  HttpResponse<String> delete(long id);

  @Get("/{id}/users{?page}")
  List<User> getUsers(@PathVariable long id, @QueryValue int page);

  @Put(value = "/{id}/users/{userId}", consumes = MediaType.TEXT_PLAIN)
  HttpResponse<String> assignUser(@PathVariable long id, @PathVariable long userId);

  @Delete(value = "/{id}/users/{userId}", consumes = MediaType.TEXT_PLAIN)
  HttpResponse<String> unassignUser(@PathVariable long id, @PathVariable long userId);

}
