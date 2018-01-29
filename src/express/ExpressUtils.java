package express;

import com.sun.net.httpserver.Headers;
import express.cookie.Cookie;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon Reinisch
 * <p>
 * A few utils which are used by express.
 */
public class ExpressUtils {

  /**
   * Extract the cookies from the 'Cookie' header.
   *
   * @param headers The Headers
   * @return An hashmap with the cookie name as key and the complete cookie as value.
   */
  public static HashMap<String, Cookie> parseCookies(Headers headers) {
    HashMap<String, Cookie> cookieList = new HashMap<>();
    List<String> headerCookies = headers.get("Cookie");

    if (headerCookies == null || headerCookies.size() == 0) {
      return cookieList;
    }

    String hcookies = headerCookies.get(0);

    String[] cookies = hcookies.split(";");
    for (String cookie : cookies) {
      String[] split = cookie.split("=");
      String name = split[0].trim();
      String value = split[1].trim();
      cookieList.put(name, new Cookie(name, value));
    }

    return cookieList;
  }

  /**
   * Method to extract the querys from an url.
   *
   * @param uri The uri.
   * @return An list with key-values which are encoded in UTF8.
   */
  public static HashMap<String, String> parseRawQuery(URI uri) {
    HashMap<String, String> querys = new HashMap<>();
    String rawQuery = uri.getRawQuery();

    if (rawQuery == null)
      return querys;

    Matcher mat = Pattern.compile("(.+?)=(.+?)(&|$)").matcher(rawQuery);
    while (mat.find()) {
      try {
        String key = URLDecoder.decode(mat.group(1), "UTF8");
        String val = URLDecoder.decode(mat.group(2), "UTF8");
        querys.put(key, val);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    return querys;
  }

  /**
   * Returns the MIME-Type of an filename.
   *
   * @param fileExtension The file extension.
   * @return The MIME-Type.
   */
  public static String getContentType(String fileExtension) {
    String ct = MIMETypes.get().get(fileExtension);

    if (ct == null)
      ct = "text/plain";

    return ct;
  }
}
