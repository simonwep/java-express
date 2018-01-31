package express.http;

import java.util.Base64;

public class Authorization {

  private final String TYPE;
  private final String DATA;

  public Authorization(String authHeader) {
    int index = authHeader.indexOf(' ');
    this.TYPE = authHeader.substring(0, index);
    this.DATA = authHeader.substring(index + 1);
  }

  public String getType() {
    return TYPE;
  }

  public String getData() {
    return DATA;
  }

  public String getDataBase64Decoded() {
    return new String(Base64.getDecoder().decode(DATA));
  }
}
