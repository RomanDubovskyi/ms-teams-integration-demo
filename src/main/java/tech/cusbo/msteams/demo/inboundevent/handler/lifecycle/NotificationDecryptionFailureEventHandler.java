package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDecryptionFailureEventHandler implements LifeCycleEventsHandler {

  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ChangeNotification event) {
    throw new RuntimeException("Graph API got decryption error from spring on change event, "
        + "THIS SHOULDN'T normally happen check decryption service, full event "
        + objectMapper.writeValueAsString(event));
  }

  @Override
  public String getEventType() {
    return "notificationDecryptionFailure";
  }
}
