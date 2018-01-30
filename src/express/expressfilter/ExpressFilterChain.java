package express.expressfilter;

import express.http.Request;
import express.http.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ExpressFilterChain {

  private List<ExpressFilter> expressFilterList = Collections.synchronizedList(new ArrayList<ExpressFilter>());

  public void addFilter(ExpressFilter expressFilter) {
    expressFilterList.add(expressFilter);
  }

  public void addMiddleware(ExpressFilter expressFilter) {
    expressFilterList.add(0, expressFilter);
  }

  public void filter(Request req, Response res) {
    ListIterator<ExpressFilter> iter = expressFilterList.listIterator();

    while (!res.isClosed() && iter.hasNext()) {
      if (iter.hasNext()) {
        AbstractExpressFilter expressFilter = iter.next();
        expressFilter.doFilter(req, res, this);
      }
    }
  }
}
