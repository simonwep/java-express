package express.filter;

import com.sun.net.httpserver.HttpExchange;
import express.Express;
import express.ExpressException;
import express.http.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;

import java.util.function.Consumer;

/**
 * @author Simon Reinisch
 * <p>
 * Handler for multiple FilterLayer.
 */
public class FilterLayerHandler {

  private final FilterLayer[] LAYER;

  public FilterLayerHandler(int layer) {

    // Create & initialize layers
    this.LAYER = new FilterLayer[layer];
    for (int i = 0; i < LAYER.length; i++)
      LAYER[i] = new FilterLayer<>();
  }

  public void handle(HttpExchange httpExchange, Express express) {
    Request request = new Request(httpExchange, express);
    Response response = new Response(httpExchange);

    // First fire all middleware's, then the normal request filter
    for (FilterLayer chain : LAYER) {
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
  @SuppressWarnings("unchecked")
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
  @SuppressWarnings("unchecked")
  public void combine(FilterLayerHandler filterLayerHandler) {
    if (filterLayerHandler != null) {
      FilterLayer[] chains = filterLayerHandler.getLayer();

      if (chains.length != LAYER.length)
        throw new ExpressException("Cannot add an filterLayerHandler with different layer sizes: " + chains.length + " != " + LAYER.length);

      for (int i = 0; i < chains.length; i++)
        LAYER[i].addAll(chains[i].getFilter());
    }
  }

  /**
   * Iterate over the different FilterLayer
   *
   * @param layerConsumer An consumer for the layer
   */
  public void forEach(Consumer<FilterLayer> layerConsumer) {
    if (layerConsumer == null)
      return;

    for (FilterLayer layer : LAYER)
      layerConsumer.accept(layer);
  }

  private FilterLayer[] getLayer() {
    return LAYER;
  }

}
