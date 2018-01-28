package express.events;

import express.http.Request;
import express.http.Response;

public interface HttpRequest {

  /**
   * Handle an http-request
   *
   * @param req - The request object
   * @param res - The response object
   */
  void handle(Request req, Response res);
}
