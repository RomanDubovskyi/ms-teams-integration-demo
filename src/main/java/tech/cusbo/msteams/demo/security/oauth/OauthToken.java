package tech.cusbo.msteams.demo.security.oauth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Table(name = "oauth_tokens")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OauthToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;
  @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
  private String accessToken;
  @Column(name = "refresh_token", columnDefinition = "TEXT")
  private String refreshToken;
  private Instant expiresAt;
  @Enumerated(EnumType.STRING)
  private OauthResource resource;
  // In production we're going to reference user entity here
  private String multitenantUserId;
  @CreationTimestamp
  private Instant createdAt;
  @UpdateTimestamp
  private Instant updatedAt;

  public boolean needsRefresh() {
    return Instant.now().isAfter(this.expiresAt.minusSeconds(600));
  }
}
