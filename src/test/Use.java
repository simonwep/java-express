package test;

import express.Express;
import express.middleware.Static;
import java.io.IOException;

public class Use {

  public static void main(String[] args) throws IOException {
    Express app = new Express();

    // Test for static file service
    app.use(new Static("examplepath\\test_statics"));


    app.listen(() -> System.out.println("Express is listening!"));
  }

}
