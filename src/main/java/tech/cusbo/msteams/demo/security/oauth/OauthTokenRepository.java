package tech.cusbo.msteams.demo.security.oauth;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tech.cusbo.msteams.demo.security.util.MsGraphMultiTenantKeyUtil;

@Repository
public class OauthTokenRepository {

  private static final int DEFAULT_OAUTH_TTL_DAYS = 90;
  private static final String OAUTH_KEY_PREFIX = "oauth|";
  private final RedisTemplate<String, OauthToken> redisTemplate;

  public OauthTokenRepository(
      @Qualifier("oauthStore") RedisTemplate<String, OauthToken> tokenStoreRedisRepo
  ) {
    this.redisTemplate = tokenStoreRedisRepo;
  }

  public void save(String multiTenantUserId, OauthToken token) {
    Duration ttl = Duration.between(
        Instant.now(),
        Instant.now().plus(DEFAULT_OAUTH_TTL_DAYS, ChronoUnit.DAYS)
    );
    redisTemplate.opsForValue().set(OAUTH_KEY_PREFIX + multiTenantUserId, token, ttl);
  }

  public Optional<OauthToken> get(String multitenantUserId) {
    String key = OAUTH_KEY_PREFIX + multitenantUserId;
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }


  public void delete(String multitenantUserId) {
    redisTemplate.delete(OAUTH_KEY_PREFIX + multitenantUserId);
  }
}
