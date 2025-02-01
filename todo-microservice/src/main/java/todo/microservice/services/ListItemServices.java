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

import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import todo.microservice.domain.ToDoItem;
import todo.microservice.domain.ToDoList;
import todo.microservice.dto.ListItemUpdateDTO;
import todo.microservice.gateways.CurrencyGateway;
import todo.microservice.repositories.ToDoItemRepository;
import todo.microservice.repositories.ToDoListRepository;

@Singleton
public class ListItemServices {

	@Inject
	private ToDoListRepository listRepo;

	@Inject
	private ToDoItemRepository repo;

	@Inject
	private CurrencyGateway currencyGateway;

	private static final Pattern PATTERN_CURRENCY = Pattern.compile(
		"Exchange (?<source>\\w+) to (?<target>\\w+) @ (?<date>([0-9]+{4}-[0-9]{2}-[0-9]{2}|latest))",
		Pattern.CASE_INSENSITIVE);

	public ToDoItem create(ToDoList list, ToDoItem item) {
		// Ignore the ID for addition
		item.setId(null);
		item.setList(list);

		/*
		 * If the title contains a substring matching this pattern,
		 * call the external API to add a note on exchange rates
		 */
		Matcher mCurrency = PATTERN_CURRENCY.matcher(item.getTitle());
		if (mCurrency.find()) {
			String source = mCurrency.group("source");
			String target = mCurrency.group("target");
			String date = mCurrency.group("date");

			Optional<Double> exchange = currencyGateway.exchange(date, source, target);
			String extraText;
			if (exchange.isEmpty()) {
				extraText = String.format("%nCould not obtain exchange rate for %s -> %s @ %s", source, target, date);
			} else {
				extraText = String.format("%nExchange rate for %s -> %s @ %s is: %.4f", source, target, date, exchange.get());
			}

			item.setBody(item.getBody() + extraText);
		}

		return repo.save(item);
	}

	public <T> T update(ToDoItem item, ListItemUpdateDTO update, Supplier<T> listNotFound, Supplier<T> allOk) {
		if (update.listId() != null) {
			Optional<ToDoList> optList = listRepo.findById(update.listId());
			if (optList.isEmpty()) {
				return listNotFound.get();
			}
			item.setList(optList.get());
		}
		if (update.title() != null) {
			item.setTitle(update.title());
		}
		if (update.body() != null) {
			item.setBody(update.body());
		}
		if (update.timestamp() != null) {
			item.setTimestamp(update.timestamp());
		}
		repo.save(item);

		return allOk.get();
	}

}
