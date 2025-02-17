package todo.microservice.edits.domain;

import jakarta.persistence.*;

@Entity
public class EditCount {

  @Id
  @GeneratedValue
  private long id;

  @Column
  private long listId;

  @Column
  private long editCount;

  public EditCount() {
    // no-arg constructor for database access
  }

  public EditCount(long listId) {
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
