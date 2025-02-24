package todo.microservice.gateways;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest
public class CurrencyGatewayCacheTest {

  @Inject
  private CurrencyClient client;

  @Inject
  private CurrencyGateway gateway;

  @Test
  public void requestsAreCached() {
    gateway.exchange("latest", "gbp", "eur");
    gateway.exchange("latest", "gbp", "eur");

    // We called exchange twice, but the client should only be invoked the first time
    verify(client, times(1)).exchange("latest", "gbp");
  }

  @MockBean(CurrencyClient.class)
  public CurrencyClient getClient() {
    CurrencyClient mockClient = mock(CurrencyClient.class);
    when(mockClient.availableCurrencies())
        .thenReturn(Map.of("gbp", "", "eur", ""));
    when(mockClient.exchange(eq("latest"), eq("gbp")))
        .thenReturn(Map.of("gbp", Map.of("eur", 1.0)));

    return mockClient;
  }

}
