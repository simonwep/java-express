package express;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import express.events.HttpRequest;
import express.http.Request;
import express.http.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressFilter extends Filter {

  private final HttpRequest REQUEST;
  private final String REQUEST_METHOD;

  private final String CONTEXT;
  private final String[] CONTEXT_PARAMS;
  private final String CONTEXT_REGEX;
  private final Pattern CONTEXT_PATTERN;

  private String description;

  public ExpressFilter(String requestMethod, String context, HttpRequest httpRequest) {
    this.REQUEST_METHOD = requestMethod;
    this.REQUEST = httpRequest;

    this.CONTEXT = context;
    this.CONTEXT_PARAMS = context.split("(/)(:|[^:]+|)(:|)");
    this.CONTEXT_REGEX = "\\Q" + context.replaceAll(":([^/]+)", "\\\\E([^/]+)\\\\Q") + "\\E";
    this.CONTEXT_PATTERN = Pattern.compile(CONTEXT_REGEX);
  }

  @Override
  public void doFilter(HttpExchange httpExchange, Chain chain) throws IOException {
    Request request = new Request(httpExchange);
    Response response = new Response(httpExchange);

    String requestMethod = httpExchange.getRequestMethod();
    String requestPath = request.getURI().getRawPath();

    if ((requestMethod.equals(REQUEST_METHOD)) || (REQUEST_METHOD.equals("*") && CONTEXT.equals("*"))) {
      REQUEST.handle(request, response);

      if (!response.isClosed())
        chain.doFilter(httpExchange);
      return;
    }

    if (!requestPath.matches(CONTEXT_REGEX) && !CONTEXT.equals("*")) {
      chain.doFilter(httpExchange);
      return;
    }

    HashMap<String, String> params = new HashMap<>();
    Matcher matcher = CONTEXT_PATTERN.matcher(requestPath);

    if (matcher.find()) {

      for (int i = 1; i <= matcher.groupCount() && i < CONTEXT_PARAMS.length; i++) {
        String g = matcher.group(i);
        params.put(CONTEXT_PARAMS[i], g);
      }

    } else {
      chain.doFilter(httpExchange);
      return;
    }

    request.setParams(params);
    REQUEST.handle(request, response);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String description() {
    return description;
  }
}
