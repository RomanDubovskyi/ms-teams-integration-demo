package tech.cusbo.msteams.demo.security;

import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@RequiredArgsConstructor
public class OauthTokenProvider implements AccessTokenProvider {

  private final OAuth2AuthorizedClientService clients;

  @Override
  @NonNull
  public String getAuthorizationToken(
      @NonNull final URI uri,
      @Nullable final Map<String, Object> additionalAuthenticationContext
  ) {

    var auth = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new IllegalStateException("No authentication in context");
    }

    OAuth2AuthorizedClient client = clients.loadAuthorizedClient(
        auth.getAuthorizedClientRegistrationId(), auth.getName()
    );
    if (client == null || client.getAccessToken() == null) {
      throw new IllegalStateException("No authorized client or access token");
    }

    return client.getAccessToken().getTokenValue();
  }

  @NonNull
  @Override
  public AllowedHostsValidator getAllowedHostsValidator() {
    AllowedHostsValidator allowedHostsValidator = new AllowedHostsValidator();
    allowedHostsValidator.setAllowedHosts(Collections.singleton("graph.microsoft.com"));
    return allowedHostsValidator;
  }
}