package express.utils;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

  private final static SimpleDateFormat GMT_FORMAT;

  static {

    // Init some values
    GMT_FORMAT = new SimpleDateFormat("yyyy MMM dd HH:mm:ss zzz");
    GMT_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Write all data from an InputStream in an String
   *
   * @param is The source inputstream
   * @return The data as string
   */
  public static String streamToString(InputStream is) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = br.readLine()) != null)
        sb.append(line);

      return sb.toString();
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the MIME-Type of an file.
   *
   * @param file The file.
   * @return The MIME-Type.
   */
  public static MediaType getContentType(File file) {
    String ex = getExtension(file);
    MediaType contentType = MediaType.getForExtension(ex);

    if (contentType == null)
      return MediaType._bin;

    return contentType;
  }

  /**
   * Generates an random token with SecureRandom
   *
   * @param byteLength The token length
   * @param radix      The base
   * @return An token with the base of radix
   */
  public static String randomToken(int byteLength, int radix) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] token = new byte[byteLength];
    secureRandom.nextBytes(token);
    return new BigInteger(1, token).toString(radix); //hex encoding
  }

  /**
   * @return Your ip.
   */
  public static String getYourIp() throws UnknownHostException {
    return Inet4Address.getLocalHost().getHostAddress();
  }

  /**
   * Extract the extension from the file.
   *
   * @param file The file.
   * @return The extension.
   */
  public static String getExtension(@NotNull File file) {
    String path = file.getAbsolutePath();
    int indx = path.lastIndexOf('.') + 1;
    if (indx == 0)
      return null;

    return path.substring(indx);
  }

  /**
   * @param date The date to format.
   * @return The current GMT-Date.
   */
  public static String getGMTDate(Date date) {
    return GMT_FORMAT.format(date);
  }
}
