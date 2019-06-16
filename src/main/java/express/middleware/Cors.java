package express.middleware;

import express.http.HttpRequestHandler;
import express.http.RequestMethod;
import express.http.request.Request;
import express.http.response.Response;

public class Cors implements HttpRequestHandler {

    private final CorsOptions options;

    public Cors(CorsOptions options) {
        this.options = options;
    }

    @Override
    public void handle(Request req, Response res) {
        CorsOptions.Filter filter = this.options.getFilter();

        // Check if filter is present
        if (filter != null && !filter.shouldBypass(req)) {
            return;
        }

        // Acquire options
        boolean ac = this.options.isAllowCredentials();
        String origins = this.options.getOrigin();
        String[] headers = this.options.getHeaders();
        RequestMethod[] methods = this.options.getMethods();

        // Apply headers
        res.setHeader("Access-Control-Allow-Credentials", Boolean.toString(ac));
        res.setHeader("Access-Control-Allow-Origin", origins != null ? origins : "*");
        res.setHeader("Access-Control-Allow-Methods", methods != null ? join(methods) : "*");
        res.setHeader("Access-Control-Request-Headers", headers != null ? join(headers) : "*");
    }

    private String join(Object[] objects) {
        StringBuilder sb = new StringBuilder();

        for (Object o : objects) {
            sb.append(o.toString()).append(", ");
        }

        String string = sb.toString();
        return string.substring(0, string.length() - 2);
    }
}
