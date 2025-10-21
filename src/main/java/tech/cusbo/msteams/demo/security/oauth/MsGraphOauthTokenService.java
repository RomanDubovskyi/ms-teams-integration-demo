package tech.cusbo.msteams.demo.security.oauth;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
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
public class MsGraphOauthTokenService {

  private static final String REUSE_PREV_SCOPE_VAL
      = "https://graph.microsoft.com/.default offline_access";

  @Value("${spring.security.oauth2.client.registration.azure.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.azure.client-secret}")
  private String clientSecret;

  @Value("${spring.security.oauth2.client.provider.azure.token-uri}")
  private String tokenUri;

  private final OauthTokenRepository oauthTokenRepository;
  private final RestTemplate restTemplate;

  public OauthToken save(OauthToken token) {
    return oauthTokenRepository.save(token);
  }

  public Optional<OauthToken> findByMultitenantUserId(String multitenantUserId) {
    return oauthTokenRepository.findByMultitenantUserId(multitenantUserId);
  }

  public void delete(Long id) {
    oauthTokenRepository.deleteById(id);
  }

  @SneakyThrows
  public OauthToken refreshToken(OauthToken token) {
    log.info("start refreshing token with value {}", token);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("grant_type", "refresh_token");
    params.add("refresh_token", token.getRefreshToken());
    params.add("scope", REUSE_PREV_SCOPE_VAL);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
      Map<String, Object> body = response.getBody();
      log.info(" refresh request to GRAPH API successful, body {}", body);
      String newRefresh = (String) body.getOrDefault("refresh_token", token.getRefreshToken());
      token.setRefreshToken(newRefresh);
      token.setAccessToken((String) body.get("access_token"));
      token.setExpiresAt(Instant.now().plusSeconds((Integer) body.get("expires_in")));

      return oauthTokenRepository.save(token);
    } catch (Exception e) {
      throw new SecurityException("Token refresh failed", e);
    }
  }
}
