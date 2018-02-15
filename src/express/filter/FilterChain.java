package express.filter;

import express.http.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Simon Reinisch
 * <p>
 * Iterator for ExpressFilters
 */
public class FilterChain<T extends HttpRequest> {

  private List<T> expressFilters = Collections.synchronizedList(new ArrayList<>());

  public void add(T expressFilter) {
    this.expressFilters.add(expressFilter);
  }

  public void add(int index, T expressFilter) {
    this.expressFilters.add(index, expressFilter);
  }

  void addAll(List<T> expressFilters) {
    this.expressFilters.addAll(expressFilters);
  }

  List<T> getFilter() {
    return expressFilters;
  }

  void filter(Request req, Response res) {
    ListIterator<T> iter =  this.expressFilters.listIterator();

    while (!res.isClosed() && iter.hasNext()) {
      iter.next().handle(req, res);

      if (req.hadRedirect()) {
        filter(req, res);
        return;
      }
    }
  }
}
