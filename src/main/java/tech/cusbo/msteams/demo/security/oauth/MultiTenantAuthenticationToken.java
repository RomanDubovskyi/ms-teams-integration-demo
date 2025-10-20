package tech.cusbo.msteams.demo.security.oauth;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import tech.cusbo.msteams.demo.security.util.MsGraphMultiTenantKeyUtil;

public class MultiTenantAuthenticationToken extends OAuth2AuthenticationToken {

  private final String multitenantUserId;

  public MultiTenantAuthenticationToken(
      OAuth2User principal,
      Collection<? extends GrantedAuthority> authorities,
      String registrationId,
      String tenantId,
      String userId
  ) {
    super(principal, authorities, registrationId);
    this.multitenantUserId = MsGraphMultiTenantKeyUtil.getMultitenantId(tenantId, userId);
  }

  @Override
  public String getName() {
    return multitenantUserId;
  }
}