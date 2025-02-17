package todo.microservice.edits.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class EditCountByPartition {
  @Id
  @GeneratedValue
  private long id;

  @Column
  private long listId;

  @Column
  private int partitionId;

  @Column
  private long editCount;

  public EditCountByPartition() {
    // no-arg constructor for reflective use
  }

  public EditCountByPartition(long listId, int partition) {
    this.listId = listId;
    this.partitionId = partition;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getListId() {
    return listId;
  }

  public void setListId(long listId) {
    this.listId = listId;
  }

  public int getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(int partition) {
    this.partitionId = partition;
  }

  public long getEditCount() {
    return editCount;
  }

  public void setEditCount(long count) {
    this.editCount = count;
  }
}
