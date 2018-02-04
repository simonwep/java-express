package express.middleware;

import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;

import java.io.File;

/**
 * @author Simon Reinisch
 * <p>
 * An Express-Middleware to serve static files.
 */
final class ExpressStatic implements HttpRequest {

  private final String PATH;

  ExpressStatic(String directoryPath) {
    this.PATH = directoryPath;
  }

  @Override
  public void handle(Request req, Response res) {
    File reqFile = new File(PATH + req.getURI().getPath());

    if (reqFile.exists())
      res.send(reqFile);
  }


}
