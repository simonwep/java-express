package express.http.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import express.Express;
import express.filter.Filter;
import express.http.Cookie;
import express.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author Simon Reinisch
 * <p>
 * Class encapsulating HTTP request data
 */
public class Request {

  private final Express EXPRESS;

  private final String PROTOCOL;                      // Request protocol
  private final URI URI;                              // Request URI
  private final InputStream BODY;                     // Request body
  private final Headers HEADERS;                      // Request Headers
  private final boolean SECURE;
  private final String CONTENT_TYPE;                  // Request content-type
  private final long CONTENT_LENGTH;                  // Request content-length
  private final String METHOD;                        // Request method
  private final Authorization AUTH;                   // Authorization header parsed
  private final InetSocketAddress INET;               // Client socket address

  private final HashMap<String, Object> MIDDLEWARE;   // Middleware Data
  private final HashMap<String, Cookie> COOKIES;      // Request cookies
  private final HashMap<String, String> QUERIES;      // URL Query parameters
  private final HashMap<String, String> FORM_QUERIES; // Form query parameters (application/x-www-form-urlencoded)

  private HashMap<String, String> params;             // URL Params, would be added in ExpressFilterImpl
  private String context;                             // Context which matched

  {
    this.MIDDLEWARE = new HashMap<>();
    this.params = new HashMap<>();
  }

  public Request(HttpExchange exchange, Express express) {
    this.EXPRESS = express;
    this.METHOD = exchange.getRequestMethod();
    this.URI = exchange.getRequestURI();
    this.HEADERS = exchange.getRequestHeaders();
    this.BODY = exchange.getRequestBody();
    this.INET = exchange.getRemoteAddress();

    this.PROTOCOL = exchange.getProtocol();
    this.SECURE = exchange instanceof HttpsExchange; // Can be suckered?

    // Parse content length
    String contentLength = HEADERS.get("Content-Length") != null ? HEADERS.get("Content-Length").get(0) : null;
    this.CONTENT_LENGTH = contentLength != null ? Long.parseLong(contentLength) : -1;

    // Check if the request contains an body-content
    this.CONTENT_TYPE = HEADERS.get("Content-Type") == null ? "" : HEADERS.get("Content-Type").get(0);

    // Check if the request has an Authorization header
    this.AUTH = HEADERS.get("Authorization") == null ? null : new Authorization(HEADERS.get("Authorization").get(0));

    // Check if the request contains x-www-form-urlencoded form data
    this.FORM_QUERIES = CONTENT_TYPE.startsWith("application/x-www-form-urlencoded")
        ? RequestUtils.parseRawQuery(Utils.streamToString(BODY))
        : new HashMap<>();

    // Parse query and cookies, both returns not null if there is nothing
    this.QUERIES = RequestUtils.parseRawQuery(exchange.getRequestURI().getRawQuery());
    this.COOKIES = RequestUtils.parseCookies(HEADERS);
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
   * @param bufferSize Buffer-size, eg. 4096.
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
   * Pipe the body from this request to an file.
   * If the file not exists, it will be created.
   *
   * @param f          The target file
   * @param bufferSize Buffer-size, eg. 4096.
   * @throws IOException If an IO-Error occurs.
   */
  public void pipe(Path f, int bufferSize) throws IOException {
    if (Files.exists(f))
      return;

    Files.createFile(f);
    pipe(Files.newOutputStream(f), bufferSize);
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
   * Add an the content from an middleware
   *
   * @param middleware     The middleware
   * @param middlewareData The data from the middleware
   */
  public void addMiddlewareContent(Filter middleware, Object middlewareData) {
    MIDDLEWARE.put(middleware.getName(), middlewareData);
  }

  /**
   * Get the data from a specific middleware by name (Also the reason
   * why the interface ExpressFilter implements a getName())
   *
   * @param name The middleware name
   * @return The middleware object
   */
  public Object getMiddlewareContent(String name) {
    return MIDDLEWARE.get(name);
  }

  /**
   * @return The request user-agent.
   */
  public String getUserAgent() {
    return HEADERS.get("User-agent").get(0);
  }

  /**
   * @return The request host.
   */
  public String getHost() {
    return HEADERS.get("Host").get(0);
  }

  /**
   * Returns the InetAddress from the client.
   *
   * @return The InetAddress.
   */
  public InetAddress getAddress() {
    return INET.getAddress();
  }

  /**
   * Returns the IP-Address from the client.
   *
   * @return The IP-Address.
   */
  public String getIp() {
    return INET.getAddress().getHostAddress();
  }

  /**
   * @return The request content-type.
   */
  public String getContentType() {
    return CONTENT_TYPE;
  }

  /**
   * Returns the to long parsed content-length.
   *
   * @return The content-length, -1 if the header was invalid.
   */
  public long getContentLength() {
    return CONTENT_LENGTH;
  }

  /**
   * @return The request path.
   */
  public String getPath() {
    return this.URI.getPath();
  }

  /**
   * @return The original request URI.
   */
  public URI getURI() {
    return this.URI;
  }

  /**
   * @return The request-method.
   */
  public String getMethod() {
    return this.METHOD;
  }

  /**
   * Checks if the connection is 'fresh'
   * It is true if the cache-control request header doesn't have a no-cache directive, the if-modified-since request header is specified
   * and last-modified request header is equal to or earlier than the modified response header or if-none-match request header is *.
   *
   * @return True if the connection is fresh, false otherwise.
   */
  public boolean isFresh() {

    if (HEADERS.containsKey("cache-control") && HEADERS.get("cache-control").get(0) != null && HEADERS.get("cache-control").get(0).equals("no-cache"))
      return true;

    if (HEADERS.containsKey("if-none-match") && HEADERS.get("if-none-match").get(0) != null && HEADERS.get("if-none-match").get(0).equals("*"))
      return true;

    if (HEADERS.containsKey("if-modified-since") && HEADERS.containsKey("last-modified") && HEADERS.containsKey("modified")) {
      List<String> lmlist = HEADERS.get("last-modified");
      List<String> mlist = HEADERS.get("modified");

      // Check lists
      if (lmlist.isEmpty() || mlist.isEmpty())
        return false;

      String lm = lmlist.get(0);
      String m = mlist.get(0);

      // Check header
      if (lm != null && m != null) {
        try {

          // Try to convert it
          Instant lmi = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(lm));
          Instant mi = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(m));

          if (lmi.isBefore(mi) || lmi.equals(mi)) {
            return true;
          }

        } catch (Exception ignored) {
        }
      }
    }

    return false;
  }

  /**
   * Indicates whether the request is "stale" and is the opposite of req.fresh
   *
   * @return The opposite of req.fresh;
   */
  public boolean isStale() {
    return !isFresh();
  }

  /**
   * Returns whenever the connection is over HTTPS.
   *
   * @return True when the connection is over HTTPS, false otherwise.
   */
  public boolean isSecure() {
    return SECURE;
  }

  /**
   * Returns true if the 'X-Requested-With' header field is 'XMLHttpRequest'.
   * Indicating that the request was made by a client library such as jQuery.
   *
   * @return True if the 'X-Requested-With' header field is 'XMLHttpRequest'.
   */
  public boolean isXHR() {
    return HEADERS.containsKey("X-Requested-With") && !HEADERS.get("X-Requested-With").isEmpty() && HEADERS.get("X-Requested-With").get(0).equals("XMLHttpRequest");
  }

  /**
   * The connection protocol HTTP/1.0, HTTP/1.1 etc.
   *
   * @return The connection protocol.
   */
  public String getProtocol() {
    return PROTOCOL;
  }

  /**
   * If there is an Authorization header, it was parsed and saved
   * in a Authorization Object.
   *
   * @return The Authorization object or null if there was no Authorization header present.
   */
  public Authorization getAuthorization() {
    return AUTH;
  }

  /**
   * @return True if there was an Authorization header and the Authorization object was successfully created.
   */
  public boolean hasAuthorization() {
    return AUTH != null;
  }

  /**
   * Returns a query from a form which uses the 'application/x-www-form-urlencoded' request header.
   *
   * @param name The name.
   * @return The value, null if there is none.
   */
  public String getFormQuery(String name) {
    return FORM_QUERIES.get(name);
  }

  /**
   * Returns an param from a dynamic url.
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
    return QUERIES.get(name);
  }

  /**
   * Returns all query's from an x-www-form-urlencoded body.
   *
   * @return An entire list of key-values
   */
  public HashMap<String, String> getFormQuerys() {
    return FORM_QUERIES;
  }

  /**
   * Returns all params from the url.
   *
   * @return An entire list of key-values
   */
  public HashMap<String, String> getParams() {
    return params;
  }

  /**
   * Set the params.
   */
  public void setParams(HashMap<String, String> params) {
    this.params = params;
  }

  /**
   * Returns the corresponding context.
   *
   * @return The corresponding context.
   */
  public String getContext() {
    return context;
  }

  /**
   * Set the corresponding context.
   *
   * @param context The corresponding context.
   */
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * Return all url-query's.
   *
   * @return An entire list of key-values
   */
  public HashMap<String, String> getQuerys() {
    return QUERIES;
  }

  /**
   * Returns an header value.
   *
   * @param header The header name
   * @return A list with values.
   */
  public List<String> getHeader(String header) {
    return Optional.ofNullable(HEADERS.get(header)).orElse(Collections.emptyList());
  }

  /**
   * @return The corresponding express object.
   */
  public Express getApp() {
    return EXPRESS;
  }

}
