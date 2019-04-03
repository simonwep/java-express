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

    private final Express express;

    private final String protocol;                      // Request protocol
    private final URI uri;                              // Request uri
    private final InputStream body;                     // Request body
    private final Headers headers;                      // Request Headers
    private final boolean secure;
    private final String contentType;                   // Request content-type
    private final long contentLength;                   // Request content-length
    private final String method;                        // Request method
    private final List<Authorization> auth;             // Authorization header parsed
    private final InetSocketAddress inet;               // Client socket address

    private final HashMap<String, Object> middleware;   // Middleware Data
    private final HashMap<String, Cookie> cookies;      // Request cookies
    private final HashMap<String, String> queries;      // URL Query parameters
    private final HashMap<String, String> formQueries; // Form query parameters (application/x-www-form-urlencoded)

    private HashMap<String, String> params;             // URL Params, would be added in ExpressFilterImpl
    private String context;                             // Context which matched

    {
        this.middleware = new HashMap<>();
        this.params = new HashMap<>();
    }

    public Request(HttpExchange exchange, Express express) {
        this.express = express;
        this.method = exchange.getRequestMethod();
        this.uri = exchange.getRequestURI();
        this.headers = exchange.getRequestHeaders();
        this.body = exchange.getRequestBody();
        this.inet = exchange.getRemoteAddress();

        this.protocol = exchange.getProtocol();
        this.secure = exchange instanceof HttpsExchange; // Can be suckered?

        // Parse content length
        String contentLength = headers.get("Content-Length") != null ? headers.get("Content-Length").get(0) : null;
        this.contentLength = contentLength != null ? Long.parseLong(contentLength) : -1;

        // Check if the request contains an body-content
        this.contentType = headers.get("Content-Type") == null ? "" : headers.get("Content-Type").get(0);

        // Check if the request has an Authorization header
        this.auth = Authorization.get(this);

        // Check if the request contains x-www-form-urlencoded form data
        this.formQueries = contentType.startsWith("application/x-www-form-urlencoded")
                ? RequestUtils.parseRawQuery(Utils.streamToString(body))
                : new HashMap<>();

        // Parse query and cookies, both returns not null if there is nothing
        this.queries = RequestUtils.parseRawQuery(exchange.getRequestURI().getRawQuery());
        this.cookies = RequestUtils.parseCookies(headers);
    }

    /**
     * @return The request body as InputStream
     */
    public InputStream getBody() {
        return body;
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
        while ((n = body.read(buffer)) != -1)
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
     * Get a request cookie by name.
     *
     * @param name The cookie name.
     * @return The cookie, null if there is no cookie with this name.
     */
    public Cookie getCookie(String name) {
        return cookies.get(name);
    }

    /**
     * Return all cookies from this request.
     *
     * @return All cookies.
     */
    public HashMap<String, Cookie> getCookies() {
        return cookies;
    }

    /**
     * Add a the content from a middleware
     *
     * @param middleware     The middleware
     * @param middlewareData The data from the middleware
     */
    public void addMiddlewareContent(Filter middleware, Object middlewareData) {
        this.middleware.put(middleware.getName(), middlewareData);
    }

    /**
     * Get the data from a specific middleware by name (Also the reason
     * why the interface ExpressFilter implements a getName())
     *
     * @param name The middleware name
     * @return The middleware object
     */
    public Object getMiddlewareContent(String name) {
        return middleware.get(name);
    }

    /**
     * @return The request user-agent.
     */
    public String getUserAgent() {
        return headers.get("User-agent").get(0);
    }

    /**
     * @return The request host.
     */
    public String getHost() {
        return headers.get("Host").get(0);
    }

    /**
     * Returns the InetAddress from the client.
     *
     * @return The InetAddress.
     */
    public InetAddress getAddress() {
        return inet.getAddress();
    }

    /**
     * Returns the IP-Address from the client.
     *
     * @return The IP-Address.
     */
    public String getIp() {
        return inet.getAddress().getHostAddress();
    }

    /**
     * @return The request content-type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the to long parsed content-length.
     *
     * @return The content-length, -1 if the header was invalid.
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * @return The request path.
     */
    public String getPath() {
        return this.uri.getPath();
    }

    /**
     * @return The original request uri.
     */
    public URI getURI() {
        return this.uri;
    }

    /**
     * @return The request-method.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Checks if the connection is 'fresh'
     * It is true if the cache-control request header doesn't have a no-cache directive, the if-modified-since request header is specified
     * and last-modified request header is equal to or earlier than the modified response header or if-none-match request header is *.
     *
     * @return True if the connection is fresh, false otherwise.
     */
    public boolean isFresh() {

        if (headers.containsKey("cache-control") && headers.get("cache-control").get(0) != null && headers.get("cache-control").get(0).equals("no-cache"))
            return true;

        if (headers.containsKey("if-none-match") && headers.get("if-none-match").get(0) != null && headers.get("if-none-match").get(0).equals("*"))
            return true;

        if (headers.containsKey("if-modified-since") && headers.containsKey("last-modified") && headers.containsKey("modified")) {
            List<String> lmlist = headers.get("last-modified");
            List<String> mlist = headers.get("modified");

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
        return secure;
    }

    /**
     * Returns true if the 'X-Requested-With' header field is 'XMLHttpRequest'.
     * Indicating that the request was made by a client library such as jQuery.
     *
     * @return True if the 'X-Requested-With' header field is 'XMLHttpRequest'.
     */
    public boolean isXHR() {
        return headers.containsKey("X-Requested-With") && !headers.get("X-Requested-With").isEmpty() && headers.get("X-Requested-With").get(0).equals("XMLHttpRequest");
    }

    /**
     * The connection protocol HTTP/1.0, HTTP/1.1 etc.
     *
     * @return The connection protocol.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return A list of authorization options in this request
     */
    public List<Authorization> getAuthorization() {
        return Collections.unmodifiableList(auth);
    }

    /**
     * @return True if there was an Authorization header and the Authorization object was successfully created.
     */
    public boolean hasAuthorization() {
        return !auth.isEmpty();
    }

    /**
     * Returns a query from a form which uses the 'application/x-www-form-urlencoded' request header.
     *
     * @param name The name.
     * @return The value, null if there is none.
     */
    public String getFormQuery(String name) {
        return formQueries.get(name);
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
        return queries.get(name);
    }

    /**
     * Returns all query's from an x-www-form-urlencoded body.
     *
     * @return An entire list of key-values
     */
    public HashMap<String, String> getFormQuerys() {
        return formQueries;
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
     *
     * @param params Request parameter
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
        return queries;
    }

    /**
     * Returns an header value.
     *
     * @param header The header name
     * @return A list with values.
     */
    public List<String> getHeader(String header) {
        return Optional.ofNullable(headers.get(header)).orElse(Collections.emptyList());
    }

    /**
     * @return The corresponding express object.
     */
    public Express getApp() {
        return express;
    }

}
