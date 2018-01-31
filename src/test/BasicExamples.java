package test;

import express.Express;
import express.http.Authorization;
import express.http.cookie.Cookie;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

public class BasicExamples {

  public static void main(String[] args) throws IOException {
    Express app = new Express();

    // Test case for url
    app.get("/", (req, res) -> res.send("Called /"));

    // Test case for url
    app.get("/user", (req, res) -> res.send("Called /user"));

    // Test case for url
    app.get("/user/bob", (req, res) -> res.send("Called /user/bob"));

    // Test case for url querying
    app.get("/getposts", (req, res) -> {
      String age = req.getQuery("age");
      String from = req.getQuery("from");
      res.send("Age: " + age + "\nFrom: " + from);
    });

    // Test case for param placeholder
    app.get("/hello/:username", (req, res) -> {
      String username = req.getParam("username");
      res.send("User " + username + " sad hello!");
    });

    // Test case for multiple param placeholder
    app.get("/hello/:username/:count", (req, res) -> {
      String username = req.getParam("username");
      String count = req.getParam("count");
      res.send("User " + username + " want to say " + count + " times hello!");
    });

    // Test case for cookie setting & multiple param placeholder
    app.get("/cookie/:name/:val", (req, res) -> {
      String name = req.getParam("name");
      String val = req.getParam("val");
      Cookie cookie = new Cookie(name, val);
      res.setCookie(cookie);
      res.send("ok");
    });

    // Test case for cookie reading
    app.get("/showcookies", (req, res) -> {
      StringBuffer buffer = new StringBuffer();
      req.getCookies().forEach((s, cookie) -> buffer.append(s).append(": ").append(cookie));
      res.send(buffer.toString());
    });

    // Test case for authorization
    app.get("/auth", (req, res) -> {
      if(req.hasAuthorization()){
        Authorization auth = req.getAuthorization();
        System.out.println("NAME: " + auth.getType());
        System.out.println("DATA: " + auth.getData());
      } else {
        System.out.println("No Authorisation");
      }
    });

    app.listen(() -> System.out.println("Express is listening!"));
  }

}
