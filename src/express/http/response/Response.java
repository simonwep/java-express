package express.http.response;

import com.sun.istack.internal.NotNull;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.http.Cookie;
import express.utils.MediaType;
import express.utils.Status;
import express.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Simon Reinisch
 * @implNote Core modul of express, don't change anything.
 */
public class Response {

  private final HttpExchange HTTP_EXCHANGE;
  private final OutputStream BODY;
  private final Headers HEADER;

  private String contentType;
  private boolean isClose;
  private long contentLength;
  private int status;

  {
    // Initialize with default data
    contentType = MediaType._txt.getMIME();
    isClose = false;
    contentLength = 0;
    status = 200;
  }

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
    byte[] data = s.getBytes();

    this.contentLength = data.length;
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
    try {
      this.contentLength = file.length();

      // Detect content type
      MediaType mediaType = Utils.getContentType(file);
      this.contentType = mediaType == null ? null : mediaType.getMIME();

      // Send header
      sendHeaders();

      // Send file
      FileInputStream fis = new FileInputStream(file);
      byte[] buffer = new byte[1024];
      int n;
      while ((n = fis.read(buffer)) != -1) {
        this.BODY.write(buffer, 0, n);
      }

      fis.close();

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
      String contentType = getContentType() == null ? MediaType._bin.getExtension() : getContentType();

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
