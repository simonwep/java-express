package test;

import express.Express;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException{
    Express express = new Express();

    express.get("/user", (req, res) -> {
      res.send("User Hello");
    });

    express.get("/sayhello/bob", (req, res) -> {
      res.send("User bob has said hello lol");
    });

    express.get("/sayhello/:username", (req, res) -> {
      String username = req.getParam("username");
      res.send("User " + username + " Say hello!");
    });

    express.get("/sayhello/:username/:count", (req, res) -> {
      String username = req.getParam("username");
      String count = req.getParam("count");
      res.send("User " + username + " want to say " + count + " times hello!");
    });


    express.listen(() -> {
      System.out.println("Server stardet!");
    });
  }

}
