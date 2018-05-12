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

  public ExpressRouter use(HttpRequest middleware) {
    addMiddleware("*", "*", middleware);
    return this;
  }

  public ExpressRouter use(String context, HttpRequest middleware) {
    addMiddleware("*", context, middleware);
    return this;
  }

  public ExpressRouter use(String context, String requestMethod, HttpRequest middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
    return this;
  }

  private void addMiddleware(String requestMethod, String context, HttpRequest middleware) {
    if (middleware instanceof FilterTask) {
      WORKER.add(new FilterWorker((FilterTask) middleware));
    }

    HANDLER.add(0, new FilterImpl(requestMethod, context, middleware));
  }

  public ExpressRouter all(HttpRequest request) {
    HANDLER.add(1, new FilterImpl("*", "*", request));
    return this;
  }

  public ExpressRouter all(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("*", context, request));
    return this;
  }

  public ExpressRouter all(String context, String requestMethod, HttpRequest request) {
    HANDLER.add(1, new FilterImpl(requestMethod, context, request));
    return this;
  }

  public ExpressRouter get(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("GET", context, request));
    return this;
  }

  public ExpressRouter post(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("POST", context, request));
    return this;
  }

  public ExpressRouter put(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("PUT", context, request));
    return this;
  }

  public ExpressRouter delete(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("DELETE", context, request));
    return this;
  }

  public ExpressRouter patch(String context, HttpRequest request) {
    HANDLER.add(1, new FilterImpl("PATCH", context, request));
    return this;
  }

  ArrayList<FilterWorker> getWorker() {
    return WORKER;
  }

  FilterLayerHandler getHandler() {
    return HANDLER;
  }
}
