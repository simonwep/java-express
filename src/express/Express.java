package express;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import express.events.Action;
import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Simon Reinisch
 * @implNote Core modul, don't change anything.
 *
 * An NodeJS like clone written in Java, see README for more information.
 */
public class Express {

  private final List<Filter> MIDDLEWARE = Collections.synchronizedList(new ArrayList<>());
  private final List<Filter> FILTER = Collections.synchronizedList(new ArrayList<>());

  // Index of last middleware to keep it sorted
  int middlewareIndex = 0;

  private HttpServer httpServer;
  private HttpContext httpContext;
  private HttpRequest request404;

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param request An listener which will be fired on every equestmethod- and  path.
   */
  public void use(HttpRequest request) {
    addFilter(true, "*", "*", request);
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param context The context where the middleware should listen, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void use(String context, HttpRequest request) {
    addFilter(true, "*", context, request);
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param context       The context where the middleware should listen, see README for information about placeholder.
   * @param requestMethod And type of request-method eg. GET, POST etc.
   * @param request       An listener which will be fired if the context matches the requestmethod- and  path.
   */
  public void use(String context, String requestMethod, HttpRequest request) {
    addFilter(true, requestMethod.toUpperCase(), context, request);
  }

  /**
   * Add an listener for request types.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener.
   */
  public void all(String context, HttpRequest request) {
    addFilter(false, "*", context, request);
  }

  /**
   * Add an listener for GET request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void get(String context, HttpRequest request) {
    addFilter(false, "GET", context, request);
  }

  /**
   * Add an listener for POST request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void post(String context, HttpRequest request) {
    addFilter(false, "POST", context, request);
  }

  /**
   * Add an listener for PUT request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void put(String context, HttpRequest request) {
    addFilter(false, "PUT", context, request);
  }

  /**
   * Add an listener for DELETE request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void delete(String context, HttpRequest request) {
    addFilter(false, "DELETE", context, request);
  }

  /**
   * Add an listener for PATCH request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void patch(String context, HttpRequest request) {
    addFilter(false, "PATCH", context, request);
  }

  /**
   * Internal method to add an filter
   *
   * @param middleware    If the filter is an middleware
   * @param requestMethod The request-method
   * @param context       The url-path
   * @param request       An listener which will be fired if the given context is matching
   */
  private void addFilter(boolean middleware, String requestMethod, String context, HttpRequest request) {
    ExpressFilter handler = new ExpressFilter(requestMethod, context, request);

    // Check if the server is already active
    if (httpContext == null) {

      // Middleware needs an seperated list because it will ALWAYS fired before each request handler
      if (middleware) {
        MIDDLEWARE.add(handler);
      } else {
        FILTER.add(handler);
      }
    } else {
      List<Filter> filters = httpContext.getFilters();

      if (middleware) {

        // Insert middleware after the last middleware
        filters.add(middlewareIndex, handler);
        middlewareIndex++;
      } else filters.add(handler);
    }
  }

  /**
   * Set an extra lisener for 404 (not found) requests, also request where no context
   * match the given request path (or method).
   *
   * @param request An listener.
   */
  public void set404(HttpRequest request) {
    this.request404 = request;
  }

  /**
   * Start the HTTP-Server on port 80.
   * This method is asyncronous so be sure to add an listener or keep it in mind!
   *
   * @throws IOException - If an IO-Error occurs, eg. the port is already in use.
   */
  public void listen() throws IOException {
    listen(null, 80);
  }

  /**
   * Start the HTTP-Server on a specific port
   * This method is asyncronous so be sure to add an listener or keep it in mind!
   *
   * @param port The port.
   * @throws IOException - If an IO-Error occurs, eg. the port is already in use.
   */
  public void listen(int port) throws IOException {
    listen(null, port);
  }

  /**
   * Start the HTTP-Server on port 80.
   * This method is asyncronous so be sure to add an listener or keep it in mind!
   *
   * @param onStart An listener which will be fired after the server is stardet.
   * @throws IOException - If an IO-Error occurs, eg. the port is already in use.
   */
  public void listen(Action onStart) throws IOException {
    listen(onStart, 80);
  }

  /**
   * Start the HTTP-Server on a specific port.
   * This method is asyncronous so be sure to add an listener or keep it in mind!
   *
   * @param onStart An listener which will be fired after the server is stardet.
   * @param port    The port.
   * @throws IOException - If an IO-Error occurs, eg. the port is already in use.
   */
  public void listen(Action onStart, int port) throws IOException {
    new Thread(() -> {
      try {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        httpServer.setExecutor(null);

        httpContext = httpServer.createContext("/", httpExchange -> {
          if (request404 != null) {
            Request request = new Request(httpExchange);
            Response response = new Response(httpExchange);
            request404.handle(request, response);
          } else {
            System.err.println("Not 404 Handler specified");
          }
        });

        httpContext.getFilters().addAll(MIDDLEWARE);
        httpContext.getFilters().addAll(FILTER);

        httpServer.start();

        // Fire listener
        onStart.action();
      } catch (IOException e) {
        // TODO: Handle errors
        e.printStackTrace();
      }
    }).start();
  }

  /**
   * This method serves an entire folder which can contains static file for your
   * web application, it automatically detect the content type and will send it to
   * the Client.
   * <p>
   * To use it simply put it in the <code>app.use()</code> method!
   *
   * @param path The root directory
   * @return An HttpRequest interface with the service.
   */
  public static HttpRequest statics(String path) {
    return (req, res) -> {
      File reqFile = new File(path + req.getURI().getPath());

      if (reqFile.exists()) {
        String extension = reqFile.getAbsolutePath().replaceAll("^(.*\\.|.*\\\\|.+$)", "");
        String contentType = ExpressUtils.getContentType(extension);
        res.send(reqFile, contentType);
      }
    };
  }
}
