package tech.cusbo.msteams.demo.inboundevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotificationEncryptedContent;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class GraphEventsEncryptionService {

  private final ObjectMapper objectMapper;
  private PrivateKey cachedPrivateKey;

  @Getter
  @Value("${api.graph.subscription.encryption.key.id}")
  private String encryptionKeyId;

  @Value("${api.graph.subscription.encryption.key.pass}")
  private String keystorePassword;

  @Getter
  @Value("${api.graph.subscription.public.encryption.key}")
  private String publicKeyBase64;

  @Getter
  @Value("${api.graph.subscription.private.encryption.key}")
  private String privateKeyBase64;

  @PostConstruct
  public void init() {
    Security.addProvider(new BouncyCastleProvider());
    try {
      byte[] keystoreBytes = Base64.getDecoder().decode(privateKeyBase64);
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(new ByteArrayInputStream(keystoreBytes), keystorePassword.toCharArray());
      cachedPrivateKey = (PrivateKey) ks.getKey(
          "graph-sub-key",
          keystorePassword.toCharArray()
       );
    } catch (Exception e) {
      throw new IllegalStateException("Can't init private key for ms graph events encryption", e);
    }
  }

  public PrivateKey getPrivateKey() {
    if (cachedPrivateKey == null) {
      throw new IllegalStateException("Private key not initialized");
    }
    return cachedPrivateKey;
  }

  @SneakyThrows
  public byte[] decryptNotificationContent(ChangeNotificationEncryptedContent encryptedContent) {
    String encryptedDataBase64 = encryptedContent.getData();
    String encryptedSymmetricKeyBase64 = encryptedContent.getDataKey();
    String signatureBase64 = encryptedContent.getDataSignature();
    String currentCertificateId = encryptedContent.getEncryptionCertificateId();
    if (!Objects.equals(currentCertificateId, encryptionKeyId)) {
      throw new SecurityException("Invalid encryption certificate id");
    }

    byte[] encryptedSymmetricKey = Base64.getDecoder().decode(encryptedSymmetricKeyBase64);
    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
    rsaCipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
    byte[] symmetricKey = rsaCipher.doFinal(encryptedSymmetricKey);

    byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
    byte[] expectedSignature = Base64.getDecoder().decode(signatureBase64);

    Mac hmac = Mac.getInstance("HmacSHA256");
    hmac.init(new SecretKeySpec(symmetricKey, "HmacSHA256"));
    byte[] computedSignature = hmac.doFinal(encryptedData);

    if (!MessageDigest.isEqual(expectedSignature, computedSignature)) {
      throw new SecurityException("Signature verification failed - data may be tampered");
    }

    byte[] iv = Arrays.copyOf(symmetricKey, 16);
    Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec aesKey = new SecretKeySpec(symmetricKey, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    aesCipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
    return aesCipher.doFinal(encryptedData);
  }
}
