package express.expressfilter;

import express.Express;
import express.events.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;

import java.util.HashMap;

/**
 * @author Simon Reinisch
 * @implNote Core modul of express, don't change anything.
 * <p>
 * Filter to handle request handling & parsing.
 */
public class ExpressFilterImpl implements HttpRequest {

  private final Express APP;

  private final HttpRequest REQUEST;
  private final String REQ;
  private final String CONTEXT;
  private final boolean REQ_ALL;
  private final boolean CONTEXT_ALL;

  public ExpressFilterImpl(Express app, String requestMethod, String context, HttpRequest httpRequest) {
    this.APP = app;
    this.REQ = requestMethod;
    this.REQUEST = httpRequest;
    this.CONTEXT = context;

    // Save some informations which don't need to be processed again
    this.REQ_ALL = requestMethod.equals("*");
    this.CONTEXT_ALL = context.equals("*");
  }

  @Override
  public void handle(Request req, Response res) {
    String requestMethod = req.getMethod();
    String requestPath = req.getRedirect() != null ? req.getRedirect() : req.getURI().getRawPath();

    // Check if
    if (!(REQ_ALL || REQ.equals(requestMethod))) {
      return;
    } else if (CONTEXT_ALL) {
      REQUEST.handle(req, res);
      return;
    }

    // Parse params
    HashMap<String, String> params = matchURL(CONTEXT, requestPath);
    if (params == null)
      return;

    // Save parameter to request object
    req.setParams(params);

    // Check parameter lsitener
    params.forEach((s, s2) -> {
      HttpRequest hreq = APP.getParameterListener().get(s);
      if (hreq != null)
        hreq.handle(req, res);
    });

    // Check if the response is closed
    if (res.isClosed())
      return;

    // Handle request
    REQUEST.handle(req, res);
  }

  /**
   * Extract and match the parameter from the url with an filter.
   */
  private HashMap<String, String> matchURL(String filter, String url) {
    HashMap<String, String> params = new HashMap<>();
    StringBuilder key = new StringBuilder();
    StringBuilder val = new StringBuilder();
    char[] uc = url.toCharArray();
    char[] fc = filter.toCharArray();
    int ui = 0, fi = 0;

    for (; fi < fc.length; fi++, ui++) {

      if (fc[fi] == ':') {
        key.setLength(0);
        val.setLength(0);

        fi++;
        while (fi < fc.length && fc[fi] != '/')
          key.append(fc[fi++]);

        while (ui < uc.length && uc[ui] != '/')
          val.append(uc[ui++]);

        params.put(key.toString(), val.toString());
      } else if (fc[fi] != uc[ui]) {

        // Failed
        return null;
      }
    }

    if (ui < url.length() || fi < filter.length()) {
      return null;
    }

    return params;
  }


}
