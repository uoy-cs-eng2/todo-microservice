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

import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.core.annotation.Creator;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Gateway object for the Currency Exchange Rates API by fawazahmed0 on <a
 * href="https://github.com/fawazahmed0/exchange-api">GitHub</a>.
 */
@Singleton
public class DefaultCurrencyGateway implements CurrencyGateway {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCurrencyGateway.class);

  // We only ask for the available set of currencies at the start
  private final Optional<Set<String>> availableCurrencies;
  private final CurrencyClient client;

  @Creator
  public DefaultCurrencyGateway(CurrencyClient client) {
    this.client = client;

    final Map<String, String> clientCurrencies = client.availableCurrencies();
    if (clientCurrencies != null) {
      availableCurrencies = Optional.of(new HashSet<>(clientCurrencies.keySet()));
    } else {
      availableCurrencies = Optional.empty();
      LOGGER.warn("Currency service is currently down: will not be able to validate currency names");
    }
  }

  @Override
  public boolean isValidCurrency(String currency) {
    return availableCurrencies.isEmpty() || availableCurrencies.get().contains(currency.toLowerCase());
  }

  @Cacheable("exchange-rates")
  @Override
  public Optional<Double> exchange(String date, String source, String target) {
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

    Map<String, Object> rawData = client.exchange(date, source);
    if (rawData != null) {
      Object targetData = rawData.get(source);
      if (targetData instanceof Map rates) {
        return Optional.of(((Number) rates.get(target)).doubleValue());
      }
    }
    LOGGER.warn("Could not find exchange value for {} on {}", source, date);
    return Optional.empty();
  }

}
