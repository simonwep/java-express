package express;

import com.sun.net.httpserver.HttpServer;
import express.events.Action;
import express.events.HttpRequest;
import express.expressfilter.ExpressFilter;
import express.expressfilter.ExpressFilterChain;
import express.http.Request;
import express.http.Response;
import express.middleware.ExpressWorker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Simon Reinisch
 * @implNote Core modul, don't change anything.
 * <p>
 * An NodeJS like clone written in Java, see README for more information.
 */
public class Express {

  private final List<ExpressWorker> WORKERS = Collections.synchronizedList(new ArrayList<>());
  private final ExpressFilterChain FILTER_CHAIN = new ExpressFilterChain();

  private HttpServer httpServer;

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

    if (request instanceof ExpressWorker){
      ((ExpressWorker) request).start();
      WORKERS.add((ExpressWorker) request);
    }


    // Middleware needs an seperated list because it will ALWAYS fired before each request handler
    if (middleware)
      FILTER_CHAIN.addMiddleware(handler);
    else
      FILTER_CHAIN.addFilter(handler);
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

        httpServer.createContext("/", httpExchange -> {
          Request request = new Request(httpExchange);
          Response response = new Response(httpExchange);
          FILTER_CHAIN.filter(request, response);
        });

        httpServer.start();

        // Fire listener
        onStart.action();
      } catch (IOException e) {
        // TODO: Handle errors
        e.printStackTrace();
      }
    }).start();
  }
}
