package express.expressfilter;

import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Simon Reinisch
 * <p>
 * Iterator for ExpressFilters
 */
public class ExpressFilterChain<T extends HttpRequest> {

  private List<T> expressFilters = Collections.synchronizedList(new ArrayList<>());

  public void add(T expressFilter) {
    expressFilters.add(expressFilter);
  }

  public void filter(Request req, Response res) {
    ListIterator<T> iter = expressFilters.listIterator();

    while (!res.isClosed() && iter.hasNext()) {
      iter.next().handle(req, res);

      if (req.hadRedirect()) {
        filter(req, res);
        return;
      }
    }
  }
}
