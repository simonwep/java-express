package express.middleware;

import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Status;
import express.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author Simon Reinisch
 * An middleware to provide access to static server-files.
 */
public final class FileProvider implements HttpRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(FileProvider.class);

    private FileProviderOptions options;
    private String root;

    FileProvider(String root, FileProviderOptions options) throws IOException {
        Path rootDir = Paths.get(root);

        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
            throw new IOException(rootDir + " does not exists or isn't a directory.");
        }

        this.root = rootDir.toAbsolutePath().toString();
        this.options = options;
    }

    @Override
    public void handle(Request req, Response res) {
        try {
            String path = req.getURI().getPath();

            // Check context
            String context = req.getContext();
            if (path.indexOf(context) == 0) {
                path = path.substring(context.length());
            }

            // If the path is empty try index.html
            if (path.length() <= 1) {
                path = "index.html";
            }

            Path reqFile = Paths.get(root + File.separator + path);

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

                        Optional<Path> founded = Files.walk(parent)
                                .filter(sub -> getBaseName(sub).equals(name))
                                .findFirst();

                        if (founded.isPresent()) {
                            reqFile = founded.get();
                        }
                    }
                } catch (IOException e) {
                    log.error("Cannot walk file tree.", e);
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

                // Check if extension is present
                if (options.getExtensions() != null) {
                    String reqEx = Utils.getExtension(reqFile);

                    if (reqEx == null) {
                        return;
                    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finish(Path file, Request req, Response res) {
        if (options.getHandler() != null) {
            options.getHandler().handle(req, res);
        }

        try {

            // Apply header
            if (options.isLastModified()) {
                Instant instant = Instant.ofEpochMilli(Files.getLastModifiedTime(file).toMillis());
                DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME
                        .withZone(ZoneOffset.UTC);

                res.setHeader("Last-Modified", formatter.format(instant));
            }

        } catch (IOException e) {
            res.sendStatus(Status._500);
            log.error("Cannot read LastModifiedTime from file [{}]", file.toString(), e);
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

}
