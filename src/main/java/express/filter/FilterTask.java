package express.filter;

/**
 * Interface for filter tasks.
 *
 * @author Simon Reinisch
 */
public interface FilterTask {

  /**
   * Returns the delay between the updates
   *
   * @return Update delay in milliseconds
   */
  long getDelay();

  /** Will be fired between the delays */
  void onUpdate();

  /** Will be fired on express-start */
  void onStart();

  /** Will be fired on express-stop */
  void onStop();
}
