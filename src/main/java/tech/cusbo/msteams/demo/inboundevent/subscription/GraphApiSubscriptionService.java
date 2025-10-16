package tech.cusbo.msteams.demo.inboundevent.subscription;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.Duration;
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
import tech.cusbo.msteams.demo.inboundevent.GraphEventsEncryptionService;
import tech.cusbo.msteams.demo.security.util.SecureRandomGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphApiSubscriptionService {

  private static final int ALLOWED_DAYS_BEFORE_EXP = 2;
  private final GraphEventsEncryptionService encryptionKeyProvider;
  private final GraphSubscriptionSecretRepository graphSubscriptionSecretRepo;
  private final GraphSubscriptionUserRepository userGraphSubscriptionsRepo;
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
  public void ensureEventSubscriptionsForLoggedInUserAsync(String tenantId, String msUserId) {
    List<Subscription> currSubs = graphClient.subscriptions().get().getValue();
    Map<String, Subscription> currSubResourceMap = currSubs.stream()
        .collect(Collectors.toMap(Subscription::getResource, Function.identity()));
    String mail = graphClient.me().get().getMail();

    for (GraphSubscriptionResourceDto targetSub : defaultSubs) {
      if (currSubResourceMap.containsKey(targetSub.resource())) {
        continue;
      }

      try {
        var newSub = createSubscription(targetSub);
        log.info("created sub for user {}, sub id: {}", mail, newSub.getId());
        var subTtl = Duration.between(OffsetDateTime.now(), newSub.getExpirationDateTime());
        userGraphSubscriptionsRepo.save(newSub.getId(), tenantId, msUserId, subTtl);
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
    subscription.setIncludeResourceData(true);
    OffsetDateTime expireAt = OffsetDateTime.now().plusDays(ALLOWED_DAYS_BEFORE_EXP);
    subscription.setExpirationDateTime(expireAt);
    String clientStateSecret = SecureRandomGenerator.generateSecureRandomBase64String();
    subscription.setClientState(clientStateSecret);
    subscription.setEncryptionCertificate(encryptionKeyProvider.getPublicKeyBase64());
    subscription.setEncryptionCertificateId(encryptionKeyProvider.getEncryptionKeyId());

    var newSub = graphClient.subscriptions().post(subscription);
    graphSubscriptionSecretRepo.save(
        subscription.getId(),
        clientStateSecret,
        Duration.between(OffsetDateTime.now(), expireAt)
    );

    return newSub;
  }
}
