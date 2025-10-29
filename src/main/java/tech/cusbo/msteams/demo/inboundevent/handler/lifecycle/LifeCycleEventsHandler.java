package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.microsoft.graph.models.ChangeNotification;

public interface LifeCycleEventsHandler {

  void handle(ChangeNotification event);

  String getEventType();
}
