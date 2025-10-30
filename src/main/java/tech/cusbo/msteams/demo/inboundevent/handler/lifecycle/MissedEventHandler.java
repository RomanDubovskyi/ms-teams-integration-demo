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
public class MissedEventHandler implements LifeCycleEventsHandler {

  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ChangeNotification event) {
    // TODO: add here logic of re-syncing chat messages, channel updates, etc (depending on sub)
    throw new RuntimeException("MISSED events, has to resync related resource data, "
        + " full event " + objectMapper.writeValueAsString(event));
  }

  @Override
  public String getEventType() {
    return "missed";
  }
}
