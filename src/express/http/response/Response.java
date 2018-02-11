package express.http.response;

import com.sun.istack.internal.NotNull;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.http.Cookie;
import express.utils.MediaType;
import express.utils.Status;
import express.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * @author Simon Reinisch
 * @implNote Core modul of express, don't change anything.
 */
public class Response {

  private final HttpExchange HTTP_EXCHANGE;
  private final OutputStream BODY;
  private final Headers HEADER;

  private MediaType contentType = MediaType._txt;
  private boolean isClose = false;
  private long contentLength = 0;
  private int status = 200;

  public Response(HttpExchange exchange) {
    this.HTTP_EXCHANGE = exchange;
    this.HEADER = exchange.getResponseHeaders();
    this.BODY = exchange.getResponseBody();
  }

  /**
   * Add an specific value to the reponse header.
   *
   * @param key   The header name.
   * @param value The header value.
   * @return This Response instance.
   */
  public Response setHeader(String key, String value) {
    HEADER.add(key, value);
    return this;
  }

  /**
   * @param key The header key.
   * @return The values which are associated with this key.
   */
  public List<String> getHeader(String key) {
    return HEADER.get(key);
  }

  /**
   * Sets the response Location HTTP header to the specified path parameter.
   *
   * @param location The location.
   */
  public void setLocation(String location) {
    HEADER.add("Location", location);
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
    if (checkIfClosed()) return this;
    this.HEADER.add("Set-Cookie", cookie.toString());
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
  public Response setStatus(@NotNull Status status) {
    if (checkIfClosed()) return this;
    this.status = status.getCode();
    return this;
  }

  /**
   * Set the response-status and send the response.
   *
   * @param status The response status.
   */
  public void sendStatus(@NotNull Status status) {
    if (checkIfClosed()) return;
    this.status = status.getCode();
    send();
  }

  /**
   * @return The current contentType
   */
  public MediaType getContentType() {
    return contentType;
  }

  /**
   * Set the contentType for this response.
   *
   * @param contentType - The contentType
   */
  public void setContentType(MediaType contentType) {
    this.contentType = contentType;
  }

  /**
   * Send an empty response (Content-Length = 0)
   */
  public void send() {
    if (checkIfClosed()) return;
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
    if (checkIfClosed()) return;
    this.contentLength += s.length();
    sendHeaders();

    try {
      this.BODY.write(s.getBytes());
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }

    close();
  }

  /**
   * Send an entire file as response
   * The mime type will be automatically detected.
   *
   * @param file The file.
   */
  public void send(@NotNull File file) {
    if (checkIfClosed()) return;
    this.contentLength += file.length();
    this.contentType = Utils.getContentType(file);
    sendHeaders();

    try {
      byte[] bytes = Files.readAllBytes(file.toPath());
      this.BODY.write(bytes);
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }

    close();
  }

  /**
   * @return If the response is already closed (headers are send).
   */
  public boolean isClosed() {
    return this.isClose;
  }

  private boolean checkIfClosed() {
    if (isClose) {
      System.err.println("Response is already closed.");
      return true;
    }
    return false;
  }

  private void sendHeaders() {
    try {

      // Fallback
      String contentType = getContentType().getExtension() == null ? MediaType._bin.getExtension() : getContentType().getMIME();

      // Set header and send response
      this.HEADER.set("Content-Type", contentType);
      this.HTTP_EXCHANGE.sendResponseHeaders(status, contentLength);
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }
  }

  private void close() {
    try {
      this.BODY.close();
      this.isClose = true;
    } catch (IOException e) {
      // TODO: Handle error
      e.printStackTrace();
    }
  }

}
