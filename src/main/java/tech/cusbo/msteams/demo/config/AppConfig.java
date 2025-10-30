package tech.cusbo.msteams.demo.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.graph.core.requests.GraphClientFactory;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.AzureIdentityAuthenticationProvider;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.OkHttpRequestAdapter;
import com.microsoft.kiota.serialization.JsonParseNodeFactory;
import com.microsoft.kiota.serialization.JsonSerializationWriterFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.client.RestTemplate;
import tech.cusbo.msteams.demo.security.oauth.SpringSecurityOauthTokenProvider;

@Configuration
public class AppConfig {

  @Value("${teams.app.id}")
  private String appId;
  @Value("${teams.app.secret}")
  private String appSecret;
  @Value("${teams.app.tenant-id}")
  private String tenantId;

  @Bean
  public ObjectMapper defaultObjectMapper() {
    // MS Graph Api events have mismatch with class enum field "ChangeType": created -> Created
    ObjectMapper mapper = JsonMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .build();
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  @Primary
  @Qualifier("oauthScopeServiceClient")
  public GraphServiceClient graphClient(OAuth2AuthorizedClientService clients) {
    var tokenProvider = new SpringSecurityOauthTokenProvider(clients);
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
  @Qualifier("appScopeServiceClient")
  public GraphServiceClient appScopeServiceClient() {
    ClientSecretCredential credential = new ClientSecretCredentialBuilder()
        .tenantId(tenantId)
        .clientId(appId)
        .clientSecret(appSecret)
        .build();

    String[] allowedHosts = new String[]{"graph.microsoft.com"};
    var authProvider = new AzureIdentityAuthenticationProvider(credential, allowedHosts);
    OkHttpRequestAdapter adapter = new OkHttpRequestAdapter(authProvider);
    adapter.setBaseUrl("https://graph.microsoft.com/v1.0");

    return new GraphServiceClient(adapter);
  }
}
