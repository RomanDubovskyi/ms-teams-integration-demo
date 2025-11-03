package tech.cusbo.msteams.demo.inboundevent.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Table(
    name = "graph_events_subscriptions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_graph_sub_external_id",
        columnNames = "externalId"
    )
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GraphEventsSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String externalId;
  @Column(columnDefinition = "TEXT")
  private String secret;
  // In production we're going to reference user entity here
  private String multitenantUserId;
  @Enumerated(value = EnumType.STRING)
  private SubscriptionState subscriptionState;
  @Enumerated(value = EnumType.STRING)
  private SubscriptionOwnerType ownerType;
  @CreationTimestamp
  private Instant createdAt;
  @UpdateTimestamp
  private Instant updatedAt;
}
