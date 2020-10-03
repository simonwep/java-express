package express.http.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.http.Cookie;
import express.utils.MediaType;
import express.utils.Status;
import express.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * @author Simon Reinisch
 * Class for an http-response.
 */
public class Response {

    private static final Logger log = LoggerFactory.getLogger(Response.class);

    private final HttpExchange httpExchange;
    private final OutputStream body;
    private final Headers headers;

    private String contentType = MediaType._txt.getMIME();
    private boolean isClose = false;
    private long contentLength = 0;
    private int status = 200;

    public Response(HttpExchange exchange) {
        this.httpExchange = exchange;
        this.headers = exchange.getResponseHeaders();
        this.body = exchange.getResponseBody();
    }

    /**
     * Add an specific value to the reponse header.
     *
     * @param key   The header name.
     * @param value The header value.
     * @return This Response instance.
     */
    public Response setHeader(String key, String value) {
        headers.add(key, value);
        return this;
    }

    /**
     * @param key The header key.
     * @return The values which are associated with this key.
     */
    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Sets the response Location HTTP header to the specified path parameter.
     *
     * @param location The location.
     */
    public void redirect(String location) {
        headers.add("Location", location);
        setStatus(Status._302);
        send();
    }

    /**
     * Set an cookie.
     *
     * @param cookie The cookie.
     * @return This Response instance.
     */
    public Response setCookie(Cookie cookie) {
        if (isClosed()) return this;
        this.headers.add("Set-Cookie", cookie.toString());
        return this;
    }

    /**
     * @return Current response status.
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Set the response-status.
     * Default is 200 (ok).
     *
     * @param status The response status.
     * @return This Response instance.
     */
    public Response setStatus(Status status) {
        if (isClosed()) return this;
        this.status = status.getCode();
        return this;
    }

    /**
     * Set the response-status and send the response.
     *
     * @param status The response status.
     */
    public void sendStatus(Status status) {
        if (isClosed()) return;
        this.status = status.getCode();
        send();
    }

    /**
     * @return The current contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the contentType for this response.
     *
     * @param contentType - The contentType
     */
    public void setContentType(MediaType contentType) {
        this.contentType = contentType.getMIME();
    }

    /**
     * Set the contentType for this response.
     *
     * @param contentType - The contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Send an empty response (Content-Length = 0)
     */
    public void send() {
        if (isClosed()) return;
        this.contentLength = 0;
        sendHeaders();
        close();
    }

    /**
     * Send an string as response.
     *
     * @param s The string.
     */
    public void send(String s) {
        if (s == null) {
            send();
            return;
        }

        if (isClosed()) return;
        byte[] data = s.getBytes();

        this.contentLength = data.length;
        sendHeaders();

        try {
            this.body.write(s.getBytes());
        } catch (IOException e) {
            log.error("Failed to write char sequence to client.",e );
        }

        close();
    }

    /**
     * Sets the 'Content-Disposition' header to 'attachment' and his
     * Content-Disposition "filename=" parameter to the file name.
     * Normally this triggers an download event client-side.
     *
     * @param file The file which will be send as attachment.
     * @return True if the file was successfully send, false if the file doesn't exists or the respose is already closed.
     */
    public boolean sendAttachment(Path file) {
        if (isClosed() || !Files.isRegularFile(file)) {
            return false;
        }

        String dispo = "attachment; filename=\"" + file.getFileName() + "\"";
        setHeader("Content-Disposition", dispo);

        return send(file);
    }

    /**
     * Send an entire file as response
     * The mime type will be automatically detected.
     *
     * @param file The file.
     * @return True if the file was successfully send, false if the file doesn't exists or the respose is already closed.
     */
    public boolean send(Path file) {

        if (isClosed() || !Files.isRegularFile(file)) {
            return false;
        }

        try {
            this.contentLength = Files.size(file);

            // Detect content type
            MediaType mediaType = Utils.getContentType(file);
            this.contentType = mediaType == null ? null : mediaType.getMIME();

            // Send header
            sendHeaders();

            // Send file
            InputStream fis = Files.newInputStream(file, StandardOpenOption.READ);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                this.body.write(buffer, 0, n);
            }

            fis.close();

        } catch (IOException e) {
            log.error("Failed to pipe file to output stream.", e);
            return false;
        } finally {
            close();
        }

        return true;
    }

    /**
     * Send a byte array as response. Content type will be
     * set to application/octet-streamFrom
     *
     * @param bytes Byte array
     * @return If operation was successful
     */
    public boolean sendBytes(byte[] bytes) {

        if (isClosed() || bytes == null) {
            return false;
        }

        try {
            this.contentLength = bytes.length;

            // Set content type to octet streamFrom
            this.contentType = MediaType._bin.getMIME();

            // Send header
            sendHeaders();

            // Write bytes to body
            this.body.write(bytes);
        } catch (IOException e) {
            log.error("Failed to pipe file to output stream.", e);
            return false;
        } finally {
            close();
        }

        return true;
    }

    /**
     * Streams an input stream to the client.
     * Requires a contentLength as well as a MediaType
     *
     * @param contentLength Total size
     * @param is            Inputstream
     * @param mediaType     Stream type
     * @return If operation was successful
     */
    public boolean streamFrom(long contentLength, InputStream is, MediaType mediaType) {

        if (isClosed() || is == null) {
            return false;
        }

        try {
            this.contentLength = contentLength;

            // Set content type to octet-stream
            this.contentType = mediaType.getMIME();

            // Send header
            sendHeaders();

            // Write bytes to body
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                this.body.write(buffer, 0, n);
            }

            is.close();
        } catch (IOException e) {
            log.error("Failed to pipe file to output stream.", e);
            return false;
        } finally {
            close();
        }

        return true;
    }

    /**
     * @return If the response is already closed (headers have been sent).
     */
    public boolean isClosed() {
        return this.isClose;
    }

    private void sendHeaders() {
        try {

            // Fallback
            String contentType = getContentType() == null ? MediaType._bin.getExtension() : getContentType();

            // Set header and send response
            this.headers.set("Content-Type", contentType);
            this.httpExchange.sendResponseHeaders(status, contentLength);
        } catch (IOException e) {
            log.error("Failed to send headers.", e);
        }
    }

    private void close() {
        try {
            this.body.close();
            this.isClose = true;
        } catch (IOException e) {
            log.error("Failed to close output stream.", e);
        }
    }

}
