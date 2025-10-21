package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.cusbo.msteams.demo.inboundevent.GraphEventsEncryptionService;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphSubscriptionService;

@Slf4j
@Service
public class ChangeEventStrategyService {

  private final GraphSubscriptionService subscriptionService;
  private final Map<String, ChangeEventHandler> changeEventHandlerMap;
  private final GraphEventsEncryptionService encryptionService;

  public ChangeEventStrategyService(
      @Qualifier("changeEventHandlerMap") Map<String, ChangeEventHandler> changeEventHandlerMap,
      GraphEventsEncryptionService encryptionService,
      GraphSubscriptionService subscriptionService
  ) {
    this.changeEventHandlerMap = changeEventHandlerMap;
    this.encryptionService = encryptionService;
    this.subscriptionService = subscriptionService;
  }

  @Async
  public void pickHandlerAndProcessAsync(JsonNode event) {
    String eventSecret = event.path("clientState").asText();
    String subscriptionId = event.path("subscriptionId").asText();
    var subscription = subscriptionService.findByExternalId(subscriptionId);
    if (subscription.isEmpty() || !Objects.equals(eventSecret, subscription.get().getSecret())) {
      throw new SecurityException("Invalid secret [clientState] field in the event, "
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
