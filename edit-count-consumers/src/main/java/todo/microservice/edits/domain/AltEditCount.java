package todo.microservice.edits.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class AltEditCount {

  @Id
  @GeneratedValue
  private long id;

  @Column
  private long listId;

  @Column
  private long editCount;

  public AltEditCount() {
    // no-arg constructor for database access
  }

  public AltEditCount(long listId) {
    this.listId = listId;
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

  public long getEditCount() {
    return editCount;
  }

  public void setEditCount(long editCount) {
    this.editCount = editCount;
  }
}
