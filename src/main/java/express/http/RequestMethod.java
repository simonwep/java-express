package express.http;

/**
 * Enum with basic request methods.
 *
 * @author Simon Reinisch
 */
public enum RequestMethod {

  // Real request methods
  GET("GET"),
  POST("POST"),
  PUT("PUT"),
  PATCH("PATCH"),
  DELETE("DELETE"),
  CONNECT("CONNECT"),
  OPTIONS("OPTIONS"),
  TRACE("TRACE"),
  HEAD("HEAD"),

  // Express specific method which will catch every method
  ALL("*");

  private String method;

  RequestMethod(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }
}
