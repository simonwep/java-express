package express.middleware;

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
   * @return max age from this cookie
   */
  public long getMaxAge() {
    return MAX_AGE;
  }

  /**
   * @return create date from the cookie
   */
  public long getCreated() {
    return CREATED;
  }

  /**
   * @return session data
   */
  public Object getData() {
    return data;
  }

  /**
   * Set the session data
   * @param data the data object
   * @return the object itself
   */
  public Object setData(Object data) {
    return this.data = data;
  }

}
