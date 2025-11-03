package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.azure.core.credential.AccessToken;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphEventsSubscription;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphSubscriptionService;
import tech.cusbo.msteams.demo.inboundevent.subscription.SubscriptionOwnerType;
import tech.cusbo.msteams.demo.inboundevent.subscription.SubscriptionState;

@Slf4j
@Component
public class ReauthorizedRequestEventHandler implements LifeCycleEventsHandler {

  private final GraphSubscriptionService subscriptionService;
  private final OAuth2AuthorizedClientManager clientManager;
  private final GraphServiceClient appGraphClient;


  public ReauthorizedRequestEventHandler(
      @Qualifier("appScopeServiceClient") GraphServiceClient appGraphClient,
      GraphSubscriptionService subscriptionService,
      OAuth2AuthorizedClientManager clientManager
  ) {
    this.appGraphClient = appGraphClient;
    this.subscriptionService = subscriptionService;
    this.clientManager = clientManager;
  }

  @Override
  public void handle(ChangeNotification event) {
    String subscriptionId = event.getSubscriptionId();
    log.info("Reauthorization requested for subscription {}", subscriptionId);
    GraphEventsSubscription subscription = subscriptionService.findByExternalId(subscriptionId)
        .orElseThrow(
            () -> new RuntimeException(
                "Can't reauthorize, no USER for subscription " + subscriptionId
            )
        );

    GraphServiceClient graphClient = subscription.getOwnerType() == SubscriptionOwnerType.app
        ? appGraphClient
        : getRequestScopedClient(subscription);
    Subscription prolongedSub = new Subscription();
    prolongedSub.setExpirationDateTime(OffsetDateTime.now().plusDays(2));
    graphClient
        .subscriptions()
        .bySubscriptionId(subscriptionId)
        .patch(prolongedSub);
    subscription.setSubscriptionState(SubscriptionState.active);
    subscriptionService.save(subscription);
    log.info(
        "Successfully reauthorized subscription with external id {} for {}",
        subscriptionId,
        subscription.getMultitenantUserId()
    );
  }

  private GraphServiceClient getRequestScopedClient(GraphEventsSubscription subscription) {
    String multitenantUserId = subscription.getMultitenantUserId();
    var req = OAuth2AuthorizeRequest
        .withClientRegistrationId("azure")
        .principal(multitenantUserId)
        .build();

    var client = clientManager.authorize(req);
    if (client == null || client.getAccessToken() == null) {
      throw new SecurityException("Cannot load/refresh user token for " + multitenantUserId);
    }

    var accessToken = client.getAccessToken();
    log.info("Loaded client successfully, using clientManager");
    return new GraphServiceClient(request -> {
      var tok = new AccessToken(
          accessToken.getTokenValue(),
          OffsetDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault())
      );
      return Mono.just(tok);
    });
  }

  @Override
  public String getEventType() {
    return "reauthorizationRequired";
  }
}
