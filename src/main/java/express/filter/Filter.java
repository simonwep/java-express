package express.filter;

/**
 * ExpressFilter to save middleware data, the name is the identifier.
 *
 * @author Simon Reinisch
 */
public interface Filter {

  /**
   * Identifier for the middleware
   *
   * @return The filter name
   */
  String getName();
}
