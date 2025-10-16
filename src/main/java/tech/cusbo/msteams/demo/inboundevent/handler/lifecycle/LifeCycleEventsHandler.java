package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;

public interface LifeCycleEventsHandler {

  void handle(JsonNode event);

  String getEventType();
}
