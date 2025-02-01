package todo.microservice.gateways;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class CurrencyGatewayTest {
  @Inject
  CurrencyGateway gateway;

  @Test
  public void validCurrencyUppercase() {
    assertTrue(gateway.isValidCurrency("EUR"));
  }

  @Test
  public void validCurrencyLowercase() {
    assertTrue(gateway.isValidCurrency("eur"));
  }

  @Test
  public void invalidCurrency() {
    assertFalse(gateway.isValidCurrency("___"));
  }

  @Test
  public void exchangePoundsToEuros() {
    Optional<Double> oRate = gateway.exchange("latest", "gbp", "eur");
    assertTrue(oRate.isPresent());
  }
}
