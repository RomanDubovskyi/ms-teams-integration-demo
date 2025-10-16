package tech.cusbo.msteams.demo.security.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomGenerator {

  private static final SecureRandom secureRandomGenerator = new SecureRandom();

  public static String generateSecureRandomBase64String() {
    byte[] buf = new byte[32];
    secureRandomGenerator.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}
