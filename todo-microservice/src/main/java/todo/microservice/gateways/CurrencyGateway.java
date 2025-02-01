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

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.micronaut.core.annotation.Creator;
import jakarta.inject.Singleton;

/**
 * Gateway object for the Currency API by fawazahmed0 on <a href="https://github.com/fawazahmed0/currency-api">GitHub</a>.
 */
@Singleton
public class CurrencyGateway {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyGateway.class);

	private static final class CurrencyCacheLoader extends CacheLoader<CurrencyPair, Double> {
		private CurrencyClient client;

		public CurrencyCacheLoader(CurrencyClient client) {
			this.client = client;
		}

		@Override
		public Double load(CurrencyPair key) {
			Map<String, Object> rawData = client.exchange(key.date, key.source);
			if (rawData != null) {
				Object targetData = rawData.get(key.source);
				if (targetData instanceof Map rates) {
					return ((Number) rates.get(key.target)).doubleValue();
				}
			}
			throw new NoSuchElementException("Could not find exchange value for " + key);
		}
	}

	private record CurrencyPair(String date, String source, String target) {}

	// We only ask for the available set of currencies at the start
	private final Optional<Set<String>> availableCurrencies;

	// We use a Guava cache to reduce the number of calls on the API 
	private final LoadingCache<CurrencyPair, Double> currencyData; 

	@Creator
	public CurrencyGateway(CurrencyClient client) {
		final Map<String, String> clientCurrencies = client.availableCurrencies();
		if (clientCurrencies != null) {
			availableCurrencies = Optional.of(new HashSet<>(clientCurrencies.keySet()));
		} else {
			availableCurrencies = Optional.empty();
			LOGGER.warn("Currency service is currently down: will not be able to validate currency names");
		}

		currencyData = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterWrite(Duration.ofHours(8))
			.build(new CurrencyCacheLoader(client));
	}

	public boolean isValidCurrency(String currency) {
		return availableCurrencies.isEmpty() || availableCurrencies.get().contains(currency);
	}

	public Optional<Double> exchange(String date, String source, String target) {
		try {
			// API always uses lowercase names for currencies
			source = source.toLowerCase();
			target = target.toLowerCase();

			if (!isValidCurrency(source)) {
				LOGGER.warn("Invalid source currency {}", source);
				return Optional.empty();
			} else if (!isValidCurrency(target)) {
				LOGGER.warn("Invalid target currency {}", target);
				return Optional.empty();
			}

			return Optional.of(currencyData.get(new CurrencyPair(date, source, target)));
		} catch (ExecutionException e) {
			LOGGER.error(e.getMessage(), e);
			return Optional.empty();
		}
	}

}
