package express.http.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.http.Cookie;
import express.utils.MediaType;
import express.utils.Status;
import express.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simon Reinisch
 * Class for an http-response.
 */
public class Response {

  private final HttpExchange httpExchange;
  private final OutputStream body;
  private final Headers headers;
  private final Logger logger;

  private String contentType;
  private boolean isClose;
  private long contentLength;
  private int status;

  {
    // Initialize with default data
    this.contentType = MediaType._txt.getMIME();
    this.isClose = false;
    this.contentLength = 0;
    this.status = 200;
    this.logger = Logger.getLogger(getClass().getSimpleName());
    this.logger.setUseParentHandlers(false); // Disable default console log
  }

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
      logger.log(Level.INFO, "Failed to write charsequence to client.", e);
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
    if (isClosed() || !Files.isRegularFile(file))
      return false;

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
    if (isClosed() || !Files.isRegularFile(file))
      return false;

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
      logger.log(Level.INFO, "Failed to pipe file to outputstream.", e);
      close();
      return false;
    }

    close();
    return true;
  }

  /**
   * @return If the response is already closed (headers are send).
   */
  public boolean isClosed() {
    return this.isClose;
  }

  /**
   * Returns the logger which is concered for this Response object.
   * There is no default-handler active, if you want to log it you need to set an handler.
   *
   * @return The logger from this Response object.
   */
  public Logger getLogger() {
    return logger;
  }

  private void sendHeaders() {
    try {

      // Fallback
      String contentType = getContentType() == null ? MediaType._bin.getExtension() : getContentType();

      // Set header and send response
      this.headers.set("Content-Type", contentType);
      this.httpExchange.sendResponseHeaders(status, contentLength);
    } catch (IOException e) {
      logger.log(Level.INFO, "Failed to send headers.", e);
    }
  }

  private void close() {
    try {
      this.body.close();
      this.isClose = true;
    } catch (IOException e) {
      logger.log(Level.INFO, "Failed to close outputstream.", e);
    }
  }

}
