package tech.cusbo.msteams.demo.security.util;

public class MsGraphRedisUtil {
  private static final String KEY_SPLITERATOR = "userId";
  private static final String KEY_PREFIX = "tenantId:";

  public static String getMultitenantId(String tenantId, String msUserId) {
    return KEY_PREFIX + tenantId + KEY_SPLITERATOR + msUserId;
  }

  public static String getUserId(String multitenantId) {
    String[] splitted = multitenantId.split(KEY_SPLITERATOR);
    return splitted[splitted.length - 1];
  }
}
