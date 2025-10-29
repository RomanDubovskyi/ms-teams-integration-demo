package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.Chat;
import com.microsoft.kiota.serialization.ParseNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventHandler implements ChangeEventHandler {
  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ParseNode parseNode, ChangeNotification event) {
    Chat chat = parseNode.getObjectValue(Chat::createFromDiscriminatorValue);
    log.info(
        "GOT the following with content to handle {} \n with action {}",
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(chat),
        event.getChangeType().getValue()
    );
  }

  @Override
  public String getODataType() {
    return "#Microsoft.Graph.chat";
  }
}
