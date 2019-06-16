package express.middleware;

import express.http.RequestMethod;
import express.http.request.Request;

public class CorsOptions {

    private boolean allowCredentials;
    private RequestMethod[] methods;
    private String[] headers;
    private String origin;
    private Filter filter;

    public CorsOptions(boolean allowCredentials, String origin, String[] headers, RequestMethod[] methods, Filter filter) {
        this.allowCredentials = allowCredentials;
        this.origin = origin;
        this.filter = filter;
        this.methods = methods;
        this.headers = headers;
    }

    public CorsOptions() {
        this(false, null, null, null, null);
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String getOrigin() {
        return origin;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public RequestMethod[] getMethods() {
        return methods;
    }

    public void setMethods(RequestMethod[] methods) {
        this.methods = methods;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    interface Filter {
        boolean shouldBypass(Request req);
    }
}