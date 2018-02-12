package express.expressfilter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import express.events.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;

import java.io.IOException;

public class FilterLayerHandler implements HttpHandler {

  private ExpressFilterChain[] LAYER;

  {
    // Initialize layers
    LAYER = new ExpressFilterChain[2];

    for (int i = 0; i < LAYER.length; i++) {
      LAYER[i] = new ExpressFilterChain();
    }
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    Request request = new Request(httpExchange);
    Response response = new Response(httpExchange);

    // First fire all middlewares, then the normal request filter
    for (ExpressFilterChain chain : LAYER) {
      chain.filter(request, response);

      if (response.isClosed())
        return;
    }
  }

  /**
   * Add an new handler for an specific handler layer.
   *
   * @param level   The layer.
   * @param handler The handler, will be append to the top of the layer.
   */
  public void add(int level, HttpRequest handler) {

    if (level >= LAYER.length)
      throw new IndexOutOfBoundsException("Out of bounds: " + level + " > " + LAYER.length);
    if (level < 0)
      throw new IndexOutOfBoundsException("Cannot be under zero: " + level + " < 0");

    LAYER[level].add(handler);
  }

}
