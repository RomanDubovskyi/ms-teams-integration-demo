package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LifeCycleEventStrategyService {

  private final Map<String, LifeCycleEventsHandler> lifecycleEventHandlers;

  public LifeCycleEventStrategyService(
      @Qualifier("lifecycleEventHandlers") Map<String, LifeCycleEventsHandler> lifecycleEventHandlers
  ) {
    this.lifecycleEventHandlers = lifecycleEventHandlers;
  }

  @Async
  public void handleLifeCycleEventAsync(JsonNode event) {
    String eventType = event.path("lifecycleEvent").asText();
    log.info("RECEIVED event {} to process in strategy service", event);
    LifeCycleEventsHandler handler = lifecycleEventHandlers.get(eventType);
    handler.handle(event);
  }
}
