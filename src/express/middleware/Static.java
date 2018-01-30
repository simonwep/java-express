package express.middleware;

import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;
import express.utils.MIMETypes;

import java.io.File;

/**
 * @author Simon Reinisch
 * <p>
 * An Express-Middleware to serve static files.
 */
public class Static implements HttpRequest {

  private final String PATH;

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, it automatically detect the content type and will send it to
   * the Client.
   * <p>
   * To use it simply put it in the <code>app.use()</code> method!
   *
   * @param directoryPath The root directory
   */
  public Static(String directoryPath) {
    this.PATH = directoryPath;
  }

  @Override
  public void handle(Request req, Response res) {
    File reqFile = new File(PATH + req.getURI().getPath());

    if (reqFile.exists()) {
      String extension = reqFile.getAbsolutePath().replaceAll("^(.*\\.|.*\\\\|.+$)", "");
      String contentType = getContentType(extension);
      res.send(reqFile, contentType);
    }
  }

  /**
   * Returns the MIME-Type of an filename.
   *
   * @param fileExtension The file extension.
   * @return The MIME-Type.
   */
  private static String getContentType(String fileExtension) {
    String ct = MIMETypes.get().get(fileExtension);

    if (ct == null)
      ct = "text/plain";

    return ct;
  }
}
