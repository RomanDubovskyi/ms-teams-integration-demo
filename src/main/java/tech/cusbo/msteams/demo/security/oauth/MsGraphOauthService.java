package tech.cusbo.msteams.demo.security.oauth;

import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsGraphOauthService {

  private static final String REUSE_PREV_SCOPE_VAL
      = "https://graph.microsoft.com/.default offline_access";

  @Value("${spring.security.oauth2.client.registration.azure.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.azure.client-secret}")
  private String clientSecret;

  @Value("${spring.security.oauth2.client.provider.azure.token-uri}")
  private String tokenUri;

  private final RestTemplate restTemplate;

  @SneakyThrows
  public OauthToken refreshToken(String refreshToken) {
    log.warn("start refreshing token with value {}", refreshToken);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("grant_type", "refresh_token");
    params.add("refresh_token", refreshToken);
    params.add("scope", REUSE_PREV_SCOPE_VAL);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
      Map<String, Object> body = response.getBody();
      String newAccessToken = (String) body.get("access_token");
      String newRefreshToken = (String) body.getOrDefault("refresh_token", refreshToken);
      Integer expiresIn = (Integer) body.get("expires_in");
      Instant expiresAt = Instant.now().plusSeconds(expiresIn);

      log.info(" refresh request to GRAPH API successful, body {}", body);
      return new OauthToken(
          newAccessToken,
          newRefreshToken,
          expiresAt,
          OauthResource.MS_GRAPH
      );

    } catch (Exception e) {
      throw new SecurityException("Token refresh failed", e);
    }
  }
}