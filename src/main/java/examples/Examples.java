package examples;

import express.Express;
import express.http.Cookie;
import express.http.SessionCookie;
import express.middleware.Middleware;

import java.io.IOException;

public class Examples {

  public static void main(String[] args) throws IOException {
    Express app = new Express();

    app.get("/", (req, res) -> res.send("Hello World"))
    
    .get("/posts", (req, res) -> {
      String page = req.getQuery("page"); // Contains '12'
      String from = req.getQuery("from"); // Contains 'John'
      res.send("Page: " + page + ", from: " + from); // Send: "Page: 12, from: John"
    })

    .get("/posts/:user/:type", (req, res) -> {
      String user = req.getParam("user"); // Contains 'john'
      String type = req.getParam("type"); // Contains 'all'
      res.send("User: " + user + ", type: " + type); // Send: "User: john, type: all"
    })

    .get("/setcookie", (req, res) -> {
      Cookie cookie = new Cookie("username", "john");
      res.setCookie(cookie);
      res.send("Cookie has been set!");
    })

    .get("/showcookie", (req, res) -> {
      Cookie cookie = req.getCookie("username");
      String username = cookie.getValue();
      res.send("The username is: " + username); // Prints "The username is: john"
    })

    .post("/register", (req, res) -> {
      String email = req.getFormQuery("email");
      String username = req.getFormQuery("username");
      // Process data

      // Prints "E-Mail: john@gmail.com, Username: john"
      res.send("E-Mail: " + email + ", Username: " + username);
    })

    .get("/res", (req, res) -> {
      // res.send();                     // Send empty response
      // res.send("Hello World");        // Send an string
      // res.send("chart.pdf");          // Send an file
      // res.setStatus(200);             // Set the response status
      // res.getStatus();                // Returns the current response status
      // res.setCookie(new Cookie(...)); // Send an cookie
      // res.isClosed();                 // Check if already something has been send to the client
    })

    .get("/req/", (req, res) -> {
      // req.getURI();                        // Request URI
      // req.getHost();                       // Request host (mostly localhost)
      // req.getMethod();                     // Request method (here GET)
      // req.getContentType();                // Request content type, is here null because it's an GET request
      // req.getBody();                       // Request body inputstream
      // req.getUserAgent();                  // Request user-agent
      // req.getParam("parameter");           // Returns an url parameter
      // req.getQuery("queryname");           // Returns an url query by key
      // req.getFormQuery("formqueryname");   // Returns an form input value
      // req.getFormQuerys();                 // Returns all form querys
      // req.getCookie("user");               // Returns an cookie by name
      // req.getCookies();                    // Returns all cookies
      // req.hasAuthorization();              // Check if the request contains an authorization header
      // req.getAuthorization();              // Returns the authorization header
      // req.getMiddlewareContent("name");    // Returns data from middleware
      // req.pipe(new OutputStream() {...});  // Pipe the body to an outputstream
    })

    .use(Middleware.cookieSession("f3v4", 9000))

    .get("/session", (req, res) -> {

      /*
       * CookieSession named its data "SessionCookie" which is
       * an SessionCookie so we can Cast it.
       */
      SessionCookie sessionCookie = (SessionCookie) req.getMiddlewareContent("SessionCookie");
      int count;

      // Check if the data is null, we want to implement an simple counter
      if (sessionCookie.getData() == null) {

        // Set the default data to 1 (first request with this session cookie)
        count = (int) sessionCookie.setData(1);
      } else {

        // Now we know that the cookie has an integer as data property, increase it
        count = (int) sessionCookie.setData((int) sessionCookie.getData() + 1);
      }

      // Send an info message
      res.send("You take use of your session cookie " + count + " times.");
    })

    .listen(() -> System.out.println("Express is listening!"));
  }

}
