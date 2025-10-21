package tech.cusbo.msteams.demo.inboundevent.subscription;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Table(name = "graph_events_subscriptions")
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
  @CreationTimestamp
  private Instant createdAt;
  @UpdateTimestamp
  private Instant updatedAt;
}
