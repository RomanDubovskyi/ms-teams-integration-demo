package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.Chat;
import com.microsoft.kiota.serialization.ParseNode;
import java.util.Set;
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
  public void handle(ParseNode decryptedContent, ChangeNotification event) {
    Chat chat = decryptedContent.getObjectValue(Chat::createFromDiscriminatorValue);
    log.info(
        "GOT the following with content to handle {} \n with action {}",
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(chat),
        event.getChangeType().getValue()
    );
  }

  @Override
  public Set<String> getODataTypes() {
    return Set.of("#Microsoft.Graph.chat");
  }
}
