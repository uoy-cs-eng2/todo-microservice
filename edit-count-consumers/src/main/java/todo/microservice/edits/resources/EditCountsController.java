package todo.microservice.edits.resources;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import todo.microservice.edits.domain.AltEditCount;
import todo.microservice.edits.repositories.AltEditCountRepository;

import java.util.Optional;

@Tag(name="counts")
@Controller("/editcounts")
public class EditCountsController {

  @Inject
  private AltEditCountRepository repo;

  @Get("/{listId}")
  public Long getCount(@PathVariable long listId) {
    Optional<AltEditCount> oEditCount = repo.findByListId(listId);
    return oEditCount.map(e -> e.getEditCount()).orElse(null);
  }

}
