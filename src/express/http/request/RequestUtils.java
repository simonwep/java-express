package express.http.request;

import com.sun.net.httpserver.Headers;
import express.http.Cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

public class RequestUtils {

  /**
   * Extract the cookies from the 'Cookie' header.
   *
   * @param headers The Headers
   * @return An hashmap with the cookie name as key and the complete cookie as value.
   */
  protected static HashMap<String, Cookie> parseCookies(Headers headers) {
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
   * @param rawQuery The raw query
   * @return An list with key-values which are encoded in UTF8.
   */
  protected static HashMap<String, String> parseRawQuery(String rawQuery) {
    HashMap<String, String> querys = new HashMap<>();

    if (rawQuery == null)
      return querys;

    StringBuilder key = new StringBuilder();
    StringBuilder val = new StringBuilder();
    char[] chars = rawQuery.toCharArray();
    boolean keyac = false;
    char c = '=';

    for (int i = 0; i < chars.length; i++) {
      c = chars[i];

      if (c == '=')
        keyac = true;
      else if (c == '&') {

        try {
          querys.put(URLDecoder.decode(key.toString(), "UTF-8"), URLDecoder.decode(val.toString(), "UTF8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          // TODO: Handle error
        }

        key.setLength(0);
        val.setLength(0);
        keyac = false;
      } else if (keyac)
        val.append(c);
      else
        key.append(c);
    }

    if (c != '=' && c != '&')
      querys.put(key.toString(), val.toString());

    return querys;
  }

}
