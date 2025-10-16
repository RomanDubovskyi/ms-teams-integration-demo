package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.JsonNode;

public interface ChangeEventHandler {

  void handle(JsonNode jsonNode);

  String getEventType();
}
