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

  public void save(String tenantId, String msUserId, OauthToken token) {
    String key = OAUTH_KEY_PREFIX + MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId);
    Duration ttl = Duration.between(
        Instant.now(),
        Instant.now().plus(DEFAULT_OAUTH_TTL_DAYS, ChronoUnit.DAYS)
    );
    redisTemplate.opsForValue().set(key, token, ttl);
  }

  public void save(String multiTenantUserId, OauthToken token) {
    Duration ttl = Duration.between(
        Instant.now(),
        Instant.now().plus(DEFAULT_OAUTH_TTL_DAYS, ChronoUnit.DAYS)
    );
    redisTemplate.opsForValue().set(multiTenantUserId, token, ttl);
  }

  public Optional<OauthToken> get(String tenantId, String msUserId) {
    String key = OAUTH_KEY_PREFIX + MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId);
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  public Optional<OauthToken> get(String multitenantUserId) {
    String key = OAUTH_KEY_PREFIX + multitenantUserId;
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }


  public void delete(String multitenantUserId) {
    redisTemplate.delete(multitenantUserId);
  }
}
