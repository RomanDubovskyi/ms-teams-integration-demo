package tech.cusbo.msteams.demo.inboundevent.subscription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tech.cusbo.msteams.demo.security.oauth.MultiTenantAuthenticationToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnLoginSubscribeToEventsHandler implements AuthenticationSuccessHandler {

  private final GraphSubscriptionService subscriptionService;

  @Override
  @SneakyThrows
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    String registrationId = oauthToken.getAuthorizedClientRegistrationId();
    if (!"azure".equalsIgnoreCase(registrationId)) {
      log.info("Skipping for non-Azure provider: {}", registrationId);
      request.getRequestDispatcher("/").forward(request, response);
      return;
    }

    OidcUser oidcUser = (OidcUser) oauthToken.getPrincipal();
    String msUserId = oidcUser.getClaimAsString("oid");
    String tenantId = oidcUser.getClaimAsString("tid");

    Authentication multiTenantAuth = new MultiTenantAuthenticationToken(
        oauthToken.getPrincipal(),
        oauthToken.getAuthorities(),
        oauthToken.getAuthorizedClientRegistrationId(),
        tenantId,
        msUserId
    );
    SecurityContextHolder.getContext().setAuthentication(multiTenantAuth);
    subscriptionService.ensureEventSubscriptionsForLoggedInUserAsync(
        tenantId, msUserId
    );
    request.getRequestDispatcher("/").forward(request, response);
  }
}
