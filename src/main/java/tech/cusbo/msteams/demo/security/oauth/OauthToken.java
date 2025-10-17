package tech.cusbo.msteams.demo.security.oauth;

import java.time.Instant;

public record OauthToken(
    String accessToken,
    String refreshToken,
    Instant expiresAt,
    OauthResource service
) {

}
