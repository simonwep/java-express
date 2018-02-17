package express.middleware;

import express.http.HttpRequest;

/**
 * @author Simon Reinisch
 * <p>
 * Class which serves middleware
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
   * web application, it automatically detect the content type and will send it to
   * the Client.
   * <p>
   * To use it simply put it in the <code>app.use()</code> method!
   *
   * @param directoryPath The root directory
   */
  public static ExpressStatic statics(String directoryPath) {
    return new ExpressStatic(directoryPath);
  }

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, it automatically detect the content type and will send it to
   * the Client.
   * <p>
   * To use it simply put it in the <code>app.use()</code> method!
   *
   * @param directoryPath The root directory
   * @param extensions    The allowed extensions
   */
  public static ExpressStatic statics(String directoryPath, String... extensions) {
    return new ExpressStatic(directoryPath, extensions);
  }

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, it automatically detect the content type and will send it to
   * the Client.
   * <p>
   * To use it simply put it in the <code>app.use()</code> method!
   *
   * @param directoryPath The root directory
   * @param middleware    An handler which will be fired BEFORE the files will be served
   * @param extensions    The allowed extensions
   */
  public static ExpressStatic statics(String directoryPath, HttpRequest middleware, String... extensions) {
    return new ExpressStatic(directoryPath, extensions, middleware);
  }
}
