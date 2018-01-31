package express.middleware;

public class SessionCookie {

  private final long MAX_AGE;
  private final long CREATED;

  private Object data;

  public SessionCookie(long maxAge) {
    this.MAX_AGE = maxAge;
    this.CREATED = System.currentTimeMillis();
  }

  public long getMaxAge() {
    return MAX_AGE;
  }

  public long getCreated() {
    return CREATED;
  }

  public Object getData() {
    return data;
  }

  public Object setData(Object data) {
    return this.data = data;
  }

}
