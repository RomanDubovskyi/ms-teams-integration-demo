package tech.cusbo.msteams.demo.inboundevent.handler.change;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.Channel;
import com.microsoft.graph.models.Team;
import com.microsoft.kiota.serialization.ParseNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamEventHandler implements ChangeEventHandler {

  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void handle(ParseNode parseNode, ChangeNotification event) {
    Team team = parseNode.getObjectValue(Team::createFromDiscriminatorValue);
    log.info(
        "GOT the following with content to handle {} \n with action {}",
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(team),
        event.getChangeType().getValue()
    );
  }

  @Override
  public String getODataType() {
    return "#Microsoft.Graph.team";
  }
}
