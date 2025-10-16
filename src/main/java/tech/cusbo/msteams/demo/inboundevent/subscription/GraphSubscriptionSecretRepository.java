package tech.cusbo.msteams.demo.inboundevent.subscription;

import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GraphSubscriptionSecretRepository {

  private static final String GRAPH_SUB_SECRET_KEY_PREFIX
      = "graphSubscriptionSecret|subscriptionId:";

  private final RedisTemplate<String, String> tokenStoreRedisRepo;

  public GraphSubscriptionSecretRepository(
      @Qualifier("tokenStoreRedisRepo") RedisTemplate<String, String> tokenStoreRedisRepo
  ) {
    this.tokenStoreRedisRepo = tokenStoreRedisRepo;
  }

  public void save(String subId, String secret, Duration ttl) {
    tokenStoreRedisRepo.opsForValue().set(
        GRAPH_SUB_SECRET_KEY_PREFIX + subId,
        secret,
        ttl
    );
  }

  public void delete(String subId) {
    tokenStoreRedisRepo.opsForValue().getAndDelete(GRAPH_SUB_SECRET_KEY_PREFIX + subId);
  }

  public Optional<String> get(String subId) {
    return Optional.ofNullable(
        tokenStoreRedisRepo.opsForValue().get(GRAPH_SUB_SECRET_KEY_PREFIX + subId)
    );
  }
}
