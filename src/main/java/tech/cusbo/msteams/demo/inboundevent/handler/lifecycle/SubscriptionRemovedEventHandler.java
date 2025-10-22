package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;


import com.fasterxml.jackson.databind.JsonNode;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphSubscriptionService;
import tech.cusbo.msteams.demo.inboundevent.subscription.SubscriptionState;

@Component
@RequiredArgsConstructor
public class SubscriptionRemovedEventHandler implements LifeCycleEventsHandler {

  private final GraphSubscriptionService subscriptionService;

  @Override
  public void handle(JsonNode event) {
    String subscriptionId = event.path("subscriptionId").asText();
    var subscription = subscriptionService.findByExternalId(subscriptionId)
        .orElseThrow(
            () -> new NoSuchElementException("Not subscription found by id " + subscriptionId)
        );
    subscription.setSubscriptionState(SubscriptionState.expired);
    subscriptionService.save(subscription);
    // TODO: ideally we could add here re-sub logic as additional measure to recover sub
  }

  @Override
  public String getEventType() {
    return "subscriptionRemoved";
  }
}
