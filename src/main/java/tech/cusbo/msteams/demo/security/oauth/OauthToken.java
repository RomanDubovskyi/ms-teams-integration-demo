package tech.cusbo.msteams.demo.security.oauth;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "oauth_tokens")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OauthToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
