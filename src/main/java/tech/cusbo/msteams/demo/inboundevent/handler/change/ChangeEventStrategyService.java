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
  public void pickHandlerAndProcessAsync(ChangeNotification event) {
    String eventSecret = event.getClientState();
    var subscription = subscriptionService.findByExternalId(event.getSubscriptionId());
    if (subscription.isEmpty() || !Objects.equals(eventSecret, subscription.get().getSecret())) {
      throw new SecurityException("Invalid secret [clientState] field in the event, "
          + "can't prove identity, full event" + event);
    }

    byte[] contentBytes = encryptionService
        .decryptNotificationContent(event.getEncryptedContent());
    log.info("Decrypted event payload: {}", new String(contentBytes));
    JsonParseNodeFactory parseNodeFactory = new JsonParseNodeFactory();
    ParseNode parsedContent = parseNodeFactory.getParseNode(
        "application/json",
        new ByteArrayInputStream(contentBytes)
    );

    String dataType = event.getResourceData().getOdataType();
    ChangeEventHandler defaultHandler = changeEventHandlerMap.get("unsupported");
    ChangeEventHandler handler = changeEventHandlerMap.getOrDefault(dataType, defaultHandler);
    handler.handle(parsedContent, event);
  }
}
