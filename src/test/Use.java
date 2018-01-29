package test;

import express.Express;
import express.cookie.Cookie;

import java.io.IOException;
import java.util.HashMap;

public class Use {

  public static void main(String[] args) throws IOException {
    Express app = new Express();

    // Test for static file service
    app.use(Express.statics("examplepath\\test_statics"));

    app.listen(() -> System.out.println("Express is listening!"));
  }

}
