package todo.microservice.gateways;

import java.util.Optional;

public interface CurrencyGateway {
  boolean isValidCurrency(String currency);
  Optional<Double> exchange(String date, String source, String target);
}
