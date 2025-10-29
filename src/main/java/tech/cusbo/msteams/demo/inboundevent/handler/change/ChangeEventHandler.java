package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.kiota.serialization.ParseNode;

public interface ChangeEventHandler {

  void handle(ParseNode parseNode, ChangeNotification event);

  String getODataType();
}
