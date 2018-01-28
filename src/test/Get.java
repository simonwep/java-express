package test;

import express.Express;
import express.cookie.Cookie;

import java.io.IOException;
import java.util.HashMap;

public class Get {

  public static void main(String[] args) throws IOException {
    Express express = new Express();

    express.get("/", (req, res) -> res.send("Called /"));

    express.get("/user", (req, res) -> res.send("Called /user"));

    express.get("/user/bob", (req, res) -> res.send("Called /user/bob"));

    express.get("/hello/:username", (req, res) -> {
      String username = req.getParam("username");
      res.send("User " + username + " sad hello!");
    });

    express.get("/hello/:username/:count", (req, res) -> {
      String username = req.getParam("username");
      String count = req.getParam("count");
      res.send("User " + username + " want to say " + count + " times hello!");
    });

    express.get("/cookie/:name/:val", (req, res) -> {
      String name = req.getParam("name");
      String val = req.getParam("val");
      Cookie cookie = new Cookie(name, val);
      res.setCookie(cookie);
      res.send("ok");
    });

    express.get("/showcookies", (req, res) -> {
      HashMap<String, Cookie> cookies = req.getCookies();
      StringBuffer buffer = new StringBuffer();
      cookies.forEach((s, cookie) -> buffer.append(s).append(": ").append(cookie));
      res.send(buffer.toString());
    });

    express.listen(() -> System.out.println("Server stardet!"));
  }

}
