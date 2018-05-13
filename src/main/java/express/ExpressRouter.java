package express;

import express.filter.FilterImpl;
import express.filter.FilterLayerHandler;
import express.filter.FilterTask;
import express.filter.FilterWorker;
import express.http.HttpRequestHandler;

import java.util.ArrayList;

/**
 * @author Simon Reinisch
 * Basic implementation of an router
 */
public class ExpressRouter implements Router {

  private final ArrayList<FilterWorker> workers;
  private final FilterLayerHandler handler;

  {
    // Initialize
    workers = new ArrayList<>();
    handler = new FilterLayerHandler(2);
  }

  public ExpressRouter use(HttpRequestHandler middleware) {
    addMiddleware("*", "*", middleware);
    return this;
  }

  public ExpressRouter use(String context, HttpRequestHandler middleware) {
    addMiddleware("*", context, middleware);
    return this;
  }

  public ExpressRouter use(String context, String requestMethod, HttpRequestHandler middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
    return this;
  }

  private void addMiddleware(String requestMethod, String context, HttpRequestHandler middleware) {
    if (middleware instanceof FilterTask) {
      workers.add(new FilterWorker((FilterTask) middleware));
    }

    handler.add(0, new FilterImpl(requestMethod, context, middleware));
  }

  public ExpressRouter all(HttpRequestHandler request) {
    handler.add(1, new FilterImpl("*", "*", request));
    return this;
  }

  public ExpressRouter all(String context, HttpRequestHandler request) {
    handler.add(1, new FilterImpl("*", context, request));
    return this;
  }

  public ExpressRouter all(String context, String requestMethod, HttpRequestHandler request) {
    handler.add(1, new FilterImpl(requestMethod, context, request));
    return this;
  }

  public ExpressRouter get(String context, HttpRequestHandler request) {
    handler.add(1, new FilterImpl("GET", context, request));
    return this;
  }

  public ExpressRouter post(String context, HttpRequestHandler request) {
    handler.add(1, new FilterImpl("POST", context, request));
    return this;
  }

  public ExpressRouter put(String context, HttpRequestHandler request) {
    handler.add(1, new FilterImpl("PUT", context, request));
    return this;
  }

  public ExpressRouter delete(String context, HttpRequestHandler request) {
    handler.add(1, new FilterImpl("DELETE", context, request));
    return this;
  }

  public ExpressRouter patch(String context, HttpRequestHandler request) {
    handler.add(1, new FilterImpl("PATCH", context, request));
    return this;
  }

  ArrayList<FilterWorker> getWorker() {
    return workers;
  }

  FilterLayerHandler getHandler() {
    return handler;
  }
}
