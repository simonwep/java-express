package express.expressfilter;

/**
 * @author Simon Reinisch
 * <p>
 * ExpressFilter to save middleware data, the name is the indentifier.
 */
public interface ExpressFilter {

  /**
   * Identifier for the middleware
   *
   * @return The filter name
   */
  String getName();
}
