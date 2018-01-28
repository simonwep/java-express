package express;

import com.sun.net.httpserver.HttpServer;
import express.events.Action;
import express.events.HttpRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Express {

  protected final ConcurrentHashMap<String, ArrayList<ExpressHandler>> MITTLEWARE = new ConcurrentHashMap<>();
  protected final ConcurrentHashMap<String, ArrayList<ExpressHandler>> REQUEST = new ConcurrentHashMap<>();

  protected HttpServer httpServer;

  public Express() {
  }

  public void use(HttpRequest request) {
    addToRouteMap(MITTLEWARE, "*", "*", request);
  }

  public void use(String context, HttpRequest request) {
    addToRouteMap(MITTLEWARE, "*", context, request);
  }

  public void use(String method, String context, HttpRequest request) {
    addToRouteMap(MITTLEWARE, method.toUpperCase(), context, request);
  }

  public void on(String method, String context, HttpRequest request) {
    addToRouteMap(REQUEST, method.toUpperCase(), context, request);
  }

  public void all(String context, HttpRequest request) {
    addToRouteMap(REQUEST, "*", context, request);
  }

  public void get(String context, HttpRequest request) {
    addToRouteMap(REQUEST, "GET", context, request);
  }

  public void post(String context, HttpRequest request) {
    addToRouteMap(REQUEST, "POST", context, request);
  }

  public void put(String context, HttpRequest request) {
    addToRouteMap(REQUEST, "PUT", context, request);
  }

  public void delete(String context, HttpRequest request) {
    addToRouteMap(REQUEST, "DELETE", context, request);
  }

  public void patch(String context, HttpRequest request) {
    addToRouteMap(REQUEST, "PATCH", context, request);
  }

  private void addToRouteMap(ConcurrentHashMap<String, ArrayList<ExpressHandler>> routemap, String key, String context, HttpRequest request) {

    if (!routemap.containsKey(key))
      routemap.put(key, new ArrayList<>());

    routemap.get(key).add(new ExpressHandler(context, request));
  }

  public void listen() throws IOException {
    launch(null, 80);
  }


  public void listen(int port) throws IOException {
    launch(null, port);
  }

  public void listen(Action action) throws IOException {
    launch(action, 80);
  }

  public void listen(int port, Action action) throws IOException {
    launch(action, port);
  }

  private void launch(Action action, int port) throws IOException {
    new Thread(() -> {
      try {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        httpServer.setExecutor(null);
        httpServer.createContext("/", new ExpressContext(this));

        httpServer.start();
        action.action();
      } catch (IOException e) {
        // TODO: Handle errors
        e.printStackTrace();
      }
    }).start();
  }

}
