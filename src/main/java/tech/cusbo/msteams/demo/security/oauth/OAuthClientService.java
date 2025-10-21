package tech.cusbo.msteams.demo.security.oauth;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import tech.cusbo.msteams.demo.security.util.MsGraphMultiTenantKeyUtil;

@Service
@RequiredArgsConstructor
public class OAuthClientService implements OAuth2AuthorizedClientService {

  private final ClientRegistrationRepository clientRegistrationRepository;
  private final OauthTokenRepository tokenRepository;
  private final MsGraphOauthService oauthService;

  @Override
  @SneakyThrows
  public void saveAuthorizedClient(OAuth2AuthorizedClient client, Authentication principal) {
    if (!"azure".equalsIgnoreCase(client.getClientRegistration().getRegistrationId())) {
      throw new RuntimeException("IT'S NOT DESIGNED TO HANDLE ANYTHING ELSE THEN AZURE");
    }

    OAuth2AccessToken at = client.getAccessToken();
    OAuth2RefreshToken rt = client.getRefreshToken();

    OauthToken token = new OauthToken(
        at.getTokenValue(),
        rt != null ? rt.getTokenValue() : null,
        at.getExpiresAt(),
        OauthResource.MS_GRAPH
    );

    OAuth2User authUser = (OAuth2User) principal.getPrincipal();
    String tenantId = (String) authUser.getAttributes().get("tid");
    String msUserId = (String) authUser.getAttributes().get("oid");
    tokenRepository.save(MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId), token);
  }

  @Override
  public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId,
      String principalName) {
    if (!"azure".equalsIgnoreCase(registrationId)) {
      throw new RuntimeException("IT'S NOT DESIGNED TO HANDLE ANYTHING ELSE THEN AZURE, "
          + "RECEIVED " + registrationId);
    }
    Optional<OauthToken> opt = tokenRepository.get(principalName);
    if (opt.isEmpty()) {
      return null;
    }

    OauthToken token = opt.get();
    if (needsRefresh(token)) {
      OauthToken newToken = oauthService.refreshToken(token.refreshToken());
      tokenRepository.save(principalName, newToken);
      token = newToken;
    }

    OAuth2AccessToken accessToken = new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        token.accessToken(),
        Instant.now(),
        token.expiresAt()
    );

    OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(token.refreshToken(), Instant.now());
    var registration = clientRegistrationRepository.findByRegistrationId(registrationId);
    return (T) new OAuth2AuthorizedClient(registration, principalName, accessToken, refreshToken);
  }

  @Override
  public void removeAuthorizedClient(String registrationId, String principalName) {
    if (!"azure".equalsIgnoreCase(registrationId)) {
      throw new RuntimeException("IT'S NOT DESIGNED TO HANDLE ANYTHING ELSE THEN AZURE, "
          + "RECEIVED " + registrationId);
    }

    tokenRepository.delete(principalName);
  }

  private boolean needsRefresh(OauthToken token) {
    return Instant.now().isAfter(token.expiresAt().minusSeconds(600));
  }
}
