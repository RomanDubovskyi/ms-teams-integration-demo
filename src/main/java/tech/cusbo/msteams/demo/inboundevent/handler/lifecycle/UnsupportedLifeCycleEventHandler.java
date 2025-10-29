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
public class UnsupportedLifeCycleEventHandler implements LifeCycleEventsHandler {

  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ChangeNotification event) {
    throw new RuntimeException(
        "Received unsupported lifecycle event " + objectMapper.writeValueAsString(event)
    );
  }

  @Override
  public String getEventType() {
    return "unsupported";
  }
}
