package tech.cusbo.msteams.demo.inboundevent.subscription;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.cusbo.msteams.demo.inboundevent.GraphEventsEncryptionService;
import tech.cusbo.msteams.demo.security.util.MsGraphMultiTenantKeyUtil;
import tech.cusbo.msteams.demo.security.util.SecureRandomGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphSubscriptionService {

  private static final int ALLOWED_DAYS_BEFORE_EXP = 2;
  private final GraphSubscriptionRepository graphSubscriptionRepository;
  private final GraphEventsEncryptionService encryptionKeyProvider;
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

    for (GraphSubscriptionResourceDto targetSub : defaultSubs) {
      if (currSubResourceMap.containsKey(targetSub.resource())) {
        continue;
      }

      try {
        var newSub = sendCreateSubscriptionRequest(targetSub);
        log.info("created sub for user {}, sub id: {}", msUserId, newSub.getId());
        GraphEventsSubscription internalSubInfo = new GraphEventsSubscription();
        internalSubInfo.setExternalId(newSub.getId());
        internalSubInfo.setSecret(newSub.getClientState());
        String multitenantUserId = MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId);
        internalSubInfo.setMultitenantUserId(multitenantUserId);
        internalSubInfo.setSubscriptionState(SubscriptionState.active);
        graphSubscriptionRepository.save(internalSubInfo);
      } catch (Exception e) {
        log.error("couldn't subscribe for ms resource {}", targetSub, e);
      }
    }
  }

  private Subscription sendCreateSubscriptionRequest(
      GraphSubscriptionResourceDto subscriptionResourceDto
  ) {
    Subscription subscription = new Subscription();
    subscription.setChangeType(String.join(",", subscriptionResourceDto.changeTypes()));
    subscription.setNotificationUrl(apiInboundEventsUrl);
    subscription.setLifecycleNotificationUrl(apiInboundLifeCycleEventsUrl);
    subscription.setResource(subscriptionResourceDto.resource());
    subscription.setIncludeResourceData(true);
    // TODO: 3 days is the max ttl for interested to us services but it
    //  makes more sense to fetch this from GraphAPI if possible instead of hardcoding
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime expireAt = now.plusDays(ALLOWED_DAYS_BEFORE_EXP);
    subscription.setExpirationDateTime(expireAt);
    String clientStateSecret = SecureRandomGenerator.generateSecureRandomBase64String();
    subscription.setClientState(clientStateSecret);
    subscription.setEncryptionCertificate(encryptionKeyProvider.getPublicKeyBase64());
    subscription.setEncryptionCertificateId(encryptionKeyProvider.getEncryptionKeyId());

    return graphClient.subscriptions().post(subscription);
  }

  public Optional<GraphEventsSubscription> findByExternalId(String subscriptionId) {
    return graphSubscriptionRepository.findByExternalId(subscriptionId);
  }

  public void save(GraphEventsSubscription subscription) {
    graphSubscriptionRepository.save(subscription);
  }
}
