package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.kiota.serialization.JsonParseNodeFactory;
import com.microsoft.kiota.serialization.ParseNode;
import java.io.ByteArrayInputStream;
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
  public void pickHandlerAndProcessAsync(ChangeNotification topLevelEvent) {
    String eventSecret = topLevelEvent.getClientState();
    var subscription = subscriptionService.findByExternalId(topLevelEvent.getSubscriptionId());
    if (subscription.isEmpty() || !Objects.equals(eventSecret, subscription.get().getSecret())) {
      throw new SecurityException("Invalid secret [clientState] field in the event, "
          + "can't prove identity, full event" + topLevelEvent);
    }

    byte[] decryptedBytes = encryptionService
        .decryptNotificationContent(topLevelEvent.getEncryptedContent());
    log.info("Decrypted event payload: {}", new String(decryptedBytes));
    JsonParseNodeFactory parseNodeFactory = new JsonParseNodeFactory();
    ParseNode decryptedContent = parseNodeFactory.getParseNode(
        "application/json",
        new ByteArrayInputStream(decryptedBytes)
    );

    String dataType = topLevelEvent.getResourceData().getOdataType();
    ChangeEventHandler defaultHandler = changeEventHandlerMap.get("unsupported");
    ChangeEventHandler handler = changeEventHandlerMap.getOrDefault(dataType, defaultHandler);
    handler.handle(decryptedContent, topLevelEvent);
  }
}
