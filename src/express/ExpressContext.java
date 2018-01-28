package express;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class ExpressContext implements HttpHandler {

  private final Express EXPRESS;

  public ExpressContext(Express express) {
    this.EXPRESS = express;
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    new Thread(new ExpressContextThread(httpExchange, EXPRESS)).start();
  }


}
