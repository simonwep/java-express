package express.expressfilter;

import express.events.HttpRequest;
import express.http.request.Request;
import express.http.response.Response;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon Reinisch
 * @implNote Core modul of express, don't change anything.
 * <p>
 * Filter to handle request handling & parsing.
 */
public class ExpressFilterImpl implements HttpRequest {

  private final HttpRequest REQUEST;
  private final String REQUEST_METHOD;
  private final String CONTEXT;

  public ExpressFilterImpl(String requestMethod, String context, HttpRequest httpRequest) {
    this.REQUEST_METHOD = requestMethod;
    this.REQUEST = httpRequest;
    this.CONTEXT = context;
  }

  @Override
  public void handle(Request req, Response res) {
    String requestMethod = req.getMethod();
    String requestPath = req.getRedirect() != null ? req.getRedirect() : req.getURI().getRawPath();

    if (!(REQUEST_METHOD.equals("*") || REQUEST_METHOD.equals(requestMethod))) {
      return;
    } else if (CONTEXT.equals("*")) {
      REQUEST.handle(req, res);
      return;
    }

    // Parse params
    HashMap<String, String> params = matchURL(CONTEXT, requestPath);
    if(params == null)
      return;

    req.setParams(params);
    REQUEST.handle(req, res);
  }

  private static HashMap<String, String> matchURL(String filter, String url) {
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
        while (fi < fc.length && fc[fi] != '/') {
          key.append(fc[fi++]);
        }

        while (ui < uc.length && uc[ui] != '/') {
          val.append(uc[ui++]);
        }

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
