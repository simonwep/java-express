package express.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.cookie.Cookie;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Request {

  private final HttpExchange HTTP_EXCHANGE;
  private final InputStream BODY;
  private final Headers HEADER;

  private final ArrayList<Cookie> COOKIES;

  private HashMap<String, String> params;

  public Request(HttpExchange exchange) {
    this.HTTP_EXCHANGE = exchange;
    this.HEADER = exchange.getRequestHeaders();
    this.BODY = exchange.getRequestBody();
    this.COOKIES = parseCookies(exchange.getRequestHeaders());
  }

  public ArrayList<Cookie> getCookies() {
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

  private ArrayList<Cookie> parseCookies(Headers headers) {
    ArrayList<Cookie> cookieList = new ArrayList<>();
    String hcookies = headers.get("Cookie").get(0);

    if (hcookies == null || hcookies.length() == 0) {
      return cookieList;
    }

    String[] cookies = hcookies.split(";");
    for (String cookie : cookies) {
      String[] split = cookie.split("=");
      cookieList.add(new Cookie(split[0].trim(), split[1].trim()));
    }

    return cookieList;
  }

}
