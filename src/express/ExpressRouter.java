package express;

import com.sun.istack.internal.NotNull;
import express.filter.FilterImpl;
import express.filter.FilterLayerHandler;
import express.filter.FilterTask;
import express.filter.FilterWorker;
import express.http.HttpRequest;

import java.util.ArrayList;

public class ExpressRouter implements Router {

  private final ArrayList<FilterWorker> WORKER;
  private final FilterLayerHandler HANDLER;

  {
    // Initialize
    WORKER = new ArrayList<>();
    HANDLER = new FilterLayerHandler(2);
  }

  public ExpressRouter() {

  }

  public void use(@NotNull HttpRequest middleware) {
    addMiddleware("*", "*", middleware);
  }

  public void use(@NotNull String context, @NotNull HttpRequest middleware) {
    addMiddleware("*", context, middleware);
  }

  public void use(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
  }

  private void addMiddleware(@NotNull String requestMethod, @NotNull String context, HttpRequest middleware) {
    if (middleware instanceof FilterTask) {
      WORKER.add(new FilterWorker((FilterTask) middleware));
    }

    HANDLER.add(0, new FilterImpl(requestMethod, context, middleware));
  }

  public void all(@NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("*", "*", request));
  }

  public void all(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("*", context, request));
  }

  public void all(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl(requestMethod, context, request));
  }

  public void get(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("GET", context, request));
  }

  public void post(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("POST", context, request));
  }

  public void put(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("PUT", context, request));
  }

  public void delete(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("DELETE", context, request));
  }

  public void patch(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl("PATCH", context, request));
  }

  public void on(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest request) {
    HANDLER.add(1, new FilterImpl(requestMethod, context, request));
  }

  ArrayList<FilterWorker> getWorker() {
    return WORKER;
  }

  FilterLayerHandler getHandler() {
    return HANDLER;
  }
}
