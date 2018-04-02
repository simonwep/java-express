package express.middleware;

import express.http.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Status;
import express.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simon Reinisch
 * An middleware to provide access to static server-files.
 */
final class FileProvider implements HttpRequest {

  private final Logger LOGGER;
  private FileProviderOptions OPTIONS;
  private String ROOT;

  FileProvider(String root, FileProviderOptions options) throws IOException {
    File rootDir = new File(root);

    if (!rootDir.exists() || !rootDir.isDirectory())
      throw new IOException(rootDir + " does not exists or isn't an directory.");

    this.ROOT = rootDir.getAbsolutePath();
    this.OPTIONS = options;
    this.LOGGER = Logger.getLogger(this.getClass().getSimpleName());
  }

  @Override
  public void handle(Request req, Response res) {
    String path = req.getURI().getPath();

    if (path.length() <= 1)
      path = "index.html";

    Path reqFile = Paths.get(ROOT + "\\" + path);

    /*
     * If the file wasn't found, it will search in the target-directory for
     * the file by the raw-name without extension.
     */
    if (OPTIONS.isFallBackSearching() && !Files.exists(reqFile) && !Files.isDirectory(reqFile)) {
      String name = reqFile.getFileName().toString();

      try {
        Path parent = reqFile.getParent();

        // Check if reading is allowed
        if (Files.isReadable(parent)) {

          Optional<Path> founded = Files.walk(parent).filter(sub -> getBaseName(sub).equals(name)).findFirst();

          if (founded.isPresent())
            reqFile = founded.get();
        }
      } catch (IOException e) {
        this.LOGGER.log(Level.WARNING, "Cannot walk file tree.", e);
      }
    }

    if (Files.exists(reqFile) && Files.isRegularFile(reqFile)) {

      if (reqFile.getFileName().toString().charAt(0) == '.') {
        switch (OPTIONS.getDotFiles()) {
          case IGNORE:
            res.setStatus(Status._404);
            return;
          case DENY:
            res.setStatus(Status._403);
            return;
        }
      }

      if (OPTIONS.getExtensions() != null) {
        String reqEx = Utils.getExtension(reqFile);

        if (reqEx == null)
          return;

        for (String ex : OPTIONS.getExtensions()) {
          if (reqEx.equals(ex)) {
            finish(reqFile, req, res);
            break;
          }
        }

        res.setStatus(Status._403);
      } else {
        finish(reqFile, req, res);
      }
    }
  }

  private void finish(Path file, Request req, Response res) {
    if (OPTIONS.getHandler() != null)
      OPTIONS.getHandler().handle(req, res);

    try {

      // Apply header
      if (OPTIONS.isLastModified())
        res.setHeader("Last-Modified", Utils.getGMTDate(new Date(Files.getLastModifiedTime(file).toMillis())));
    } catch (IOException e) {
      res.sendStatus(Status._500);
      this.LOGGER.log(Level.WARNING, "Cannot read LastModifiedTime from file " + file.toString(), e);
      return;
    }

    res.setHeader("Cache-Control", String.valueOf(OPTIONS.getMaxAge()));
    res.send(file);
  }

  private String getBaseName(Path path) {
    String name = path.getFileName().toString();
    int index = name.lastIndexOf('.');
    return index == -1 ? name : name.substring(0, index);
  }

  /**
   * @return The logger from this FileProvider instance.
   */
  public Logger getLogger() {
    return LOGGER;
  }
}
