package express.http;

import express.http.request.Request;
import express.http.response.Response;

/**
 * Interface to handle an http-request.
 *
 * @author Simon Reinisch
 */
@FunctionalInterface
public interface HttpRequestHandler {

  /**
   * Handle an http-request
   *
   * @param req - The request object
   * @param res - The response object
   */
  void handle(Request req, Response res);
}
