package express;

import com.sun.net.httpserver.*;
import express.events.Action;
import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;
import express.middleware.ExpressMiddleware;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Express {

  private final List<Filter> FILTER = Collections.synchronizedList(new ArrayList<>());

  private HttpServer httpServer;
  private HttpContext httpContext;
  private HttpRequest request404;

  public Express() {

  }

  public void use(HttpRequest request) {
    add("*", "*", request);
  }

  public void use(String context, HttpRequest request) {
    add("*", context, request);
  }

  public void use(String method, String context, HttpRequest request) {
    add(method.toUpperCase(), context, request);
  }

  public void on(String method, String context, HttpRequest request) {
    add(method.toUpperCase(), context, request);
  }

  public void all(String context, HttpRequest request) {
    add("*", context, request);
  }

  public void get(String context, HttpRequest request) {
    add("GET", context, request);
  }

  public void post(String context, HttpRequest request) {
    add("POST", context, request);
  }

  public void put(String context, HttpRequest request) {
    add("PUT", context, request);
  }

  public void delete(String context, HttpRequest request) {
    add("DELETE", context, request);
  }

  public void patch(String context, HttpRequest request) {
    add("PATCH", context, request);
  }

  private void add(String requestMethod, String context, HttpRequest request) {
    ExpressMiddleware handler = new ExpressMiddleware(requestMethod, context, request);
    if (httpContext == null) {
      FILTER.add(handler);
    } else {
      httpContext.getFilters().add(handler);
    }
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

  public void set404(HttpRequest request) {
    this.request404 = request;
  }

  private void launch(Action action, int port) throws IOException {
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

        httpContext.getFilters().addAll(FILTER);

        httpServer.start();
        action.action();
      } catch (IOException e) {
        // TODO: Handle errors
        e.printStackTrace();
      }
    }).start();
  }

}
