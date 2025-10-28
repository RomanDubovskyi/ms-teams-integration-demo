package tech.cusbo.msteams.demo.inboundevent.subscription;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.cusbo.msteams.demo.inboundevent.GraphEventsEncryptionService;
import tech.cusbo.msteams.demo.security.util.MsGraphMultiTenantKeyUtil;
import tech.cusbo.msteams.demo.security.util.SecureRandomGenerator;

@Slf4j
@Service
public class GraphSubscriptionService {

  private static final int ALLOWED_DAYS_BEFORE_EXP = 2;
  private final GraphSubscriptionRepository graphSubscriptionRepository;
  private final GraphEventsEncryptionService encryptionKeyProvider;
  private final GraphServiceClient oauthGraphClient;
  private final GraphServiceClient appGraphClient;

  @Value("${api.graph.inbound.webhook-url}")
  private String apiInboundEventsUrl;
  @Value("${api.graph.lifecycle.webhook-url}")
  private String apiInboundLifeCycleEventsUrl;
  @Value("${teams.app.tenant-id}")
  private String appTenantId;
  @Value("${teams.app.id}")
  private String appId;

  private final List<GraphSubscriptionResourceDto> userDefaultSubs = List.of(
      new GraphSubscriptionResourceDto("/me/chats", List.of("created", "updated", "deleted")),
      new GraphSubscriptionResourceDto("/me/chats/getAllMessages", List.of("created"))
  );

  private final List<GraphSubscriptionResourceDto> appDefaultSubs = List.of(
      new GraphSubscriptionResourceDto("/teams/getAllMessages", List.of("created", "updated")),
      new GraphSubscriptionResourceDto("/teams", List.of("created", "updated", "deleted")),
      new GraphSubscriptionResourceDto(
          "/teams/getAllMembers",
          List.of("created", "updated", "deleted")
      ),
      new GraphSubscriptionResourceDto(
          "/teams/getAllChannels",
          List.of("created", "updated", "deleted")
      )
  );

  public GraphSubscriptionService(
      GraphSubscriptionRepository graphSubscriptionRepository,
      GraphEventsEncryptionService encryptionKeyProvider,
      @Qualifier("oauthScopeServiceClient") GraphServiceClient oauthGraphClient,
      @Qualifier("appScopeServiceClient") GraphServiceClient appGraphClient
  ) {
    this.graphSubscriptionRepository = graphSubscriptionRepository;
    this.encryptionKeyProvider = encryptionKeyProvider;
    this.oauthGraphClient = oauthGraphClient;
    this.appGraphClient = appGraphClient;
  }

  public Optional<GraphEventsSubscription> findByExternalId(String subscriptionId) {
    return graphSubscriptionRepository.findByExternalId(subscriptionId);
  }

  public void save(GraphEventsSubscription subscription) {
    graphSubscriptionRepository.save(subscription);
  }

  public void ensureEventSubscriptionsByApp() {
    List<Subscription> currSubs = appGraphClient.subscriptions().get().getValue();
    Map<String, Subscription> currSubResourceMap = currSubs.stream()
        .collect(Collectors.toMap(Subscription::getResource, Function.identity(), (a, b) -> a));

    for (GraphSubscriptionResourceDto targetSub : appDefaultSubs) {
      if (currSubResourceMap.containsKey(targetSub.resource())) {
        continue;
      }

      try {
        var newSub = sendCreateSubscriptionRequest(appGraphClient, targetSub);
        log.info("created app sub for resource={}, id={}", targetSub.resource(), newSub.getId());

        GraphEventsSubscription appCurrentSub = new GraphEventsSubscription();
        appCurrentSub.setExternalId(newSub.getId());
        appCurrentSub.setSecret(newSub.getClientState());
        String appMultitenantId = MsGraphMultiTenantKeyUtil.getMultitenantId(appTenantId, appId);
        appCurrentSub.setMultitenantUserId(appMultitenantId);
        appCurrentSub.setSubscriptionState(SubscriptionState.active);
        appCurrentSub.setOwnerType(SubscriptionOwnerType.app);
        graphSubscriptionRepository.save(appCurrentSub);

      } catch (Exception e) {
        throw new RuntimeException(
            "Can't create new app subscription " + targetSub + " for app with id " + appId, e
        );
      }
    }
  }

  @Async
  public void ensureEventSubscriptionsByUserAsync(String tenantId, String msUserId) {
    List<Subscription> currSubs = oauthGraphClient.subscriptions().get().getValue();
    Map<String, Subscription> currSubResourceMap = currSubs.stream()
        .collect(Collectors.toMap(Subscription::getResource, Function.identity()));

    for (GraphSubscriptionResourceDto targetSub : userDefaultSubs) {
      if (currSubResourceMap.containsKey(targetSub.resource())) {
        continue;
      }

      try {
        var newSub = sendCreateSubscriptionRequest(oauthGraphClient, targetSub);
        log.info("created sub for user {}, sub id: {}", msUserId, newSub.getId());
        GraphEventsSubscription userCurrentSub = new GraphEventsSubscription();
        userCurrentSub.setExternalId(newSub.getId());
        userCurrentSub.setSecret(newSub.getClientState());
        String multitenantUserId = MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId);
        userCurrentSub.setMultitenantUserId(multitenantUserId);
        userCurrentSub.setSubscriptionState(SubscriptionState.active);
        userCurrentSub.setOwnerType(SubscriptionOwnerType.user);
        graphSubscriptionRepository.save(userCurrentSub);
      } catch (Exception e) {
        log.error("couldn't subscribe for ms resource {}", targetSub, e);
      }
    }
  }

  private Subscription sendCreateSubscriptionRequest(
      GraphServiceClient client,
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

    return client.subscriptions().post(subscription);
  }
}
