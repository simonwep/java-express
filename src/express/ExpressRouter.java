package express;

import express.filter.FilterImpl;
import express.filter.FilterLayerHandler;
import express.filter.FilterTask;
import express.filter.FilterWorker;
import express.http.HttpRequest;

import java.util.ArrayList;

/**
 * @author Simon Reinisch
 * Basic implementation of an router
 */
public class ExpressRouter implements Router {

  private final ArrayList<FilterWorker> WORKER;
  private final FilterLayerHandler HANDLER;

  {
    // Initialize
    WORKER = new ArrayList<>();
    HANDLER = new FilterLayerHandler(2);
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

  ArrayList<FilterWorker> getWorker() {
    return WORKER;
  }

  FilterLayerHandler getHandler() {
    return HANDLER;
  }
}
