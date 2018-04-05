package express;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import express.filter.FilterImpl;
import express.filter.FilterLayerHandler;
import express.filter.FilterTask;
import express.filter.FilterWorker;
import express.http.HttpRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Simon Reinisch
 * Core class of java-express
 */
public class Express implements Router {

  private final ConcurrentHashMap<String, HttpRequest> PARAMETER_LISTENER;
  private final ConcurrentHashMap<Object, Object> LOCALS;

  private final ArrayList<FilterWorker> WORKER;
  private final FilterLayerHandler HANDLER;

  private Executor executor;
  private String hostname;
  private HttpServer httpServer;
  private HttpsConfigurator httpsConfigurator;

  {
    // Initialize
    PARAMETER_LISTENER = new ConcurrentHashMap<>();
    LOCALS = new ConcurrentHashMap<>();

    WORKER = new ArrayList<>();
    HANDLER = new FilterLayerHandler(2);

    executor = Executors.newCachedThreadPool();
  }

  /**
   * Create an express instance and bind the server to an hostname.
   * Default is "Localhost"
   *
   * @param hostname The host name
   */
  public Express(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Default, will bind the server to "localhost"
   *
   * @param httpsConfigurator The HttpsConfigurator for https
   */
  public Express(HttpsConfigurator httpsConfigurator) {
    this.httpsConfigurator = httpsConfigurator;
  }

  /**
   * Create an express instance and bind the server to an hostname.
   * Default is "Localhost"
   *
   * @param hostname          The host name
   * @param httpsConfigurator The HttpsConfigurator for https
   */
  public Express(String hostname, HttpsConfigurator httpsConfigurator) {
    this.hostname = hostname;
    this.httpsConfigurator = httpsConfigurator;
  }

  /**
   * Default, will bind the server to "localhost"
   */
  public Express() {
  }

  /**
   * @return True if the server uses https.
   */
  public boolean isSecure() {
    return httpsConfigurator != null;
  }

  /**
   * Add an listener which will be called when an url with this parameter is called.
   *
   * @param param   The parameter name.
   * @param request An request handler.
   */
  public void onParam(String param, HttpRequest request) {
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
  public Object set(String key, String val) {
    return LOCALS.put(key, val);
  }

  /**
   * Returns the value which was allocated by this key.
   *
   * @param key The key.
   * @return The value.
   */
  public Object get(String key) {
    return LOCALS.get(key);
  }

  /**
   * Set an executor service. Default is CachedThreadPool
   * Can only changed if the server isn't already stardet.
   *
   * @param executor The new executor.
   * @throws IOException If the server is currently running
   */
  public void setExecutor(Executor executor) throws IOException {
    if (httpServer != null) {
      throw new IOException("Cannot set the executor after the server has starderd!");
    } else {
      this.executor = executor;
    }
  }

  /**
   * Add an routing object.
   *
   * @param router The router.
   */
  public void use(ExpressRouter router) {
    this.HANDLER.combine(router.getHandler());
    this.WORKER.addAll(router.getWorker());
  }

  /**
   * Add an routing object with an specific root root.
   *
   * @param root   The root path for all request to this router.
   * @param router The router.
   */
  @SuppressWarnings("unchecked")
  public void use(String root, ExpressRouter router) {

    router.getHandler().forEach(fl -> fl.getFilter().forEach(layer -> {
      ((FilterImpl) layer).setRoot(root);
    }));

    this.HANDLER.combine(router.getHandler());
    this.WORKER.addAll(router.getWorker());
  }

  public void use(HttpRequest middleware) {
    addMiddleware("*", "*", middleware);
  }

  public void use(String context, HttpRequest middleware) {
    addMiddleware("*", context, middleware);
  }

  public void use(String context, String requestMethod, HttpRequest middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
  }

  // Internal service to handle middleware
  private void addMiddleware(String requestMethod, String context, HttpRequest middleware) {
    if (middleware instanceof FilterTask) {
      WORKER.add(new FilterWorker((FilterTask) middleware));
    }

    HANDLER.add(0, new FilterImpl(requestMethod, context, middleware));
  }

  public void all(HttpRequest request) {
    HANDLER.add(1, new FilterImpl("*", "*", request));
  }

  public void all(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("*", context, request));
  }

  public void all(String context, String requestMethod, HttpRequest request) {
    HANDLER.add(1, new FilterImpl(requestMethod, context, request));
  }

  public void get(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("GET", context, request));
  }

  public void post(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("POST", context, request));
  }

  public void put(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("PUT", context, request));
  }

  public void delete(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("DELETE", context, request));
  }

  public void patch(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("PATCH", context, request));
  }

  /**
   * Start the HTTP-Server on port 80.
   * This method is asynchronous so be sure to add an listener or keep it in mind!
   */
  public void listen() {
    listen(null, 80);
  }

  /**
   * Start the HTTP-Server on a specific port
   * This method is asynchronous so be sure to add an listener or keep it in mind!
   *
   * @param port The port.
   */
  public void listen(int port) {
    listen(null, port);
  }

  /**
   * Start the HTTP-Server on port 80.
   * This method is asynchronous so be sure to add an listener or keep it in mind!
   *
   * @param onStart An listener which will be fired after the server is stardet.
   */
  public void listen(ExpressListener onStart) {
    listen(onStart, 80);
  }

  /**
   * Start the HTTP-Server on a specific port.
   * This method is asynchronous so be sure to add an listener or keep it in mind!
   *
   * @param onStart An listener which will be fired after the server is stardet.
   * @param port    The port.
   */
  public void listen(ExpressListener onStart, int port) {
    new Thread(() -> {
      try {

        // Fire worker threads
        WORKER.forEach(FilterWorker::start);

        InetSocketAddress socketAddress = this.hostname == null ? new InetSocketAddress(port) : new InetSocketAddress(this.hostname, port);
        if (httpsConfigurator != null) {

          // Create https server
          httpServer = HttpsServer.create(socketAddress, 0);
          ((HttpsServer) httpServer).setHttpsConfigurator(httpsConfigurator);
        } else {

          // Create http server
          httpServer = HttpServer.create(socketAddress, 0);
        }

        // Set thread executor
        httpServer.setExecutor(executor);

        // Create handler for all contexts
        httpServer.createContext("/", exchange -> HANDLER.handle(exchange, this));

        // Start server
        httpServer.start();

        // Fire listener
        if (onStart != null)
          onStart.action();

      } catch (IOException e) {
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
      WORKER.forEach(FilterWorker::stop);
    }
  }
}
