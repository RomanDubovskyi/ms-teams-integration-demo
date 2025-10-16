package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationChallengeEventHandler implements LifeCycleEventsHandler {

  @Override
  public void handle(JsonNode event) {
    throw new RuntimeException("THIS EVENT HAS TO BE DEALT WITH ON CONTROLLER LVL"
        + " SHOULD NEVER REACH THIS PART, full event" + event.toPrettyString());
  }

  @Override
  public String getEventType() {
    return "authenticationChallenge";
  }
}
