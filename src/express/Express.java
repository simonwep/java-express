package express;

import com.sun.istack.internal.NotNull;
import com.sun.net.httpserver.HttpServer;
import express.events.Action;
import express.events.HttpRequest;
import express.expressfilter.ExpressFilterImpl;
import express.expressfilter.ExpressFilterTask;
import express.expressfilter.ExpressFilterWorker;
import express.expressfilter.FilterLayerHandler;
import express.middleware.ExpressMiddleware;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Simon Reinisch
 * @implNote Core modul, don't change anything.
 * <p>
 * An NodeJS like clone written in Java.
 */
public class Express extends ExpressMiddleware {

  private final ConcurrentHashMap<String, HttpRequest> PARAMETER_LISTENER;
  private final ConcurrentHashMap<Object, Object> LOCALS;

  private final ArrayList<ExpressFilterWorker> WORKER;
  private final FilterLayerHandler HANDLER;

  private Executor executor;
  private String hostname;
  private HttpServer httpServer;

  {
    // Initialize
    PARAMETER_LISTENER = new ConcurrentHashMap<>();
    LOCALS = new ConcurrentHashMap<>();

    WORKER = new ArrayList<>();
    HANDLER = new FilterLayerHandler();

    executor = Executors.newCachedThreadPool();
    hostname = "localhost";
  }

  /**
   * Create an express instance and bind the server to an hostname.
   * Default is "Localhost"
   *
   * @param hostname The host name
   */
  public Express(@NotNull String hostname) {
    this.hostname = hostname;
  }

  /**
   * Default, will bind the server to "localhost"
   */
  public Express() {
  }

  /**
   * Add an listener which will be called when an url with this parameter is called.
   *
   * @param param   The parameter name.
   * @param request An request handler.
   */
  public void onParam(@NotNull String param, @NotNull HttpRequest request) {
    PARAMETER_LISTENER.put(param, request);
  }

  public ConcurrentHashMap<String, HttpRequest> getParameterListener() {
    return PARAMETER_LISTENER;
  }

  /**
   * Add an key-val pair to the express app, can be used
   * to store data. Uses ConcurrentHashMap so it's thread save.
   *
   * @param key The key
   * @param val The value
   * @return The last value which was attached by this key, can be null.
   */
  public Object set(Object key, Object val) {
    return LOCALS.put(key, val);
  }

  /**
   * Returns the value which was allocated by this key.
   *
   * @param key The key.
   * @return The value.
   */
  public Object get(Object key) {
    return LOCALS.get(key);
  }

  /**
   * Set an executor service. Default is CachedThreadPool
   * Can only changed if the server isn't already stardet.
   *
   * @param executor The new executor.
   * @throws IOException If the server is currently running
   */
  public void setExecutor(@NotNull Executor executor) throws IOException {
    if (httpServer != null) {
      throw new IOException("Cannot set the executor after the server has starderd!");
    } else {
      this.executor = executor;
    }
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param middleware An middleware which will be fired on every equestmethod- and  path.
   */
  public void use(@NotNull HttpRequest middleware) {
    addMiddleware("*", "*", middleware);
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param context    The context where the middleware should listen.
   * @param middleware An middleware which will be fired if the context matches the requestpath.
   */
  public void use(@NotNull String context, @NotNull HttpRequest middleware) {
    addMiddleware("*", context, middleware);
  }

  /**
   * Add an middleware which will be firea BEFORE EACH request-type listener will be fired.
   *
   * @param context       The context where the middleware should listen for the request handler..
   * @param requestMethod And type of request-method eg. GET, POST etc.
   * @param middleware    An middleware which will be fired if the context matches the requestmethod- and  path.
   */
  public void use(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
  }

  // Internal service to handle middleware
  private void addMiddleware(@NotNull String requestMethod, @NotNull String context, HttpRequest middleware) {
    if (middleware instanceof ExpressFilterTask) {
      WORKER.add(new ExpressFilterWorker((ExpressFilterTask) middleware));
    }

    HANDLER.add(0, new ExpressFilterImpl(this, requestMethod, context, middleware));
  }

  /**
   * Add an listener for all request methods.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void all(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, "*", context, request));
  }

  /**
   * Add an listener for GET request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void get(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, "GET", context, request));
  }

  /**
   * Add an listener for POST request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void post(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, "POST", context, request));
  }

  /**
   * Add an listener for PUT request's.
   *
   * @param context The context for the request handler..
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void put(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, "PUT", context, request));
  }

  /**
   * Add an listener for DELETE request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void delete(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, "DELETE", context, request));
  }

  /**
   * Add an listener for PATCH request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  public void patch(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, "PATCH", context, request));
  }

  /**
   * Add an listener for an specific request method.
   *
   * @param requestMethod The request method
   * @param context       The context for the request handler.
   * @param request       An listener which will be fired if the context matches the requestpath.
   */
  public void on(@NotNull String requestMethod, @NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(this, requestMethod, context, request));
  }

  /**
   * Add an listener for an specific request method.
   * You cann add multiple contexts.
   *
   * @param requestMethod The request method
   * @param contexts      The contexts for the request handler..
   * @param request       An listener which will be fired if the context matches the requestpath.
   */
  public void on(@NotNull String requestMethod, @NotNull HttpRequest request, String... contexts) {
    for (String c : contexts)
      HANDLER.add(1, new ExpressFilterImpl(this, requestMethod, c, request));
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
        httpServer = HttpServer.create(new InetSocketAddress(this.hostname, port), 0);
        httpServer.setExecutor(executor);           // Set thread executor
        httpServer.createContext("/", HANDLER);  // Set handler for all contexts
        httpServer.start();                         // Start server

        // Fire listener
        if (onStart != null)
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
