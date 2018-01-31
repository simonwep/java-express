package express.expressfilter;

import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;

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
  private final String[] CONTEXT_PARAMS;
  private final String CONTEXT_REGEX;
  private final Pattern CONTEXT_PATTERN;

  public ExpressFilterImpl(String requestMethod, String context, HttpRequest httpRequest) {
    this.REQUEST_METHOD = requestMethod;
    this.REQUEST = httpRequest;

    this.CONTEXT = context;
    this.CONTEXT_PARAMS = context.split("(/)(:|[^:]+|)(:|)");
    this.CONTEXT_REGEX = "\\Q" + context.replaceAll(":([^/]+)", "\\\\E([^/]+)\\\\Q") + "\\E";
    this.CONTEXT_PATTERN = Pattern.compile(CONTEXT_REGEX);
  }

  @Override
  public void handle(Request req, Response res) {
    String requestMethod = req.getMethod();
    String requestPath = req.getURI().getRawPath();

    if (!requestMethod.equals(REQUEST_METHOD) || (REQUEST_METHOD.equals("*") && CONTEXT.equals("*"))) {
      REQUEST.handle(req, res);
      return;
    }

    if (!requestPath.matches(CONTEXT_REGEX) && !CONTEXT.equals("*")) {
      return;
    }

    // Parse params, see README
    HashMap<String, String> params = new HashMap<>();
    Matcher matcher = CONTEXT_PATTERN.matcher(requestPath);

    // Match all params
    if (matcher.find()) {

      for (int i = 1; i <= matcher.groupCount() && i < CONTEXT_PARAMS.length; i++) {
        String g = matcher.group(i);
        params.put(CONTEXT_PARAMS[i], g);
      }

    } else {
      return;
    }

    req.setParams(params);
    REQUEST.handle(req, res);
  }
}
