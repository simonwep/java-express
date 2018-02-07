package express.middleware;

import express.utils.Utils;
import express.events.HttpRequest;
import express.expressfilter.ExpressFilter;
import express.expressfilter.ExpressFilterTask;
import express.http.request.Request;
import express.http.response.Response;
import express.http.Cookie;
import express.http.SessionCookie;

import java.util.concurrent.ConcurrentHashMap;

final class ExpressCookieSession implements HttpRequest, ExpressFilter, ExpressFilterTask {

  private final static String MIDDLEWARE_NAME = "sessioncookie";

  private final ConcurrentHashMap<String, SessionCookie> COOKIES = new ConcurrentHashMap<>();
  private final String COOKIE_NAME;
  private final long MAX_AGE;

  ExpressCookieSession(String cookieName, long maxAge) {
    this.COOKIE_NAME = cookieName;
    this.MAX_AGE = maxAge;
  }

  @Override
  public void handle(Request req, Response res) {
    Cookie cookie = req.getCookie(COOKIE_NAME);

    if (cookie != null && COOKIES.containsKey(cookie.getValue())) {

      req.addMiddlewareContent(this, COOKIES.get(cookie.getValue()));
    } else {
      String token = Utils.randomToken(32, 16);

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
    COOKIES.clear();
  }

  @Override
  public long getDelay() {
    return 60000;
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
