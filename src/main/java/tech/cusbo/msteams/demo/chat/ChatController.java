package tech.cusbo.msteams.demo.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.Chat;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ChatMessageCollectionResponse;
import com.microsoft.graph.models.ChatType;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

  private final GraphServiceClient graphClient;
  private final ObjectMapper objectMapper;

  @PostMapping
  @SneakyThrows
  public ResponseEntity<String> startChat(@RequestBody CreateChatDto createChatDto) {
    var me = graphClient.me().get();
    Chat newChat = constructChatModel(createChatDto, me);
    Chat created = graphClient.chats().post(newChat);
    return ResponseEntity.ok(objectMapper.writeValueAsString(created));
  }

  @GetMapping("/{chatId}")
  @SneakyThrows
  public String getChatById(@PathVariable String chatId) {
    Chat chat = graphClient.me()
        .chats()
        .byChatId(chatId)
        .get(r -> r.queryParameters.select = new String[]{"id", "topic", "chatType"});
    return objectMapper.writeValueAsString(chat);
  }

  @GetMapping
  @SneakyThrows
  public String getAuthUserChats() {
    List<Chat> chats = graphClient.me().chats().get().getValue();
    return objectMapper.writeValueAsString(chats);
  }

  @GetMapping("/{chatId}/messages")
  @SneakyThrows
  public String getChatMessages(@PathVariable String chatId) {
    ChatMessageCollectionResponse messages = graphClient.chats()
        .byChatId(chatId).messages().get();
    return objectMapper.writeValueAsString(messages);
  }

  @PostMapping("/{chatId}/messages")
  @SneakyThrows
  public ResponseEntity<String> postMessage(
      @PathVariable String chatId,
      @RequestBody Map<String, String> payload
  ) {
    ChatMessage message = new ChatMessage();
    ItemBody body = new ItemBody();
    body.setContentType(BodyType.Html);
    body.setContent(payload.get("content"));
    message.setBody(body);

    ChatMessage created = graphClient
        .chats()
        .byChatId(chatId)
        .messages()
        .post(message);

    return ResponseEntity.ok(objectMapper.writeValueAsString(created));
  }

  private Chat constructChatModel(CreateChatDto createChatDto, User me) {
    AadUserConversationMember authorMember = new AadUserConversationMember();
    authorMember.setRoles(List.of("owner"));
    authorMember.setUserId(me.getId());
    authorMember.getAdditionalData().put(
        "user@odata.bind",
        "https://graph.microsoft.com/v1.0/users('" + me.getId() + "')"
    );

    var members = Arrays.stream(createChatDto.memberIds())
        .map(id -> {
          AadUserConversationMember m = new AadUserConversationMember();
          m.setRoles(List.of("owner"));
          m.setUserId(id);
          m.getAdditionalData().put(
              "user@odata.bind",
              "https://graph.microsoft.com/v1.0/users('" + id + "')"
          );
          return (ConversationMember) m;
        })
        .toList();

    List<ConversationMember> allMembers = new ArrayList<>();
    allMembers.add(authorMember);
    allMembers.addAll(members);

    Chat newChat = new Chat();
    newChat.setChatType(createChatDto.type());
    newChat.setTopic(createChatDto.topic());
    newChat.setMembers(allMembers);
    return newChat;
  }
}
