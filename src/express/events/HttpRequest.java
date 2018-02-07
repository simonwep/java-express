package express.events;

import express.http.request.Request;
import express.http.response.Response;

/**
 * @author Simon Reinisch
 */
public interface HttpRequest {

  /**
   * Handle an http-request
   *
   * @param req - The request object
   * @param res - The response object
   */
  void handle(Request req, Response res);
}
