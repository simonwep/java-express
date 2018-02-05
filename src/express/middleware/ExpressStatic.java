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

  private String PATH;
  private String[] EXTENSIONS;
  private HttpRequest ONHANDLE;

  ExpressStatic(Object... params) {
    for (Object o : params) {
      if (o instanceof String[]) this.EXTENSIONS = (String[]) o;
      else if (o instanceof String) this.PATH = (String) o;
      else if (o instanceof HttpRequest) this.ONHANDLE = (HttpRequest) o;
    }
  }

  @Override
  public void handle(Request req, Response res) {
    File reqFile = new File(PATH + req.getURI().getPath());

    if (reqFile.exists()) {

      if (EXTENSIONS != null) {
        String reqEx = reqFile.getAbsolutePath().replaceAll("^(.*\\.|.*\\|.+$)", "");

        for (String ex : EXTENSIONS) {
          if (reqEx.equals(ex)) {

            if (ONHANDLE != null) ONHANDLE.handle(req, res);
            res.send(reqFile);
            break;
          }
        }

      } else {
        if (ONHANDLE != null) ONHANDLE.handle(req, res);
        res.send(reqFile);
      }
    }
  }


}
