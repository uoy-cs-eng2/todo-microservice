package todo.microservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Serdeable
@Entity
public class User {
  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String username;

  @JsonIgnore
  @ManyToMany(mappedBy = "users")
  private Set<ToDoItem> items = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Set<ToDoItem> getItems() {
    return items;
  }

  public void setItems(Set<ToDoItem> items) {
    this.items = items;
  }
}
