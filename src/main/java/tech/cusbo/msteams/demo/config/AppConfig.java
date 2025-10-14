package tech.cusbo.msteams.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.graph.core.requests.GraphClientFactory;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.OkHttpRequestAdapter;
import com.microsoft.kiota.serialization.JsonParseNodeFactory;
import com.microsoft.kiota.serialization.JsonSerializationWriterFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.RestTemplate;
import tech.cusbo.msteams.demo.security.OauthTokenProvider;

@Configuration
public class AppConfig {

  @Bean
  public ObjectMapper defaultObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter();
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public GraphServiceClient graphClient(OAuth2AuthorizedClientService clients) {
    var tokenProvider = new OauthTokenProvider(clients);
    var authProvider = new BaseBearerTokenAuthenticationProvider(tokenProvider);

    var adapter = new OkHttpRequestAdapter(
        authProvider,
        new JsonParseNodeFactory(),
        new JsonSerializationWriterFactory(),
        GraphClientFactory.create().build()
    );

    return new GraphServiceClient(adapter);
  }

  @Bean
  @Qualifier("stringTokenStore")
  public RedisTemplate<String, String> tokenStoreRedisRepo(RedisConnectionFactory factory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    template.setConnectionFactory(factory);
    template.setKeySerializer(stringRedisSerializer);
    template.setValueSerializer(stringRedisSerializer);
    return template;
  }
}
