package tech.cusbo.msteams.demo.inboundevent.subscription;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphSubscriptionRepository extends JpaRepository<GraphEventsSubscription, Long> {

  Optional<GraphEventsSubscription> findByMultitenantUserIdAndSubscriptionState(
      String multitenantUserId,
      SubscriptionState subscriptionState
  );

  Optional<GraphEventsSubscription> findByExternalId(String externalId);
}
