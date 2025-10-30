package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.kiota.serialization.ParseNode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationMemberEventHandler implements ChangeEventHandler {

  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ParseNode decryptedContent, ChangeNotification event) {
    ConversationMember conversationMember = decryptedContent.getObjectValue(
        ConversationMember::createFromDiscriminatorValue
    );
    log.info(
        "GOT the following with content to handle {} \n with action {}",
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(conversationMember),
        event.getChangeType().getValue()
    );
  }

  @Override
  public Set<String> getODataTypes() {
    return Set.of(
        "#Microsoft.Graph.conversationMember",
        // All others are subtypes, added for safety
        "#Microsoft.Graph.aadUserConversationMember",
        "#Microsoft.Graph.anonymousGuestConversationMember",
        "#Microsoft.Graph.azureCommunicationServicesUserConversationMember",
        "#Microsoft.Graph.microsoftAccountUserConversationMember",
        "#Microsoft.Graph.skypeForBusinessUserConversationMember",
        "#Microsoft.Graph.skypeUserConversationMember"
    );
  }
}
