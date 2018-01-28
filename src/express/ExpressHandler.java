package express;

import express.events.HttpRequest;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressHandler {

  private final HttpRequest REQUEST;
  private final String CONTEXT;

  private final String[] CONTEXT_PARAMS;
  private final String CONTEXT_REGEX;
  private final Pattern CONTEXT_PATTERN;
  private final boolean USE_CONTEXT_PATTERN;

  public ExpressHandler(String context, HttpRequest request) {
    this.CONTEXT = context;
    this.REQUEST = request;

    this.CONTEXT_PARAMS = context.split("(/)(:|[^:]+|)(:|)");
    this.CONTEXT_REGEX = "\\Q" + context.replaceAll(":([^/]+)", "\\\\E([^/]+)\\\\Q") + "\\E";
    this.CONTEXT_PATTERN = Pattern.compile(CONTEXT_REGEX);
    this.USE_CONTEXT_PATTERN = this.CONTEXT_PARAMS.length == 0;
  }

  public HttpRequest getRequest() {
    return REQUEST;
  }

  public String getContext() {
    return CONTEXT;
  }

  public HashMap<String, String> parseParams(String context) {
    if (!context.matches(CONTEXT_REGEX))
      return null;

    HashMap<String, String> params = new HashMap<>();
    Matcher matcher = CONTEXT_PATTERN.matcher(context);

    if (matcher.find()) {

      for (int i = 1; i <= matcher.groupCount() && i < CONTEXT_PARAMS.length; i++) {
        String g = matcher.group(i);
        params.put(CONTEXT_PARAMS[i], g);
      }
    } else {
      return null;
    }

    return params;
  }
}
