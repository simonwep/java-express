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

  public static Status valueOf(int code) {
    for(Status status : values()) {
      if(status.code == code) return status;
    }
    return null;
  }

  public String getDescription() {
    return description;
  }

  public int getCode() {
    return code;
  }
}
