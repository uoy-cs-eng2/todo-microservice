package todo.microservice.edits.consumers;

import io.micronaut.configuration.kafka.annotation.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import todo.microservice.edits.domain.EditCount;
import todo.microservice.edits.events.ItemChangeEvent;
import todo.microservice.edits.repositories.EditCountRepository;

/**
 * This is an example of using a table with per-list counts, where there is still the
 * risk that we end up losing updates if we have multiple concurrent transactions trying
 * to increment the same row (hence the {@code threads = 3} value).
 *
 * In fact, they might even fail during the initial load if several consumers try to
 * create the first row for a list ID at the same time: hence the exponential retry
 * that is needed to get them to eventually record the edit.
 */
@KafkaListener(
    groupId="per-list-editcount-table",
    threads = 3,
    offsetReset = OffsetReset.EARLIEST,
    errorStrategy = @ErrorStrategy(
        value = ErrorStrategyValue.RETRY_EXPONENTIALLY_ON_ERROR,
        retryCount = 3,
        retryDelay = "100ms"
    )
)
public class PerListTableConsumer {
  @Inject
  private EditCountRepository repository;

  @Transactional
  @Topic("items")
  public void itemChange(ItemChangeEvent ev) {
    final long listId = ev.item().getList().getId();

    EditCount editCount = repository.findByListId(listId).orElse(new EditCount(listId));
    editCount.setEditCount(editCount.getEditCount() + 1);
    repository.save(editCount);

    System.out.printf("%s - %s - Edit count for list %d: %d%s",
        getClass().getSimpleName(),
        Thread.currentThread().getName(), listId, editCount.getEditCount(), System.lineSeparator());
  }

}