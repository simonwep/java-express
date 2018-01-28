package test;

import express.Express;

import java.io.IOException;

public class Use {

  public static void main(String[] args) throws IOException {
    Express express = new Express();

    /**
     * Test case for using static ressources
     * replace 'yourpath' with the path of, for example, the static files
     */
    express.use(Express.statics("yourpath\\test_statics"));

    express.listen(() -> System.out.println("Express is listening!"));
  }

}
