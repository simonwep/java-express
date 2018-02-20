package express.middleware;


import java.io.IOException;

/**
 * @author Simon Reinisch
 * Class which provides middleware
 */
public class ExpressMiddleware {

  // Don't allow creating an instance from this class
  private ExpressMiddleware() { }

  /**
   * Create an new cookie-session middleware.
   * You can access and edit to session-cookie data via request.getMiddlewareContent('SessionCookie').
   *
   * @param cookieName An name for the session-cookie, it's recommed to use NOT SID for security reasons
   * @param maxAge     An maxage for the cookie
   */
  public static ExpressCookieSession cookieSession(String cookieName, long maxAge) {
    return new ExpressCookieSession(cookieName, maxAge);
  }

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, You can use <code>StaticOptions</code> to add som configurations.
   *
   * @param directoryPath The root directory
   */
  public static ExpressStatic statics(String directoryPath) throws IOException {
    return new ExpressStatic(directoryPath, new StaticOptions());
  }

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, You can use <code>StaticOptions</code> to add som configurations.
   *
   * @param directoryPath The root directory
   * @param staticOptions Optional options for the file serving.
   */
  public static ExpressStatic statics(String directoryPath, StaticOptions staticOptions) throws IOException {
    return new ExpressStatic(directoryPath, staticOptions);
  }

}
