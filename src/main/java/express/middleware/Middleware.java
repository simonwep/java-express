package express.middleware;


import java.io.IOException;

/**
 * @author Simon Reinisch
 * Class which provides middleware
 */
public final class Middleware {

  // Don't allow instantiating this class
  private Middleware() {}

  /**
   * Create an new cookie-session middleware.
   * You can access and edit to session-cookie data via request.getMiddlewareContent('SessionCookie').
   *
   * @param cookieName An name for the session-cookie, it's recommend to use NOT SID for security reasons
   * @param maxAge     An max-age for the cookie
   */
  public static CookieSession cookieSession(String cookieName, long maxAge) {
    return new CookieSession(cookieName, maxAge);
  }

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, You can use <code>StaticOptions</code> to add som configurations.
   *
   * @param directoryPath The root directory
   */
  public static FileProvider statics(String directoryPath) throws IOException {
    return new FileProvider(directoryPath, new FileProviderOptions());
  }

  /**
   * This class serves an entire folder which can contains static file for your
   * web application, You can use <code>StaticOptions</code> to add som configurations.
   *
   * @param directoryPath The root directory
   * @param staticOptions Optional options for the file serving.
   */
  public static FileProvider statics(String directoryPath, FileProviderOptions staticOptions) throws IOException {
    return new FileProvider(directoryPath, staticOptions);
  }

}
