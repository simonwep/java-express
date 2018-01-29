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

public class Express {

  private final List<Filter> MIDDLEWARE = Collections.synchronizedList(new ArrayList<>());
  private final List<Filter> FILTER = Collections.synchronizedList(new ArrayList<>());
  int middlewareIndex = 0;

  private HttpServer httpServer;
  private HttpContext httpContext;
  private HttpRequest request404;

  public void use(HttpRequest request) {
    addFilter(true, "*", "*", request);
  }

  public void use(String context, HttpRequest request) {
    addFilter(true, "*", context, request);
  }

  public void use(String context, String requestMethod, HttpRequest request) {
    addFilter(true, requestMethod.toUpperCase(), context, request);
  }

  public void all(String context, HttpRequest request) {
    addFilter(false, "*", context, request);
  }

  public void get(String context, HttpRequest request) {
    addFilter(false, "GET", context, request);
  }

  public void post(String context, HttpRequest request) {
    addFilter(false, "POST", context, request);
  }

  public void put(String context, HttpRequest request) {
    addFilter(false, "PUT", context, request);
  }

  public void delete(String context, HttpRequest request) {
    addFilter(false, "DELETE", context, request);
  }

  public void patch(String context, HttpRequest request) {
    addFilter(false, "PATCH", context, request);
  }

  private void addFilter(boolean middleware, String requestMethod, String context, HttpRequest request) {
    ExpressFilter handler = new ExpressFilter(requestMethod, context, request);
    if (httpContext == null) {
      if (middleware) {
        MIDDLEWARE.add(handler);
      } else {
        FILTER.add(handler);
      }
    } else {
      List<Filter> filters = httpContext.getFilters();

      if (middleware) {
        filters.add(middlewareIndex, handler);
        middlewareIndex++;
      } else filters.add(handler);
    }
  }

  private void addFirst(String requestMethod, String context, HttpRequest request) {
    ExpressFilter handler = new ExpressFilter(requestMethod, context, request);
    if (httpContext == null) {
      MIDDLEWARE.add(handler);
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

        httpContext.getFilters().addAll(MIDDLEWARE);
        httpContext.getFilters().addAll(FILTER);

        httpServer.start();
        action.action();
      } catch (IOException e) {
        // TODO: Handle errors
        e.printStackTrace();
      }
    }).start();
  }

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
