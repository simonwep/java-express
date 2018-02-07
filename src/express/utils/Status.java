package express.utils;

/**
 * @author Simon Reinisch
 * Enum with all status codes.
 */
public enum Status {

  // Informational
  _100(100, "Continue"),
  _101(101, "Switching Protocols"),
  _102(102, "Processing"),

  // Success
  _200(200, "OK"),
  _201(201, "Created"),
  _202(202, "Accepted"),
  _203(203, "Non-authoritative Information"),
  _204(204, "No Content"),
  _205(205, "Reset Content"),
  _206(206, "Partial Content"),
  _207(207, "Multi-Status"),
  _208(208, "Already Reported"),
  _226(226, "IM Used"),

  // Redirection
  _300(300, "Multiple Choices"),
  _301(301, "Moved Permanently"),
  _302(302, "Found"),
  _303(303, "See Other"),
  _304(304, "Not Modified"),
  _305(305, "Use Proxy"),
  _307(307, "Temporary Redirect"),
  _308(308, "Permanent Redirect"),

  // Client Error
  _400(400, "Bad Request"),
  _401(401, "Unauthorized"),
  _402(402, "Payment Required"),
  _403(403, "Forbidden"),
  _404(404, "Not Found"),
  _405(405, "Method Not Allowed"),
  _406(406, "Not Acceptable"),
  _407(407, "Proxy Authentication Required"),
  _408(408, "Request Timeout"),
  _409(409, "Conflict"),
  _410(410, "Gone"),
  _411(411, "Length Required"),
  _412(412, "Precondition Failed"),
  _413(413, "Payload Too Large"),
  _414(414, "Request-URI Too Long"),
  _415(415, "Unsupported Media Type"),
  _416(416, "Requested Range Not Satisfiable"),
  _417(417, "Expectation Failed"),
  _418(418, "I'm a teapot"),
  _421(421, "Misdirected Request"),
  _422(422, "Unprocessable Entity"),
  _423(423, "Locked"),
  _424(424, "Failed Dependency"),
  _426(426, "Upgrade Required"),
  _428(428, "Precondition Required"),
  _429(429, "Too Many Requests"),
  _431(431, "Request Header Fields Too Large"),
  _444(444, "Connection Closed Without Response"),
  _451(451, "Unavailable For Legal Reasons"),
  _499(499, "Client Closed Request"),

  // Server Error
  _500(500, "Internal Server Error"),
  _501(501, "Not Implemented"),
  _502(502, "Bad Gateway"),
  _503(503, "Service Unavailable"),
  _504(504, "Gateway Timeout"),
  _505(505, "HTTP Version Not Supported"),
  _506(506, "Variant Also Negotiates"),
  _507(507, "Insufficient Storage"),
  _508(508, "Loop Detected"),
  _510(510, "Not Extended"),
  _511(511, "Network Authentication Required"),
  _599(599, "Network Connect Timeout Error");


  static {

    // Check values
    for (Status s : values()) {
      if (s.name().charAt(0) != '_')
        throw new IllegalStateException("Status code '" + s + "' need to start with underscore.");
    }
  }

  private String description;
  private int code;

  Status(int code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public int getCode() {
    return code;
  }

  public static Status valueOf(int code) {
    switch (code) {
      case 100:
        return _100;
      case 101:
        return _101;
      case 102:
        return _102;
      case 200:
        return _200;
      case 201:
        return _201;
      case 202:
        return _202;
      case 203:
        return _203;
      case 204:
        return _204;
      case 205:
        return _205;
      case 206:
        return _206;
      case 207:
        return _207;
      case 208:
        return _208;
      case 226:
        return _226;
      case 300:
        return _300;
      case 301:
        return _301;
      case 302:
        return _302;
      case 303:
        return _303;
      case 304:
        return _304;
      case 305:
        return _305;
      case 307:
        return _307;
      case 308:
        return _308;
      case 400:
        return _400;
      case 401:
        return _401;
      case 402:
        return _402;
      case 403:
        return _403;
      case 404:
        return _404;
      case 405:
        return _405;
      case 406:
        return _406;
      case 407:
        return _407;
      case 408:
        return _408;
      case 409:
        return _409;
      case 410:
        return _410;
      case 411:
        return _411;
      case 412:
        return _412;
      case 413:
        return _413;
      case 414:
        return _414;
      case 415:
        return _415;
      case 416:
        return _416;
      case 417:
        return _417;
      case 418:
        return _418;
      case 421:
        return _421;
      case 422:
        return _422;
      case 423:
        return _423;
      case 424:
        return _424;
      case 426:
        return _426;
      case 428:
        return _428;
      case 429:
        return _429;
      case 431:
        return _431;
      case 444:
        return _444;
      case 451:
        return _451;
      case 499:
        return _499;
      case 500:
        return _500;
      case 501:
        return _501;
      case 502:
        return _502;
      case 503:
        return _503;
      case 504:
        return _504;
      case 505:
        return _505;
      case 506:
        return _506;
      case 507:
        return _507;
      case 508:
        return _508;
      case 510:
        return _510;
      case 511:
        return _511;
      case 599:
        return _599;
    }
    return null;
  }
}
