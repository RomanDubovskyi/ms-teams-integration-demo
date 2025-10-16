package tech.cusbo.msteams.demo.security;

import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final GraphServiceClient graphClient;

  @GetMapping("/logged-in")
  public ResponseEntity<String> displayCurrentUser() {
    User loggedInUser = graphClient.me().get();
    String displayInfo = loggedInUser != null ? loggedInUser.getMail() : "<No logged in users>";
    return ResponseEntity.ok(displayInfo);
  }
}
