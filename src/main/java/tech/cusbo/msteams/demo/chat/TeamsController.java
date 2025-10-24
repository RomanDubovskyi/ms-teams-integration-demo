package tech.cusbo.msteams.demo.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.Channel;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Team;
import com.microsoft.graph.serviceclient.GraphServiceClient;
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
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamsController {

  private final GraphServiceClient graphClient;
  private final ObjectMapper objectMapper;

  @GetMapping("/joined")
  @SneakyThrows
  public ResponseEntity<List<Team>> getJoinedTeams() {
    List<Team> joinedTeams = graphClient.me().joinedTeams().get().getValue();
    return ResponseEntity.ok(joinedTeams);
  }

  @GetMapping("/{teamId}/channels")
  @SneakyThrows
  public ResponseEntity<List<Channel>> getTeamsChannels(@PathVariable("teamId") String teamId) {
    List<Channel> channels = graphClient.teams().byTeamId(teamId).channels().get().getValue();
    return ResponseEntity.ok(channels);
  }

  @GetMapping("/{teamId}/channels/{channelId}")
  @SneakyThrows
  public ResponseEntity<Channel> getTeamChannel(
      @PathVariable("teamId") String teamId,
      @PathVariable("channelId") String channelId
  ) {
    Channel channel = graphClient.teams().byTeamId(teamId)
        .channels().byChannelId(channelId).get();
    return ResponseEntity.ok(channel);
  }


  @GetMapping("/{teamId}/channels/{channelId}/messages")
  @SneakyThrows
  public ResponseEntity<List<ChatMessage>> getTeamChannelMessages(
      @PathVariable("teamId") String teamId,
      @PathVariable("channelId") String channelId
  ) {
    List<ChatMessage> messages = graphClient.teams().byTeamId(teamId)
        .channels().byChannelId(channelId)
        .messages().get()
        .getValue();
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/{teamId}/channels/{channelId}/messages")
  @SneakyThrows
  public ResponseEntity<ChatMessage> postMessageToChannel(
      @PathVariable("teamId") String teamId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, String> postMessageBody
  ) {
    ChatMessage message = new ChatMessage();
    ItemBody body = new ItemBody();
    body.setContentType(BodyType.Html);
    body.setContent(postMessageBody.get("content"));
    message.setBody(body);

    ChatMessage created = graphClient.teams().byTeamId(teamId)
        .channels().byChannelId(channelId)
        .messages()
        .post(message);

    return ResponseEntity.ok(created);
  }
}
