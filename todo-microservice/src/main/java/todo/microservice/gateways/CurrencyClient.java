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
package todo.microservice.gateways;

import java.util.Map;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.CircuitBreaker;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@CircuitBreaker
@Validated
@Client("${currency.url:`https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api`}")
public interface CurrencyClient {

	String DATE_PATTERN = "[0-9]{4}-[0-2]{2}-[0-2]{2}|latest";

	/**
	 * Retrieves all available currencies from this API.
	 */
	@Get("@latest/v1/currencies")
	Map<String, String> availableCurrencies();

	/**
	 * Retrieves all exchange values from a certain currency at a certain date.
	 *
	 * @param date Date in YYYY-MM-DD format, or "latest" for the latest value.
	 * @param currency Source currency.
	 */
	@Get("@{date}/v1/currencies/{currency}.json")
	Map<String, Object> exchange(@NotBlank @Pattern(regexp=DATE_PATTERN) String date, @NotBlank String currency);

}
