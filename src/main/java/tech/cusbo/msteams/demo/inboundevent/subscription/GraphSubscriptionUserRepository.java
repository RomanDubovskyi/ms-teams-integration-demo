package tech.cusbo.msteams.demo.inboundevent.subscription;

import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tech.cusbo.msteams.demo.security.util.MsGraphMultiTenantKeyUtil;

@Repository
public class GraphSubscriptionUserRepository {

  private static final String GRAPH_SUB_USER_KEY_PREFIX = "graphUser|subscriptionId:";

  private final RedisTemplate<String, String> redisTemplate;

  public GraphSubscriptionUserRepository(
      @Qualifier("tokenStoreRedisRepo") RedisTemplate<String, String> redisTemplate
  ) {
    this.redisTemplate = redisTemplate;
  }

  public void save(String subscriptionId, String tenantId, String msUserId, Duration ttl) {
    String value = MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId);
    redisTemplate.opsForValue().set(
        GRAPH_SUB_USER_KEY_PREFIX + subscriptionId,
        value,
        ttl
    );
  }

  public void delete(String subscriptionId) {
    redisTemplate.opsForValue().getAndDelete(GRAPH_SUB_USER_KEY_PREFIX + subscriptionId);
  }

  public Optional<String> get(String subscriptionId) {
    return Optional.ofNullable(
        redisTemplate.opsForValue().get(GRAPH_SUB_USER_KEY_PREFIX + subscriptionId)
    );
  }
}
