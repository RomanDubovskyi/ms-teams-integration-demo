package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.kiota.serialization.ParseNode;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedChangeEventHandler implements ChangeEventHandler {

  @Override
  public void handle(ParseNode parseNode, ChangeNotification event) {
    throw new RuntimeException("GOT unsupported event " + parseNode);
  }

  @Override
  public String getODataType() {
    return "unsupported";
  }
}
