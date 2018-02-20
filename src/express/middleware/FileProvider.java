package express.middleware;

import express.http.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Status;
import express.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author Simon Reinisch
 * An middleware to provide access to static server-files.
 */
final class FileProvider implements HttpRequest {

  private FileProviderOptions OPTIONS;
  private String ROOT;

  FileProvider(String root, FileProviderOptions options) throws IOException {
    File rootDir = new File(root);

    if (!rootDir.exists() || !rootDir.isDirectory())
      throw new IOException(rootDir + " does not exists or isn't an directory.");

    this.ROOT = rootDir.getAbsolutePath();
    this.OPTIONS = options;
  }

  @Override
  public void handle(Request req, Response res) {
    String path = req.getURI().getPath();
    File reqFile = new File(ROOT + "\\" + path);

    /*
     * If the file wasn't found, it will search in the target-directory for
     * the file by the raw-name without extension.
     */
    if (OPTIONS.isFallBackSearching() && !reqFile.exists() || !reqFile.isFile()) {
      File dir = new File(ROOT + "\\" + getURIPath(path));
      String name = getURIFilename(path);
      reqFile = getFirst(dir, name);
    }

    if (reqFile != null && reqFile.exists()) {

      if (reqFile.getName().charAt(0) == '.') {
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

  private void finish(File file, Request req, Response res) {
    if (OPTIONS.getHandler() != null)
      OPTIONS.getHandler().handle(req, res);


    // Apply header
    if (OPTIONS.isLastModified())
      res.setHeader("Last-Modified", Utils.getGMTDate(new Date(file.lastModified())));

    res.setHeader("Cache-Control", String.valueOf(OPTIONS.getMaxAge()));
    res.send(file);
  }

  /**
   * Returns the first file which has this specific name.
   * If no file where found, null will be returned.
   *
   * @param dir  The root directory.
   * @param name The target name.
   * @return The first file which will be found, null otherwise.
   */
  private File getFirst(File dir, String name) {
    File[] fs = dir.listFiles();

    if (fs == null)
      return null;

    for (File f : fs) {
      if (f.isFile() && getBaseName(f.getName()).equals(name)) {
        return f;
      }
    }

    return null;
  }

  private String getBaseName(String path) {
    int index = path.lastIndexOf('.');
    return index == -1 ? path : path.substring(0, index);
  }

  /**
   * Extract the filename from an URI-Path.
   *
   * @param path The path.
   * @return The file name.
   */
  private String getURIFilename(String path) {
    int s = path.lastIndexOf('/');
    int e = path.lastIndexOf('.');

    if (e == -1 && s == -1)
      return "index";

    if (e == -1)
      e = path.length();

    if (s == -1)
      s = 0;

    String name = path.substring(s + 1, e);

    return name.isEmpty() ? "index" : name;
  }

  /**
   * Extract the directory path from an URI-Path.
   *
   * @param path The path
   * @return The path (without file name)
   */
  private String getURIPath(String path) {
    int s = path.indexOf('/');
    int e = path.lastIndexOf('/');

    if (e == s || s == -1 || e == -1)
      return "";

    return path.substring(s, e);
  }
}
