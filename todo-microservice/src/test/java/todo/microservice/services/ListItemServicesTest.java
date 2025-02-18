/*
 * Copyright 2023 University of York
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package todo.microservice.services;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;
import todo.microservice.dto.ListItemUpdateDTO;
import todo.microservice.events.ToDoProducer;
import todo.microservice.gateways.CurrencyGateway;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.repositories.ToDoListRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MicronautTest
public class ListItemServicesTest {

  @Inject
  ToDoListRepository listRepository;

  @Inject
  ToDoItemRepository itemRepository;

  @Inject
  ListItemServices listItemServices;

  @Inject
  ToDoProducer producer;

  @BeforeEach
  public void setup() {
    itemRepository.deleteAll();
    listRepository.deleteAll();
  }

  @Test
  public void createWithExchange() {
    ToDoList l = new ToDoList();
    l.setName("test");
    l = listRepository.save(l);

    ToDoItem item = new ToDoItem();
    item.setList(l);
    item.setTitle("exchange usd to eur @ 2025-02-01");
    item.setBody("dummy");

    item = listItemServices.create(l, item);
    assertNotNull(item.getId());
    assertTrue(item.getBody().toLowerCase().contains("exchange rate"));
  }

  @Test
  public void createProducesEvents() {
    ToDoList l = new ToDoList();
    l.setName("test");
    l = listRepository.save(l);

    ToDoItem item = new ToDoItem();
    item.setList(l);
    item.setTitle("New item");
    item.setBody("dummy");

    item = listItemServices.create(l, item);
    verify(producer).itemCreated(eq(item));
  }

  @Test
  public void updateItemTitle() {
    ToDoList l = new ToDoList();
    l.setName("test");
    l = listRepository.save(l);

    ToDoItem item = new ToDoItem();
    item.setList(l);
    item.setTitle("New item");
    item.setBody("dummy");
    listItemServices.create(l, item);

    ListItemUpdateDTO dto = new ListItemUpdateDTO(
        item.getList().getId(), "New title", null, null);
    listItemServices.update(item, dto, () -> null, () -> null);

    // Item was updated
    item = itemRepository.findById(item.getId()).get();
    assertEquals("New title", item.getTitle());

    // Item change event was produced
    verify(producer).itemUpdated(eq(item));
  }

  @MockBean(ToDoProducer.class)
  protected ToDoProducer getProducerMock() {
    return mock(ToDoProducer.class);
  }

  /**
   * Replace the real service for a stub for this test, as we're testing the item service and not
   * the real service.
   */
  @MockBean(CurrencyGateway.class)
  protected CurrencyGateway getCurrencyGatewayStub() {
    CurrencyGateway stub = mock(CurrencyGateway.class);
    when(stub.isValidCurrency(any())).thenReturn(true);
    when(stub.exchange(any(), any(), any())).thenReturn(Optional.of(1.0));
    return stub;
  }

}
