package express.expressfilter;

import express.http.Request;
import express.http.Response;

import java.io.IOException;

public abstract class AbstractExpressFilter {

  abstract void doFilter(Request req, Response res, ExpressFilterChain chain);

}
