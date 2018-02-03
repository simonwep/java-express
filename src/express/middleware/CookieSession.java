package express.middleware;

import express.ExpressUtils;
import express.events.HttpRequest;
import express.expressfilter.ExpressFilter;
import express.expressfilter.ExpressFilterTask;
import express.http.Request;
import express.http.Response;
import express.http.cookie.Cookie;

import java.util.concurrent.ConcurrentHashMap;

public class CookieSession implements HttpRequest, ExpressFilter, ExpressFilterTask {

  private final static String MIDDLEWARE_NAME = "SessionCookie";

  private final ConcurrentHashMap<String, SessionCookie> COOKIES = new ConcurrentHashMap<>();
  private final String COOKIE_NAME;
  private final long MAX_AGE;

  /**
   * Create an new cookie-session middleware.
   * You can access and edit to session-cookie data via request.getMiddlewareContent('SessionCookie').
   *
   * @param cookieName An name for the session-cookie, it's recommed to use NOT SID for security reasons
   * @param maxAge     An maxage for the cookie
   */
  public CookieSession(String cookieName, long maxAge) {
    this.COOKIE_NAME = cookieName;
    this.MAX_AGE = maxAge;
  }

  @Override
  public void handle(Request req, Response res) {
    Cookie cookie = req.getCookie(COOKIE_NAME);

    if (cookie != null && COOKIES.containsKey(cookie.getValue())) {

      req.addMiddlewareContent(this, COOKIES.get(cookie.getValue()));
    } else {
      String token = ExpressUtils.randomToken(32, 16);

      cookie = new Cookie(COOKIE_NAME, token).setMaxAge(MAX_AGE);
      res.setCookie(cookie);

      SessionCookie sessionCookie = new SessionCookie(MAX_AGE);
      COOKIES.put(token, sessionCookie);

      req.addMiddlewareContent(this, sessionCookie);
    }
  }

  @Override
  public String getName() {
    return MIDDLEWARE_NAME;
  }

  @Override
  public void onStart() {
    // Nothing to initialize
  }

  @Override
  public void onStop() {
    // Nothing to do
  }

  @Override
  public long getDelay() {
    return 15000; // 1min
  }

  @Override
  public void onUpdate() {
    long current = System.currentTimeMillis();

    COOKIES.forEach((s, o) -> {
      if (current > o.getCreated() + o.getMaxAge())
        COOKIES.remove(s);
    });
  }

}
