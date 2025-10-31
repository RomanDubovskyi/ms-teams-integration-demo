package tech.cusbo.msteams.demo.security.oauth;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsGraphOauthTokenService {

  private final OauthTokenRepository oauthTokenRepository;

  public OauthToken save(OauthToken token) {
    return oauthTokenRepository.save(token);
  }

  public Optional<OauthToken> findByMultitenantUserId(String multitenantUserId) {
    return oauthTokenRepository.findByMultitenantUserId(multitenantUserId);
  }

  public void delete(Long id) {
    oauthTokenRepository.deleteById(id);
  }
}
