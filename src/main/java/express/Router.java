package express;

import express.http.HttpRequestHandler;

/**
 * @author Simon Reinisch
 * Router interface for express
 */
public interface Router {

  /**
   * Add an middleware which will be called before each request-type listener will be fired.
   *
   * @param middleware An middleware which will be fired on every request-method and path.
   * @return The router itself to allow method call chaining.
   */
  Router use(HttpRequestHandler middleware);

  /**
   * Add an middleware which will be called before each request-type listener will be fired.
   *
   * @param context    The context where the middleware should listen.
   * @param middleware An middleware which will be fired if the context matches the request-path.
   *
   * @return The router itself to allow method call chaining.
   */
  Router use(String context, HttpRequestHandler middleware);

  /**
   * Add an middleware which will be called before each request-type listener will be fired.
   *
   * @param context       The context where the middleware should listen for the request handler..
   * @param requestMethod And type of request-method eg. GET, POST etc.
   * @param middleware    An middleware which will be fired if the context matches the request-method- and  path.
   *
   * @return The router itself to allow method call chaining.
   */
  Router use(String context, String requestMethod, HttpRequestHandler middleware);

  /**
   * Add an listener for all request methods and contexts.
   *
   * @param request Will be fired on all requests.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router all(HttpRequestHandler request);

  /**
   * Adds an handler for a specific context.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the request-path.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router all(String context, HttpRequestHandler request);

  /**
   * Adds an handler for a specific context and method.
   * You can use a star '*' to match every context / request-method.
   *
   * @param context       The context.
   * @param requestMethod The request method.
   * @param request       An listener which will be fired if the context matches the request-path.
   *
   * @return              The router itself to allow method call chaining.
   */
  Router all(String context, String requestMethod, HttpRequestHandler request);

  /**
   * Add an listener for GET request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the request-path.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router get(String context, HttpRequestHandler request);

  /**
   * Add an listener for POST request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the request-path.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router post(String context, HttpRequestHandler request);

  /**
   * Add an listener for PUT request's.
   *
   * @param context The context for the request handler..
   * @param request An listener which will be fired if the context matches the request-path.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router put(String context, HttpRequestHandler request);

  /**
   * Add an listener for DELETE request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the request-path.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router delete(String context, HttpRequestHandler request);

  /**
   * Add an listener for PATCH request's.
   *
   * @param context The context.
   * @param request An listener which will be fired if the context matches the request-path.
   *
   * @return        The router itself to allow method call chaining.
   */
  Router patch(String context, HttpRequestHandler request);

}
