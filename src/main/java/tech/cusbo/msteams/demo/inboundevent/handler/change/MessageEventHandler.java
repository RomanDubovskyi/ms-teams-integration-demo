package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventHandler implements ChangeEventHandler {

  @Override
  public void handle(JsonNode jsonNode) {
    log.info("GOT the following content to handle {} ", jsonNode.toPrettyString());
  }

  @Override
  public String getEventType() {
    return "message";
  }
}
