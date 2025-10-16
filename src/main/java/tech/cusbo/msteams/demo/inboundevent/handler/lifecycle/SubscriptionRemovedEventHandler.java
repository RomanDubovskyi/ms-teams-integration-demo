package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscriptionRemovedEventHandler implements LifeCycleEventsHandler {

  @Override
  public void handle(JsonNode event) {
    // TODO: ideally we could add here re-sub logic as additional measure to recover sub
    throw new RuntimeException("Subscription got invalidated, re-fresh logic didn't work properly, "
        + " full inbound event " + event.toPrettyString());
  }

  @Override
  public String getEventType() {
    return "subscriptionRemoved";
  }
}
