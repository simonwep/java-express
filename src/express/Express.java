package express;

import com.sun.net.httpserver.HttpServer;
import express.events.Action;
import express.events.HttpRequest;
import express.expressfilter.ExpressFilterChain;
import express.expressfilter.ExpressFilterImpl;
import express.expressfilter.ExpressFilterTask;
import express.expressfilter.ExpressFilterWorker;
import express.http.Request;
import express.http.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * @author Simon Reinisch
 * @implNote Core modul, don't change anything.
 * <p>
 * An NodeJS like clone written in Java.
 */
public class Express {

  private final ArrayList<ExpressFilterWorker> WORKER = new ArrayList<>();
  private final ExpressFilterChain MIDDLEWARE_CHAIN = new ExpressFilterChain();
  private final ExpressFilterChain FILTER_CHAIN = new ExpressFilterChain();

  private HttpServer httpServer;

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param middleware An middleware which will be fired on every equestmethod- and  path.
   */
  public void use(HttpRequest middleware) {
    addMiddleware("*", "*", middleware);
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param context    The context where the middleware should listen.
   * @param middleware An middleware which will be fired if the context matches the requestpath.
   */
  public void use(String context, HttpRequest middleware) {
    addMiddleware("*", context, middleware);
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param context       The context where the middleware should listen, see README for information about placeholder.
   * @param requestMethod And type of request-method eg. GET, POST etc.
   * @param middleware    An middleware which will be fired if the context matches the requestmethod- and  path.
   */
  public void use(String context, String requestMethod, HttpRequest middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
  }

  private void addMiddleware(String requestMethod, String context, HttpRequest middleware) {
    if (middleware instanceof ExpressFilterTask) {
      WORKER.add(new ExpressFilterWorker((ExpressFilterTask) middleware));
    }

    MIDDLEWARE_CHAIN.add(new ExpressFilterImpl(requestMethod, context, middleware));
  }

  /**
   * Add an listener for GET request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void all(String context, HttpRequest request) {
    FILTER_CHAIN.add(new ExpressFilterImpl("*", context, request));
  }

  /**
   * Add an listener for GET request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void get(String context, HttpRequest request) {
    FILTER_CHAIN.add(new ExpressFilterImpl("GET", context, request));
  }

  /**
   * Add an listener for POST request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void post(String context, HttpRequest request) {
    FILTER_CHAIN.add(new ExpressFilterImpl("POST", context, request));
  }

  /**
   * Add an listener for PUT request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void put(String context, HttpRequest request) {
    FILTER_CHAIN.add(new ExpressFilterImpl("PUT", context, request));
  }

  /**
   * Add an listener for DELETE request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void delete(String context, HttpRequest request) {
    FILTER_CHAIN.add(new ExpressFilterImpl("DELETE", context, request));
  }

  /**
   * Add an listener for PATCH request's.
   *
   * @param context The context, see README for information about placeholder.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void patch(String context, HttpRequest request) {
    FILTER_CHAIN.add(new ExpressFilterImpl("PATCH", context, request));
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
        // Fire worker threads
        WORKER.forEach(ExpressFilterWorker::start);


        // Create http server
        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        httpServer.setExecutor(null);

        httpServer.createContext("/", httpExchange -> {
          Request request = new Request(httpExchange);
          Response response = new Response(httpExchange);

          // First fire all middlewares, then the normal request filter
          MIDDLEWARE_CHAIN.filter(request, response);
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

  /**
   * Stop express
   */
  public void stop() {
    if (httpServer != null) {

      // Stop http-server
      httpServer.stop(0);

      // Stop worker threads
      WORKER.forEach(ExpressFilterWorker::stop);
    }
  }
}
