package tech.cusbo.msteams.demo.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final GraphServiceClient graphServiceClient;
  private final ObjectMapper objectMapper;

  @GetMapping
  @SneakyThrows
  public String getUsers() {
    List<User> users = graphServiceClient.users().get().getValue();
    return objectMapper.writeValueAsString(users);
  }

  @GetMapping("/guests")
  @SneakyThrows
  public String getGuestUsers() {
    UserCollectionResponse response = graphServiceClient
        .users()
        .get(r -> {
          r.queryParameters.filter = "userType eq 'Guest'";
          r.queryParameters.select = new String[]{
              "id",
              "displayName",
              "mail",
              "userPrincipalName",
              "userType",
              "externalUserState"
          };
          r.queryParameters.top = 50;
        });

    List<User> guests = response.getValue();
    return objectMapper.writeValueAsString(guests);
  }
}
