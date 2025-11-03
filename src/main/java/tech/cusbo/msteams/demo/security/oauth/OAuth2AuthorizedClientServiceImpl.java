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
public class OAuth2AuthorizedClientServiceImpl implements OAuth2AuthorizedClientService {

  private final ClientRegistrationRepository clientRegistrationRepository;
  private final MsGraphOauthTokenService oauthService;

  @Override
  @SneakyThrows
  public void saveAuthorizedClient(OAuth2AuthorizedClient client, Authentication principal) {
    if (!"azure".equalsIgnoreCase(client.getClientRegistration().getRegistrationId())) {
      throw new RuntimeException("IT'S NOT DESIGNED TO HANDLE ANYTHING ELSE THEN AZURE");
    }

    String multitenantId = parseMultitenantIdFromPrincipal(principal);

    // If user logins from another UI while having valid token we sync tokens and set new one to db
    OauthToken token = oauthService.findByMultitenantUserId(multitenantId)
        .orElseGet(OauthToken::new);
    OAuth2AccessToken at = client.getAccessToken();
    OAuth2RefreshToken rt = client.getRefreshToken();
    token.setAccessToken(at.getTokenValue());
    token.setRefreshToken(rt != null ? rt.getTokenValue() : null);
    token.setExpiresAt(at.getExpiresAt());
    token.setResource(OauthResource.MS_GRAPH);
    token.setMultitenantUserId(multitenantId);
    oauthService.save(token);
  }

  private String parseMultitenantIdFromPrincipal(Authentication principal) {
    // The format of plain string that we use to re-authorize
    if (principal.getPrincipal() instanceof String multiTenantId) {
      return multiTenantId;
    }

    // The format that is sent by azure on success login (azure oAuth2 flow)
    OAuth2User authUser = (OAuth2User) principal.getPrincipal();
    String tenantId = (String) authUser.getAttributes().get("tid");
    String msUserId = (String) authUser.getAttributes().get("oid");
    return MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, msUserId);
  }

  @Override
  public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId,
      String principalName) {
    if (!"azure".equalsIgnoreCase(registrationId)) {
      throw new RuntimeException("IT'S NOT DESIGNED TO HANDLE ANYTHING ELSE THEN AZURE, "
          + "RECEIVED " + registrationId);
    }
    Optional<OauthToken> opt = oauthService.findByMultitenantUserId(principalName);
    if (opt.isEmpty()) {
      return null;
    }

    OauthToken token = opt.get();
    OAuth2AccessToken accessToken = new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        token.getAccessToken(),
        token.getCreatedAt().isBefore(token.getExpiresAt())
            ? token.getCreatedAt()
            : token.getExpiresAt().minusSeconds(1),
        token.getExpiresAt()
    );

    OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(token.getRefreshToken(),
        Instant.now());
    var registration = clientRegistrationRepository.findByRegistrationId(registrationId);
    return (T) new OAuth2AuthorizedClient(registration, principalName, accessToken, refreshToken);
  }

  @Override
  public void removeAuthorizedClient(String registrationId, String principalName) {
    if (!"azure".equalsIgnoreCase(registrationId)) {
      throw new RuntimeException("IT'S NOT DESIGNED TO HANDLE ANYTHING ELSE THEN AZURE, "
          + "RECEIVED " + registrationId);
    }

    OauthToken token = oauthService.findByMultitenantUserId(principalName).orElseThrow();
    oauthService.delete(token.getId());
  }
}
