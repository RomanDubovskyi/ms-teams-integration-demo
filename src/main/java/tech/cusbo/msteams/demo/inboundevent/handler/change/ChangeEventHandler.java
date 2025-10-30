package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.kiota.serialization.ParseNode;
import java.util.Set;

public interface ChangeEventHandler {

  void handle(ParseNode decryptedContent, ChangeNotification event);

  Set<String> getODataTypes();
}
