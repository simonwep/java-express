package express.middleware;

import express.events.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Utils;

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
    String path = req.getURI().getPath();
    File reqFile = new File(path.equals("/") ? (PATH + "/index.html") : (PATH + path));

    if (reqFile.exists()) {

      if (EXTENSIONS != null) {
        String reqEx = Utils.getExtension(reqFile);

        for (String ex : EXTENSIONS) {
          if (reqEx.equals(ex)) {

            if (ONHANDLE != null) ONHANDLE.handle(req, res);
            res.send(reqFile);
            break;
          }
        }

      } else {
        if (ONHANDLE != null)
          ONHANDLE.handle(req, res);

        res.send(reqFile);
      }
    }
  }


}
