package express.middleware;

import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Status;
import express.utils.Utils;

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
public final class FileProvider implements HttpRequestHandler {
    private final Logger logger;
    private FileProviderOptions options;
    private String root;

    {
        this.logger = Logger.getLogger(this.getClass().getSimpleName());
        this.logger.setUseParentHandlers(false);  // Disable default console log
    }

    FileProvider(String root, FileProviderOptions options) throws IOException {
        Path rootDir = Paths.get(root);

        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir))
            throw new IOException(rootDir + " does not exists or isn't an directory.");

        this.root = rootDir.toAbsolutePath().toString();
        this.options = options;
    }

    @Override
    public void handle(Request req, Response res) {
        String path = req.getURI().getPath();

        // Check context
        String context = req.getContext();
        if (path.indexOf(context) == 0) {
            path = path.substring(context.length());
        }

        // If the path is empty try index.html
        if (path.length() <= 1)
            path = "index.html";

        Path reqFile = Paths.get(root + "\\" + path);

        /*
         * If the file wasn't found, it will search in the target-directory for
         * the file by the raw-name without extension.
         */
        if (options.isFallBackSearching() && !Files.exists(reqFile) && !Files.isDirectory(reqFile)) {
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
                this.logger.log(Level.WARNING, "Cannot walk file tree.", e);
            }
        }

        if (Files.exists(reqFile) && Files.isRegularFile(reqFile)) {

            if (reqFile.getFileName().toString().charAt(0) == '.') {
                switch (options.getDotFiles()) {
                    case IGNORE:
                        res.setStatus(Status._404);
                        return;
                    case DENY:
                        res.setStatus(Status._403);
                        return;
                }
            }

            if (options.getExtensions() != null) {
                String reqEx = Utils.getExtension(reqFile);

                if (reqEx == null)
                    return;

                for (String ex : options.getExtensions()) {
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
        if (options.getHandler() != null)
            options.getHandler().handle(req, res);

        try {

            // Apply header
            if (options.isLastModified())
                res.setHeader("Last-Modified", Utils.getGMTDate(new Date(Files.getLastModifiedTime(file).toMillis())));
        } catch (IOException e) {
            res.sendStatus(Status._500);
            this.logger.log(Level.WARNING, "Cannot read LastModifiedTime from file " + file.toString(), e);
            return;
        }

        res.setHeader("Cache-Control", String.valueOf(options.getMaxAge()));
        res.send(file);
    }

    private String getBaseName(Path path) {
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        return index == -1 ? name : name.substring(0, index);
    }


    /**
     * Returns the logger which is concered for this FileProvilder object.
     * There is no default-handler active, if you want to log it you need to set an handler.
     *
     * @return The logger from this FileProvilder object.
     */
    public Logger getLogger() {
        return logger;
    }
}
