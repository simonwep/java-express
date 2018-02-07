package express.http.request;

import java.util.Base64;

public class Authorization {

  private final String TYPE;
  private final String DATA;

  public Authorization(String authHeader) {
    int index = authHeader.indexOf(' ');
    this.TYPE = authHeader.substring(0, index);
    this.DATA = authHeader.substring(index + 1);
  }

  /**
   * @return The Authorization type
   */
  public String getType() {
    return TYPE;
  }

  /**
   * @return The Authorization data
   */
  public String getData() {
    return DATA;
  }

  /**
   * @return The Authorization data base64 decoded
   */
  public String getDataBase64Decoded() {
    return new String(Base64.getDecoder().decode(DATA));
  }
}
