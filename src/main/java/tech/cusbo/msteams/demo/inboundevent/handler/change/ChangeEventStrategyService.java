package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.cusbo.msteams.demo.inboundevent.GraphEventsEncryptionService;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphSubscriptionSecretRepository;

@Slf4j
@Service
public class ChangeEventStrategyService {

  private final GraphSubscriptionSecretRepository subscriptionSecretRepository;
  private final Map<String, ChangeEventHandler> changeEventHandlerMap;
  private final GraphEventsEncryptionService encryptionService;

  public ChangeEventStrategyService(
      @Qualifier("changeEventHandlerMap") Map<String, ChangeEventHandler> changeEventHandlerMap,
      GraphEventsEncryptionService encryptionService,
      GraphSubscriptionSecretRepository subscriptionSecretRepository
  ) {
    this.changeEventHandlerMap = changeEventHandlerMap;
    this.encryptionService = encryptionService;
    this.subscriptionSecretRepository = subscriptionSecretRepository;
  }

  @Async
  public void pickHandlerAndProcessAsync(JsonNode event) {
    String secretFromEvent = event.path("clientState").asText();
    String subscriptionId = event.path("subscriptionId").asText();
    Optional<String> storedSubscriptionSecret = subscriptionSecretRepository.get(subscriptionId);
    if (storedSubscriptionSecret.isEmpty()
        || !Objects.equals(secretFromEvent, storedSubscriptionSecret.get())) {
      throw new RuntimeException("Invalid secret [clientState] field in the event, "
          + "can't prove identity, full event" + event.toPrettyString());
    }

    JsonNode encryptedContent = event.path("encryptedContent");
    JsonNode decryptedContent = encryptionService.decryptEvent(encryptedContent);
    log.info("Decrypted event payload: {}", decryptedContent);
    String eventType = decryptedContent.path("messageType").asText();
    log.info("RECEIVED change event {} to process in strategy service", decryptedContent);
    ChangeEventHandler defaultHandler = changeEventHandlerMap.get("unsupported");
    ChangeEventHandler handler = changeEventHandlerMap.getOrDefault(eventType, defaultHandler);
    handler.handle(decryptedContent);
  }
}
