package express.http;

import java.time.Instant;
import java.util.HashMap;

/**
 * @author Simon Reinisch
 * An CookieFactory to parse an string which represents an cookie.
 */
public final class CookieFactory {

  private CookieFactory() {}

  /**
   * Parse an cookie string.
   *
   * @param cookieString The cookie as string.
   * @return The parsed cookie as object.
   */
  public static Cookie fromString(String cookieString) {
    Cookie cookie = null;
    char[] chars = cookieString.toCharArray();

    StringBuilder key = new StringBuilder();
    StringBuilder val = new StringBuilder();
    boolean swap = false;

    for (char c : chars) {

      if (c == '=') {
        swap = true;
      } else if (c == ';') {

        cookie = addField(cookie, key.toString(), val.toString());

        key.setLength(0);
        val.setLength(0);
        swap = false;
      } else if (swap) {
        val.append(c);
      } else {
        key.append(c);
      }
    }

    cookie = addField(cookie, key.toString(), val.toString());
    return cookie;
  }

  /**
   * Parse an list of strings which represents an cookie.
   *
   * @param stringCookies The list with string cookies.
   * @return A list with the parsed cookies.
   */
  public static HashMap<String, Cookie> fromStrings(String[] stringCookies) {
    HashMap<String, Cookie> cookies = new HashMap<>();

    if (stringCookies == null || stringCookies.length == 0)
      return cookies;

    for (String s : stringCookies) {
      Cookie cookie = fromString(s);
      if (cookie != null)
        cookies.put(cookie.getName(), cookie);
    }

    return cookies;
  }

  private static Cookie addField(Cookie cookie, String key, String value) {
    key = key.trim();

    if (cookie == null) {

      cookie = new Cookie(key, value);
    } else if (key.equalsIgnoreCase("Path")) {

      cookie.setPath(value);
    } else if (key.equalsIgnoreCase("Domain")) {

      cookie.setDomain(value);
    } else if (key.equalsIgnoreCase("Expire")) {

      cookie.setExpire(Instant.parse(value));
    } else if (key.equalsIgnoreCase("Max-Age")) {

      if (isInteger(value)) {
        cookie.setMaxAge(Long.parseLong(value));
      }

    } else if (key.equalsIgnoreCase("SameSite")) {

      SameSite sameSite = value.equalsIgnoreCase("LAX") ? SameSite.LAX : SameSite.STRICT;
      cookie.setSameSite(sameSite);

    } else if (key.equalsIgnoreCase("Secure")) {

      cookie.setSecure(true);
    } else if (key.equalsIgnoreCase("HttpOnly")) {

      cookie.setHttpOnly(true);
    }


    return cookie;
  }

  private static boolean isInteger(String str) {
    char[] chars = str.toCharArray();

    for (char c : chars)
      if (c < 48 || c > 57) return false;

    return true;
  }
}
