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
      @Qualifier("lifecycleEventHandlers") Map<String, LifeCycleEventsHandler> lifecycleHandlers
  ) {
    this.lifecycleEventHandlers = lifecycleHandlers;
  }

  @Async
  public void handleLifeCycleEventAsync(JsonNode event) {
    String eventType = event.path("lifecycleEvent").asText();
    log.info("RECEIVED event {} to process in strategy service", event);
    LifeCycleEventsHandler defaultHandler = lifecycleEventHandlers.get("unsupported");
    LifeCycleEventsHandler handler = lifecycleEventHandlers.getOrDefault(eventType, defaultHandler);
    handler.handle(event);
  }
}
