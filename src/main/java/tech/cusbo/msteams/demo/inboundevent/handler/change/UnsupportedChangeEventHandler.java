package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedChangeEventHandler implements ChangeEventHandler {

  @Override
  public void handle(JsonNode event) {
    throw new RuntimeException("GOT unsupported event " + event.toPrettyString());
  }

  @Override
  public String getEventType() {
    return "unsupported";
  }
}
