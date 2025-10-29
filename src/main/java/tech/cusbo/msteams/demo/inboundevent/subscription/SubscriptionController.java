package tech.cusbo.msteams.demo.inboundevent.subscription;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

  private final GraphSubscriptionService subscriptionService;
  private final GraphServiceClient appGraphClient;
  private final GraphServiceClient oauthGraphClient;

  public SubscriptionController(
      GraphSubscriptionService subscriptionService,
      @Qualifier("oauthScopeServiceClient") GraphServiceClient oauthGraphClient,
      @Qualifier("appScopeServiceClient") GraphServiceClient appGraphClient
  ) {
    this.subscriptionService = subscriptionService;
    this.oauthGraphClient = oauthGraphClient;
    this.appGraphClient = appGraphClient;
  }

  @PostMapping("/ensure")
  public ResponseEntity<String> subscribeOrgWide() {
    subscriptionService.ensureEventSubscriptionsByApp();
    return ResponseEntity.ok("Org-wide subscriptions created/ensured.");
  }

  @GetMapping("/app")
  public ResponseEntity<List<Subscription>> getAppSubscriptions() {
    List<Subscription> subs = appGraphClient.subscriptions().get().getValue();
    return ResponseEntity.ok(subs);
  }

  @GetMapping("/me")
  public ResponseEntity<List<Subscription>> getLoggedInUserSubscriptions() {
    List<Subscription> subs = oauthGraphClient.subscriptions().get().getValue();
    return ResponseEntity.ok(subs);
  }

  @DeleteMapping("/me/unsubscribe")
  public ResponseEntity<String> removeLoggedInUserSubscriptions() {
    oauthGraphClient.subscriptions().get().getValue().forEach(s -> {
          oauthGraphClient.subscriptions().bySubscriptionId(s.getId()).delete();
        }
    );
    return ResponseEntity.ok("ok");
  }

  @DeleteMapping("/app/unsubscribe")
  public ResponseEntity<String> removeAppSubscriptions() {
    appGraphClient.subscriptions().get().getValue().forEach(s -> {
          appGraphClient.subscriptions().bySubscriptionId(s.getId()).delete();
        }
    );
    return ResponseEntity.ok("ok");
  }
}
