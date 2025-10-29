package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.kiota.serialization.ParseNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventHandler implements ChangeEventHandler {

  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ParseNode decryptedContent, ChangeNotification event) {
    ChatMessage message = decryptedContent.getObjectValue(
        ChatMessage::createFromDiscriminatorValue
    );
    log.info("GOT the following with content to handle {} \n with action {}",
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message),
        event.getChangeType().getValue());
  }

  @Override
  public String getODataType() {
    return "#Microsoft.Graph.chatMessage";
  }
}
