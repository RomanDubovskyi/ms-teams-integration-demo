package tech.cusbo.msteams.demo.security;

import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final GraphServiceClient graphClient;

  @GetMapping("/logged-in")
  public ResponseEntity<String> user() {
    User loggedInUser = graphClient.me().get();
    String displayInfo = loggedInUser != null ? loggedInUser.getMail() : "<No logged in users>";
    return ResponseEntity.ok(displayInfo);
  }
}
