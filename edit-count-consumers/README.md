# Edit Count Consumers

This is an example Micronaut application which subscribes to the list item edit change events from the
`todo-microservice`, and tries to keep a count of how many times the items in each to-do list were changed.

It shows many different consumers that attempt to do this:

* `MemoryConsumer` is a naive example where we just keep an in-memory map.
  It doesn't survive restarts, and it wouldn't be able to handle the scenario where we have multiple concurrent consumers.
* `RestartingMemoryConsumer` tries to rebuild the counter by seeking to the beginning of each partition when assigned partitions.
  This would become slower and slower the more history we have, and it still doesn't handle the scenario where we have multiple concurrent consumers.
  This would happen, for instance, if we ran multiple copies of this application.
* `MultiThreadRestartingMemoryConsumer` illustrates what would happen if we ran multiple concurrent copies of `RestartingMemoryConsumer`.
  Each copy will only see its own count, and we would have to somehow synchronize or add up these counters ourselves.
  Trying to reintroduce synchronisation (e.g. by switching to a single map across all copies instead of separate thread-local maps) would mean we lose the speedup we'd get from multiple copies, though.
* `PerListTableConsumer` is the first example that tries to use a database table, keyed by list ID, directly from the per-item change events.
  This one has issues when multiple copies are run concurrently, as they may interfere with each other's changes to the table.
  In fact, it needs a retry-with-backoff error strategy to deal with things like multiple concurrent transactions trying to insert the row for the first change in a given list: one or more of them would fail, and would require retrying.
* `PerPartitionTableConsumer` uses a different table which is keyed by list and partition ID.
  This one handles restarting and multiple copies well (since each row is handled at most by one consumer at a time), but we need to use a custom repository query to sum across partitions.
* `RekeyingPerListTableConsumer` is an alternative solution, which uses both the Kafka cluster and a database table.
  Per-item changes are re-keyed by list ID, and we use these re-keyed events to update a table equivalent to `PerListTableConsumer`.
  This works fine as all the events around a given key will always be part of the same partition, so only one consumer will attempt to change its records at a time.

To try out this example, follow these steps:

1. Make sure we start from a clean slate by running the `stopTestResourcesService` Gradle task.
   This will stop any running Micronaut Test Resources server that is shared between the applications in this repository,
   and therefore discard any database or Kafka cluster we may have from previous runs.
2. Start `todo-microservice` as normal.
3. Create a to-do list, and add a good number of items to it (say, 30).
4. Start `edit-count-consumers`.
5. Notice how the various consumers compute their edit counts, and how their edit counts compare to each other.
   At this point, the correct count would be equal to the items we added (so 30 if we followed the above indication).
6. Stop `edit-count-consumers`.
7. Add a few more items (say, 5).
8. Start `edit-count-consumers`.
9. Notice how the various consumers deal (or not) with the restart, and how their edit counters compare to each other.
   The correct answer would be to report 30 + 5 = 35 edits in total.
10. Stop `todo-microservice` and `edit-count-consumers`.
