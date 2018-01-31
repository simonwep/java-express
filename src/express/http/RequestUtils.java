package express.http;

import com.sun.net.httpserver.Headers;
import express.http.cookie.Cookie;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
   * Write all data from an InputStream in an String
   *
   * @param is The source inputstream
   * @return The data as string
   */
  public static String streamToString(InputStream is) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuffer buffer = new StringBuffer();
      String line;

      while ((line = br.readLine()) != null)
        buffer.append(line);

      return buffer.toString();
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }
    return null;
  }

}
