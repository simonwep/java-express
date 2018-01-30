package express.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.http.cookie.Cookie;
import express.multipart.MultiPartData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon Reinisch
 * @implNote Core modul of express, don't change anything.
 */
public class Request {

  private final URI URI;
  private final HttpExchange HTTP_EXCHANGE;
  private final InputStream BODY;
  private final Headers HEADER;
  private final String CONTENT_TYPE;

  private final HashMap<String, Cookie> COOKIES;
  private final HashMap<String, String> QUERYS;
  private HashMap<String, MultiPartData> formData;
  private HashMap<String, String> params;

  public Request(HttpExchange exchange) {
    this.HTTP_EXCHANGE = exchange;
    this.URI = exchange.getRequestURI();
    this.HEADER = exchange.getRequestHeaders();
    this.BODY = exchange.getRequestBody();
    this.CONTENT_TYPE = HEADER.get("Content-Type").get(0);

    this.formData = new HashMap<>();
    this.params = new HashMap<>();

    this.QUERYS = parseRawQuery(exchange.getRequestURI());
    this.COOKIES = parseCookies(exchange.getRequestHeaders());
  }

  /**
   * @return The request body as InputStream
   */
  public InputStream getBody() {
    return BODY;
  }

  /**
   * Pipe the body from this request to an OutputStream.
   *
   * @param os         The OutputStream.
   * @param bufferSize Buffersize, eg. 4096.
   * @throws IOException If an IO-Error occurs.
   */
  public void pipe(OutputStream os, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int n;
    while ((n = BODY.read(buffer)) != -1)
      os.write(buffer, 0, n);
    os.close();
  }

  /**
   * Get an request cookie by name.
   *
   * @param name The cookie name.
   * @return The cookie, null if there is no cookie with this name.
   */
  public Cookie getCookie(String name) {
    return COOKIES.get(name);
  }

  /**
   * Return all cookies from this request.
   *
   * @return All cookies.
   */
  public HashMap<String, Cookie> getCookies() {
    return COOKIES;
  }

  /**
   * @return The request user-agent.
   */
  public String getUserAgent() {
    return HEADER.get("User-agent").get(0);
  }

  /**
   * @return The request host.
   */
  public String getHost() {
    return HEADER.get("Host").get(0);
  }

  /**
   * @return The request content-type.
   */
  public String getContentType() {
    return CONTENT_TYPE;
  }

  /**
   * @return The request path.
   */
  public String getPath() {
    return this.URI.getPath();
  }

  /**
   * @return The entire request uri.
   */
  public URI getURI() {
    return this.URI;
  }

  /**
   * @return The request-method.
   */
  public String getMethod() {
    return HTTP_EXCHANGE.getRequestMethod();
  }

  public HashMap<String, MultiPartData> getFormData() {
    return this.formData;
  }

  public void setFormData(HashMap<String, MultiPartData> formData) {
    this.formData = formData;
  }

  /**
   * Returns an param from a dynamic url.
   * see README
   *
   * @param param The param.
   * @return The value, null if there is none.
   */
  public String getParam(String param) {
    return params.get(param);
  }

  /**
   * Returns the value from the url-query.
   *
   * @param name The name.
   * @return The value, null if there is none.
   */
  public String getQuery(String name) {
    return QUERYS.get(name);
  }

  /**
   * Set the params.
   *
   * @param params
   */
  public void setParams(HashMap<String, String> params) {
    this.params = params;
  }

  /**
   * Returns an header value.
   *
   * @param header The header name
   * @return An list with values.
   */
  public List<String> getHeader(String header) {
    return HEADER.get(header);
  }

  /**
   * Extract the cookies from the 'Cookie' header.
   *
   * @param headers The Headers
   * @return An hashmap with the cookie name as key and the complete cookie as value.
   */
  private static HashMap<String, Cookie> parseCookies(Headers headers) {
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
  private static HashMap<String, String> parseRawQuery(URI uri) {
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

}
