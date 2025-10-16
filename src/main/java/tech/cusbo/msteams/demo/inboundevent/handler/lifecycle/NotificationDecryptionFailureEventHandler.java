package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationDecryptionFailureEventHandler implements LifeCycleEventsHandler {

  @Override
  public void handle(JsonNode event) {
    throw new RuntimeException("Graph API got decryption error from spring on change event, "
        + "THIS SHOULDN'T normally happen check decryption service, full event "
        + event.toPrettyString());
  }

  @Override
  public String getEventType() {
    return "notificationDecryptionFailure";
  }
}
