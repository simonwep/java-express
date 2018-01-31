package test;

import express.Express;
import express.http.cookie.Cookie;
import express.middleware.CookieSession;
import express.middleware.SessionCookie;
import express.middleware.Static;

import java.io.IOException;

public class MiddlewareExamples {

  public static void main(String[] args) throws IOException {
    Express app = new Express();

    // Test for static file service
    app.use(new Static("examplepath\\test_statics"));

    // Test for cookie session
    app.use(new CookieSession("ses", 9000));

    // Cookie session example
    app.get("/session", (req, res) -> {

      /**
       * Get the middleware object
       * Every middleware who extend the request is saved in an hashmap
       * with the name as key, so every middleware need to implement this interface
       * to extend the request.
       *
       * You can access your middlewares data over getMiddlewareContent("MIDDLEWARE NAME")
       */
      Object middlewareObject = req.getMiddlewareContent("SessionCookie");

      // Check if the middleware has set some data
      if (middlewareObject != null) {

        // Okay, now cast the object to an session cookie
        SessionCookie sessionCookie = (SessionCookie) middlewareObject;
        int count;

        // Check if the data is null, we want to implement an simple counter
        if (sessionCookie.getData() == null) {
          // Set the default data to 1 (first request with this session cookie)
          count = (Integer) sessionCookie.setData(1);
        } else {
          // Now we know that the cookie has an integer as data property, increase this
          count = (Integer) sessionCookie.setData((Integer) sessionCookie.getData() + 1);
        }

        // Send an happy message :D
        res.send("You take use of your session cookie " + count + " times :)");
      } else {
        res.send("No session cookie middleware used.");
      }
    });

    app.listen(() -> System.out.println("Express is listening!"));
  }

}
