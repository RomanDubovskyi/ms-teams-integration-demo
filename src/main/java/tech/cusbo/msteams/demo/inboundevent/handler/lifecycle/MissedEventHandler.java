package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MissedEventHandler implements LifeCycleEventsHandler {

  @Override
  public void handle(JsonNode event) {
    // TODO: add here logic of re-syncing chat messages, channel updates, etc (depending on sub)
    throw new RuntimeException("MISSED events, has to resync related resource data, "
        + " full event " + event.toPrettyString());
  }

  @Override
  public String getEventType() {
    return "missed";
  }
}
