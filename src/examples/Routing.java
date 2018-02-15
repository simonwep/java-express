package examples;

import express.Express;
import express.ExpressRouter;

public class Routing {

  public static void main(String[] args) throws Throwable {
    Express app = new Express();

    // Define router for index sites
    ExpressRouter indexRouter = new ExpressRouter();
    indexRouter.get("/", (req, res) -> res.send("Hello World!"));
    indexRouter.get("/index", (req, res) -> res.send("Index"));
    indexRouter.get("/about", (req, res) -> res.send("About"));

    // Define router for user pages
    ExpressRouter userRouter = new ExpressRouter();
    userRouter.get("/user/login", (req, res) -> res.send("User Login"));
    userRouter.get("/user/register", (req, res) -> res.send("User Register"));
    userRouter.get("/user/:username", (req, res) -> res.send("You want to see: " + req.getParam("username")));

    // Add roter
    app.use(indexRouter);
    app.use(userRouter);

    // Start server
    app.listen();
  }

}
