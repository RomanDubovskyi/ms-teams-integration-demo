package tech.cusbo.msteams.demo.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tech.cusbo.msteams.demo.inboundevent.subscription.GraphApiSubscriptionService;

@Component
@RequiredArgsConstructor
public class OnLoginSubscribeToEventsHandler implements AuthenticationSuccessHandler {
  private final GraphApiSubscriptionService subscriptionService;

  @Override
  @SneakyThrows
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    subscriptionService.ensureEventSubscriptionsForLoggedInUser();
    response.sendRedirect("/");
  }
}