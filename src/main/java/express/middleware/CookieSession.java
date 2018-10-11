package express.middleware;

import express.filter.Filter;
import express.filter.FilterTask;
import express.http.Cookie;
import express.http.HttpRequestHandler;
import express.http.SessionCookie;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Simon Reinisch
 * An middleware to create cookie-sessions.
 */
final class CookieSession implements HttpRequestHandler, Filter, FilterTask {

    private final static String MIDDLEWARE_NAME = "sessioncookie";

    private final ConcurrentHashMap<String, SessionCookie> cookies = new ConcurrentHashMap<>();
    private final String cookieName;
    private final long maxAge;

    CookieSession(String cookieName, long maxAge) {
        this.cookieName = cookieName;
        this.maxAge = maxAge;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void handle(Request req, Response res) {
        Cookie cookie = req.getCookie(cookieName);

        if (cookie != null && cookies.containsKey(cookie.getValue())) {
            req.addMiddlewareContent(this, cookies.get(cookie.getValue()));
        } else {
            String token;

            do {
                token = Utils.randomToken(32, 16);
            } while (cookies.contains(token));

            cookie = new Cookie(cookieName, token).setMaxAge(maxAge);
            res.setCookie(cookie);

            SessionCookie sessionCookie = new SessionCookie(maxAge);
            cookies.put(token, sessionCookie);

            req.addMiddlewareContent(this, sessionCookie);
        }
    }

    @Override
    public String getName() {
        return MIDDLEWARE_NAME;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
        cookies.clear();
    }

    @Override
    public long getDelay() {
        return 60000;
    }

    @Override
    public void onUpdate() {
        long current = System.currentTimeMillis();

        cookies.forEach((cookieHash, cookie) -> {
            if (current > cookie.getCreated() + cookie.getMaxAge()) {
                cookies.remove(cookieHash);
            }
        });
    }

}
