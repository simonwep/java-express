package express.filter;

import com.sun.net.httpserver.HttpExchange;
import express.Express;
import express.ExpressException;
import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;

import java.util.function.Consumer;

/**
 * @author Simon Reinisch
 * <p>
 * Handler for multiple FilterLayer.
 */
public class FilterLayerHandler {

  private final FilterLayer[] layers;

  public FilterLayerHandler(int layers) {

    // Create & initialize layers
    this.layers = new FilterLayer[layers];
    for (int i = 0; i < this.layers.length; i++)
      this.layers[i] = new FilterLayer<>();
  }

  public void handle(HttpExchange httpExchange, Express express) {
    Request request = new Request(httpExchange, express);
    Response response = new Response(httpExchange);

    // First fire all middleware's, then the normal request filter
    for (FilterLayer chain : layers) {
      chain.filter(request, response);

      if (response.isClosed())
        return;
    }
  }

  /**
   * Add an new handler for an specific handler layers.
   *
   * @param level   The layers.
   * @param handler The handler, will be append to the top of the layers.
   */
  @SuppressWarnings("unchecked")
  public void add(int level, HttpRequestHandler handler) {

    if (level >= layers.length)
      throw new IndexOutOfBoundsException("Out of bounds: " + level + " > " + layers.length);
    if (level < 0)
      throw new IndexOutOfBoundsException("Cannot be under zero: " + level + " < 0");

    layers[level].add(handler);
  }

  /**
   * Merge two FilterLayerHandler
   *
   * @param filterLayerHandler The FilterLayerHandler which you want to merge with this
   */
  @SuppressWarnings("unchecked")
  public void combine(FilterLayerHandler filterLayerHandler) {
    if (filterLayerHandler != null) {
      FilterLayer[] chains = filterLayerHandler.getLayers();

      if (chains.length != layers.length)
        throw new ExpressException("Cannot add an filterLayerHandler with different layers sizes: " + chains.length + " != " + layers.length);

      for (int i = 0; i < chains.length; i++)
        layers[i].addAll(chains[i].getFilter());
    }
  }

  /**
   * Iterate over the different FilterLayer
   *
   * @param layerConsumer An consumer for the layers
   */
  public void forEach(Consumer<FilterLayer> layerConsumer) {
    if (layerConsumer == null)
      return;

    for (FilterLayer layer : layers)
      layerConsumer.accept(layer);
  }

  private FilterLayer[] getLayers() {
    return layers;
  }

}
