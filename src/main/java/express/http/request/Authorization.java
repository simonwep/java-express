package express.http.request;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Simon Reinisch
 * <p>
 * Represents an HTTP Authorization header value
 * and encapsulates authorization data
 */
public class Authorization {
  
  public static final String HEADER_NAME = "Authorization";
  
  private final String type;
  private final String data;
  
  public Authorization(String authHeader) {
    String[] parts = Stream.of(authHeader.split(" "))
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
    
    this.type = parts[0];
    this.data = parts[1];
  }
  
  /**
   * @return The Authorization type
   */
  public String getType() {
    return type;
  }
  
  /**
   * @return The Authorization data
   */
  public String getData() {
    return data;
  }
  
  /**
   * @return The Authorization data base64 decoded
   */
  public String getDataBase64Decoded() {
    return new String(Base64.getDecoder().decode(data));
  }
  
  /**
   * @return A list of authorization options that are contained in the given request.
   *  Authorization options can be separated by a comma in the Authorization header.
   */
  public static List<Authorization> get(Request req) {
    List<String> headerVals = req.getHeader(HEADER_NAME);
    if(!headerVals.isEmpty()) {
      String authHeader = headerVals.get(0);
      return Collections.unmodifiableList(Stream.of(authHeader.split(","))
          .map(Authorization::new).collect(Collectors.toList()));
    }
    return Collections.emptyList();
  }
  
  /**
   * Validates the given request authentication using each of the given predicates.
   * If any of the predicates returns <code>true</code>, the request is counted as
   * validly authorized and the method returns <code>true</code>.
   */
  @SafeVarargs
  public static boolean validate(Request req, Predicate<Authorization>... validators) {
    for(Authorization auth : get(req)) {
      for(Predicate<Authorization> validator : validators) {
        if(validator.test(auth)) return true;
      }
    }
    return false;
  }
  
  /**
   * @param type The expected type of the authorization
   * @param data The expected data of the authorization
   * @return A predicate that can be used with {@link Authorization#validate(Request, Predicate...)}
   *  to test for a single type of authorization
   */
  public static Predicate<Authorization> validator(String type, String data) {
    return (auth -> auth.getType().equals(type) && auth.getData().equals(data));
  }
}
