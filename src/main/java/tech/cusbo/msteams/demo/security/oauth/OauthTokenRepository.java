package tech.cusbo.msteams.demo.security.oauth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OauthTokenRepository extends JpaRepository<OauthToken, Long> {

  Optional<OauthToken> findByMultitenantUserId(String multitenantUserId);
}
