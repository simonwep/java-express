package express;

import com.sun.istack.internal.NotNull;
import express.events.HttpRequest;
import express.expressfilter.ExpressFilterImpl;
import express.expressfilter.ExpressFilterTask;
import express.expressfilter.ExpressFilterWorker;
import express.expressfilter.FilterLayerHandler;

import java.util.ArrayList;

public class ExpressRouter implements Router {

  private final ArrayList<ExpressFilterWorker> WORKER;
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
    if (middleware instanceof ExpressFilterTask) {
      WORKER.add(new ExpressFilterWorker((ExpressFilterTask) middleware));
    }

    HANDLER.add(0, new ExpressFilterImpl(requestMethod, context, middleware));
  }

  public void all(@NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("*", "*", request));
  }

  public void all(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("*", context, request));
  }

  public void all(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(requestMethod, context, request));
  }

  public void get(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("GET", context, request));
  }

  public void post(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("POST", context, request));
  }

  public void put(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("PUT", context, request));
  }

  public void delete(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("DELETE", context, request));
  }

  public void patch(@NotNull String context, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl("PATCH", context, request));
  }

  public void on(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest request) {
    HANDLER.add(1, new ExpressFilterImpl(requestMethod, context, request));
  }

  ArrayList<ExpressFilterWorker> getWorker() {
    return WORKER;
  }

  FilterLayerHandler getHandler() {
    return HANDLER;
  }
}
