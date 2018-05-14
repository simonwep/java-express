package express.http;

/**
 * @author Simon Reinisch
 * An simple SessionCookie implemetation.
 */
public class SessionCookie {

  private final long maxAge;
  private final long created;

  private Object data;

  public SessionCookie(long maxAge) {
    this.maxAge = maxAge;
    this.created = System.currentTimeMillis();
  }

  /**
   * @return Max age from this cookie
   */
  public long getMaxAge() {
    return maxAge;
  }

  /**
   * @return Create date from the cookie
   */
  public long getCreated() {
    return created;
  }

  /**
   * @return Session data
   */
  public Object getData() {
    return data;
  }

  /**
   * Set the session data
   *
   * @param data The data object
   * @return The object itself
   */
  public Object setData(Object data) {
    return this.data = data;
  }

}
