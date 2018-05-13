package express;

/**
 * @author Simon Reinisch
 * Exception for express own errors.
 */
public class ExpressException extends RuntimeException {

  /**
   * Constructs a new exception with an empty detail message.
   */
  public ExpressException() {}

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for
   *                later retrieval by the getMessage() method.
   */
  public ExpressException(String message) {
    super(message);
  }
}