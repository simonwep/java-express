package express.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.ExpressUtils;
import express.cookie.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class Request{

  private final HttpExchange HTTP_EXCHANGE;
  private final URI URI;
  private final InputStream BODY;
  private final Headers HEADER;

  private final HashMap<String, Cookie> COOKIES;
  private final HashMap<String, String> QUERYS;
  private HashMap<String, String> params;

  public Request(HttpExchange exchange) {
    this.HTTP_EXCHANGE = exchange;
    this.URI = exchange.getRequestURI();
    this.HEADER = exchange.getRequestHeaders();
    this.BODY = exchange.getRequestBody();

    this.QUERYS = ExpressUtils.parseRawQuery(exchange.getRequestURI());
    this.COOKIES = ExpressUtils.parseCookies(exchange.getRequestHeaders());
  }

  public InputStream getBody() {
    return BODY;
  }

  public void pipe(OutputStream os, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int n;
    while ((n = BODY.read(buffer)) != -1)
      os.write(buffer, 0, n);
    os.close();
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
    return HEADER.get("Content-Type").get(0);
  }

  public URI getURI() {
    return this.URI;
  }

  public String getMethod() {
    return HTTP_EXCHANGE.getRequestMethod();
  }

  public String getParam(String key) {
    return params.get(key);
  }

  public String getQuery(String key) {
    return QUERYS.get(key);
  }

  public void setParams(HashMap<String, String> params) {
    this.params = params;
  }

  public List<String> getHeader(String header) {
    return HEADER.get(header);
  }

}
