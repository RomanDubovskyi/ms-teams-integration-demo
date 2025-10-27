package tech.cusbo.msteams.demo.inboundevent.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
public class SubscriptionAdminController {

  private final GraphSubscriptionService subscriptionService;

  @PostMapping("/ensure")
  public ResponseEntity<?> subscribeOrgWide() {
    subscriptionService.ensureAppSubscriptions();
    return ResponseEntity.ok("Org-wide subscriptions created/ensured.");
  }
}