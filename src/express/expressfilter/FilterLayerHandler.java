package express.expressfilter;

import com.sun.net.httpserver.HttpExchange;
import express.Express;
import express.ExpressException;
import express.events.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;

import java.io.IOException;

public class FilterLayerHandler {

  private ExpressFilterChain[] LAYER;

  public FilterLayerHandler(int layer) {

    // Initialize layers
    this.LAYER = new ExpressFilterChain[layer];
    for (int i = 0; i < LAYER.length; i++)
      LAYER[i] = new ExpressFilterChain<>();
  }

  public void handle(HttpExchange httpExchange, Express express) throws IOException {
    Request request = new Request(httpExchange, express);
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

  /**
   * Merge two FilterLayerHandler
   *
   * @param filterLayerHandler The FilterLayerHandler which you want to merge with this
   */
  public void combine(FilterLayerHandler filterLayerHandler) {
    if (filterLayerHandler != null) {
      ExpressFilterChain[] chains = filterLayerHandler.getLayer();

      if (chains.length != LAYER.length)
        throw new ExpressException("Cannot add an filterLayerHandler with different layer sizes: " + chains.length + " != " + LAYER.length);

      for (int i = 0; i < chains.length; i++)
        LAYER[i].addAll(chains[i].getFilter());
    }
  }

  private ExpressFilterChain[] getLayer() {
    return LAYER;
  }

}
