package tech.cusbo.msteams.demo.inboundevent.subscription;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.cusbo.msteams.demo.security.SecureRandomGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphApiSubscriptionService {

  private final GraphSubscriptionSecretRepo subSecretRepo;
  private final GraphServiceClient graphClient;

  @Value("${api.graph.inbound.webhook-url}")
  private String apiInboundEventsUrl;
  @Value("${api.graph.lifecycle.webhook-url}")
  private String apiInboundLifeCycleEventsUrl;

  private final List<GraphSubscriptionResourceDto> defaultSubs = List.of(
      new GraphSubscriptionResourceDto(
          "/me/chats",
          List.of("created", "updated", "deleted")
      ),
      new GraphSubscriptionResourceDto(
          "/me/chats/getAllMessages",
          List.of("created")
      )
  );

  @Async
  public void ensureEventSubscriptionsForLoggedInUserAsync() {
    List<Subscription> currSubs = graphClient.subscriptions().get().getValue();
    Map<String, Subscription> currSubResourceMap = currSubs.stream()
        .collect(Collectors.toMap(Subscription::getResource, Function.identity()));
    String mail = graphClient.me().get().getMail();

    for (GraphSubscriptionResourceDto targetSub : defaultSubs) {
      if (currSubResourceMap.containsKey(targetSub.resource())) {
        continue;
      }

      try {
        Subscription newSub = createSubscription(targetSub);
        log.info("created sub for user {}, sub id: {}", mail, newSub.getId());
      } catch (Exception e) {
        log.error("couldn't subscribe for ms resource {}", targetSub, e);
      }
    }
  }

  public Subscription createSubscription(GraphSubscriptionResourceDto subscriptionResourceDto) {
    Subscription subscription = new Subscription();
    subscription.setChangeType(String.join(",", subscriptionResourceDto.changeTypes()));
    subscription.setNotificationUrl(apiInboundEventsUrl);
    subscription.setLifecycleNotificationUrl(apiInboundLifeCycleEventsUrl);
    subscription.setResource(subscriptionResourceDto.resource());
    subscription.setExpirationDateTime(OffsetDateTime.now().plusDays(2));
    String clientStateSecret = SecureRandomGenerator.generateSecureRandomBase64String();
    subscription.setClientState(clientStateSecret);

    Subscription newSub = graphClient.subscriptions()
        .post(subscription);

    subSecretRepo.save(subscription.getId(), clientStateSecret);
    return newSub;
  }

  public Subscription renewSubscription(String subscriptionId) {
    Subscription subscription = new Subscription();
    subscription.setExpirationDateTime(OffsetDateTime.now().plusDays(2));

    Subscription patchedSub = graphClient
        .subscriptions()
        .bySubscriptionId(subscriptionId)
        .patch(subscription);

    subSecretRepo.extendTtlForDefaultPeriod(subscriptionId);
    return patchedSub;
  }
}
