package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.azure.core.credential.AccessToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphSubscriptionUserRepository;
import tech.cusbo.msteams.demo.security.oauth.MsGraphOauthTokenService;
import tech.cusbo.msteams.demo.security.oauth.OauthToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReauthorizedRequestEventHandler implements LifeCycleEventsHandler {

  private final GraphSubscriptionUserRepository subscriptionUserRepository;
  private final MsGraphOauthTokenService oauthTokenService;

  @Override
  public void handle(JsonNode event) {
    String subscriptionId = event.path("subscriptionId").asText();
    log.info("Reauthorization requested for subscription {}", subscriptionId);
    String multitenantUserId = subscriptionUserRepository.get(subscriptionId).orElseThrow(
        () -> new RuntimeException(
            "Can't reauthorize, no USER for subscription " + subscriptionId
        )
    );

    OauthToken oauthToken = oauthTokenService.findByMultitenantUserId(multitenantUserId)
        .orElseThrow(
        () -> new RuntimeException(
            "Can't reauthorize, no valid user access token for USER " + multitenantUserId
        )
    );
    if (oauthToken.needsRefresh()) {
      oauthToken = oauthTokenService.refreshToken(oauthToken);
    }
    Subscription prolongedSub = new Subscription();
    prolongedSub.setExpirationDateTime(OffsetDateTime.now().plusDays(2));
    String requestAccessToken = oauthToken.getAccessToken();
    Instant requestTokenExpiresAt = oauthToken.getExpiresAt();
    GraphServiceClient graphClient = new GraphServiceClient(request -> {
      var tokenTtl = OffsetDateTime.ofInstant(requestTokenExpiresAt, ZoneId.systemDefault());
      var accessToken = new AccessToken(requestAccessToken, tokenTtl);
      return Mono.just(accessToken);
    });
    graphClient
        .subscriptions()
        .bySubscriptionId(subscriptionId)
        .patch(prolongedSub);

    log.info(
        "Successfully reauthorized subscription {} for {}",
        subscriptionId,
        multitenantUserId
    );
  }

  @Override
  public String getEventType() {
    return "reauthorizationRequired";
  }
}
