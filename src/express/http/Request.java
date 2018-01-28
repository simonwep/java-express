package express.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.cookie.Cookie;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class Request {

  private final HttpExchange HTTP_EXCHANGE;
  private final InputStream BODY;
  private final Headers HEADER;

  private final HashMap<String, Cookie> COOKIES;

  private HashMap<String, String> params;

  public Request(HttpExchange exchange) {
    this.HTTP_EXCHANGE = exchange;
    this.HEADER = exchange.getRequestHeaders();
    this.BODY = exchange.getRequestBody();
    this.COOKIES = parseCookies(exchange.getRequestHeaders());
  }

  public Cookie getCookie(String name) {
    return COOKIES.get(name);
  }

  public HashMap<String, Cookie> getCookies() {
    return COOKIES;
  }

  public String getUserAgent() {
    return HEADER.get("User-agent").get(0);
  }

  public String getHost() {
    return HEADER.get("Host").get(0);
  }

  public String getContentType() {
    return HEADER.get("Accept").get(0);
  }

  public URI getRequestURI() {
    return HTTP_EXCHANGE.getRequestURI();
  }

  public String getRequestMethod() {
    return HTTP_EXCHANGE.getRequestMethod();
  }

  public String getParam(String key) {
    return params.get(key);
  }

  public void setParams(HashMap<String, String> params) {
    this.params = params;
  }

  public List<String> getHeader(String header) {
    return HEADER.get(header);
  }

  private HashMap<String, Cookie> parseCookies(Headers headers) {
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

}
