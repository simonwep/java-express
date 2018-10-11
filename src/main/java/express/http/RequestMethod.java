package express.http;

/**
 * @author Simon Reinisch
 * Enum with basic requestmethods.
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
