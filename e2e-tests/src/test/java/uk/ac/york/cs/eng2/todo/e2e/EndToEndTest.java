package uk.ac.york.cs.eng2.todo.e2e;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.york.eng2.edits.api.CountsApi;
import uk.ac.york.eng2.todo.api.ItemsApi;
import uk.ac.york.eng2.todo.api.ListsApi;
import uk.ac.york.eng2.todo.model.ListItemCreateDTO;
import uk.ac.york.eng2.todo.model.ToDoList;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class EndToEndTest {

  @Inject
  private ListsApi listsApi;

  @Inject
  private CountsApi countsApi;

  @Test
  public void createListIncrementsEditCount() {
    // Create a list
    HttpResponse<@NotNull String> listCreateResponse = listsApi.create("shopping");
    assertEquals(HttpStatus.CREATED, listCreateResponse.getStatus());
    long listID = Long.parseLong(listCreateResponse.header(HttpHeaders.LOCATION).split("/")[2]);

    // Add an item
    listsApi.addItem1(listID, new ListItemCreateDTO("hello", "world", ZonedDateTime.now()));

    // Eventually, the edit count is incremented for the list
    await().atMost(Duration.ofSeconds(10)).until(editCountBecomes(listID, 1));
  }

  protected Callable<Boolean> editCountBecomes(long listId, int expectedCount) {
    return () -> {
      @NonNull Optional<@NotNull Long> oCount = countsApi.getCount(listId).getBody();
      if (oCount.isPresent()) {
        return oCount.get() == expectedCount;
      }
      return false;
    };
  }
}
