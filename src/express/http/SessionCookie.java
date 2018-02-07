package express.http;

/**
 * @author Simon Reinisch
 * <p>
 * An simple SessionCookie
 */
public class SessionCookie {

  private final long MAX_AGE;
  private final long CREATED;

  private Object data;

  public SessionCookie(long maxAge) {
    this.MAX_AGE = maxAge;
    this.CREATED = System.currentTimeMillis();
  }

  /**
     * @return Max age from this cookie
   */
  public long getMaxAge() {
    return MAX_AGE;
  }

  /**
   * @return Create date from the cookie
   */
  public long getCreated() {
    return CREATED;
  }

  /**
   * @return Session data
   */
  public Object getData() {
    return data;
  }

  /**
   * Set the session data
   * @param data The data object
   * @return The object itself
   */
  public Object setData(Object data) {
    return this.data = data;
  }

}
