package tech.cusbo.msteams.demo.inboundevent.handler.lifecycle;

import com.azure.core.credential.AccessToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphSubscriptionUserRepository;
import tech.cusbo.msteams.demo.security.oauth.OauthToken;
import tech.cusbo.msteams.demo.security.oauth.OauthTokenRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReauthorizedRequestEventHandler implements LifeCycleEventsHandler {

  private final GraphSubscriptionUserRepository subscriptionUserRepository;
  private final OauthTokenRepository oauthTokenRepository;

  @Override
  public void handle(JsonNode event) {
    String subscriptionId = event.path("subscriptionId").asText();
    log.info("Reauthorization requested for subscription {}", subscriptionId);
    String multitenantUserId = subscriptionUserRepository.get(subscriptionId).orElseThrow(
        () -> new RuntimeException(
            "Can't reauthorize, no USER for subscription " + subscriptionId
        )
    );

    OauthToken oauthToken = oauthTokenRepository.get(multitenantUserId).orElseThrow(
        () -> new RuntimeException(
            "Can't reauthorize, no valid user access token for USER " + multitenantUserId
        )
    );
    Subscription prolongedSub = new Subscription();
    prolongedSub.setExpirationDateTime(OffsetDateTime.now().plusDays(2));
    GraphServiceClient graphClient = new GraphServiceClient(request -> {
      var tokenTtl = OffsetDateTime.ofInstant(oauthToken.expiresAt(), ZoneId.systemDefault());
      var accessToken = new AccessToken(oauthToken.accessToken(), tokenTtl);
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
