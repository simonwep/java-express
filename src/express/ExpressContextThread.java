package express;

import com.sun.net.httpserver.HttpExchange;
import express.http.Request;
import express.http.Response;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpressContextThread extends Express implements Runnable {

  private final Express EXPRESS;
  private final HttpExchange HTTP_EXCHANGE;
  private final Request REQUEST;
  private final Response RESPONSE;

  private String requestPath;
  private String requestMethod;

  public ExpressContextThread(HttpExchange httpExchange, Express express) {
    this.EXPRESS = express;
    this.HTTP_EXCHANGE = httpExchange;
    this.REQUEST = new Request(httpExchange);
    this.RESPONSE = new Response(httpExchange);

    this.requestPath = REQUEST.getRequestURI().getRawPath();
    this.requestMethod = REQUEST.getRequestMethod();
  }


  @Override
  public void run() {
    ArrayList<ExpressHandler> middleware = EXPRESS.MITTLEWARE.get(requestMethod);
    ArrayList<ExpressHandler> requests = EXPRESS.REQUEST.get(requestMethod);

    if (middleware != null && middleware.size() > 0) {
      middleware.forEach(exh -> {
        if (RESPONSE.isClosed())
          return;

        HashMap<String, String> params = exh.parseParams(requestPath);
        if (params != null) {

          REQUEST.setParams(params);
          exh.getRequest().handle(REQUEST, RESPONSE);
        }
      });
    }

    if (requests != null && requests.size() > 0) {
      requests.forEach(exh -> {

        HashMap<String, String> params = exh.parseParams(requestPath);
        if (params != null) {
          if (RESPONSE.isClosed())
            return;

          REQUEST.setParams(params);
          exh.getRequest().handle(REQUEST, RESPONSE);
        }
      });
    }
  }
}
