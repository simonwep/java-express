package express;

import com.sun.istack.internal.NotNull;
import express.http.HttpRequest;

/**
 * @author Simon Reinisch
 * Router interface for express
 */
public interface Router {

  /**
   * Add an middleware which will be called berfore each request-type listener will be fired.
   *
   * @param middleware An middleware which will be fired on every equestmethod- and  path.
   */
  void use(@NotNull HttpRequest middleware);

  /**
   * Add an middleware which will be called berfore each request-type listener will be fired.
   *
   * @param context    The context where the middleware should listen.
   * @param middleware An middleware which will be fired if the context matches the requestpath.
   */
  void use(@NotNull String context, @NotNull HttpRequest middleware);

  /**
   * Add an middleware which will be called berfore each request-type listener will be fired.
   *
   * @param context       The context where the middleware should listen for the request handler..
   * @param requestMethod And type of request-method eg. GET, POST etc.
   * @param middleware    An middleware which will be fired if the context matches the requestmethod- and  path.
   */
  void use(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest middleware);

  /**
   * Add an listener for all request methods and contexts.
   *
   * @param request Will be fired on all requests.
   */
  void all(@NotNull HttpRequest request);

  /**
   * Adds an handler for a specific context.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  void all(@NotNull String context, @NotNull HttpRequest request);

  /**
   * Adds an handler for a specific context and method.
   * You can use a star '*' to match every context / request-method.
   *
   * @param context       The context.
   * @param requestMethod The request method.
   * @param request       An listener which will be fired if the context matches the requestpath.
   */
  void all(@NotNull String context, @NotNull String requestMethod, @NotNull HttpRequest request);

  /**
   * Add an listener for GET request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  void get(@NotNull String context, @NotNull HttpRequest request);

  /**
   * Add an listener for POST request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  void post(@NotNull String context, @NotNull HttpRequest request);

  /**
   * Add an listener for PUT request's.
   *
   * @param context The context for the request handler..
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  void put(@NotNull String context, @NotNull HttpRequest request);

  /**
   * Add an listener for DELETE request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  void delete(@NotNull String context, @NotNull HttpRequest request);

  /**
   * Add an listener for PATCH request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the requestpath.
   */
  void patch(@NotNull String context, @NotNull HttpRequest request);

}
