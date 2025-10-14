package tech.cusbo.msteams.demo.inboundevent.subscription;

import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GraphSubscriptionSecretRepo {

  private static final String GRAPH_SUB_SECRET_KEY_PREFIX = "graph-sub-secret:";
  private static final Duration GRAPH_SUB_SECRET_TTL = Duration.ofDays(2);

  private final RedisTemplate<String, String> tokenStoreRedisRepo;

  public GraphSubscriptionSecretRepo(
      @Qualifier("tokenStoreRedisRepo") RedisTemplate<String, String> tokenStoreRedisRepo
  ) {
    this.tokenStoreRedisRepo = tokenStoreRedisRepo;
  }

  public void save(String subId, String secret) {
    tokenStoreRedisRepo.opsForValue().set(
        GRAPH_SUB_SECRET_KEY_PREFIX + subId,
        secret,
        GRAPH_SUB_SECRET_TTL
    );
  }

  public void delete(String subId) {
    tokenStoreRedisRepo.opsForValue().getAndDelete(GRAPH_SUB_SECRET_KEY_PREFIX + subId);
  }

  public void extendTtlForDefaultPeriod(String subId) {
    tokenStoreRedisRepo.opsForValue().getAndExpire(
        GRAPH_SUB_SECRET_KEY_PREFIX + subId,
        GRAPH_SUB_SECRET_TTL
    );
  }

  public Optional<String> get(String subId) {
    return Optional.ofNullable(
        tokenStoreRedisRepo.opsForValue().get(GRAPH_SUB_SECRET_KEY_PREFIX + subId)
    );
  }
}
