package microtrafficsim.osmcreator.graph;

/**
 * @author Dominic Parga Cacheiro
 */
public enum StreetDirection {
  FORWARDS,
  BACKWARDS,
  BIDIRECTIONAL;

  public static StreetDirection merge(StreetDirection a, StreetDirection b) {
    if (a == null || b == null)
      return null;
    if (a == b)
      return a;
    return BIDIRECTIONAL;
  }

  public static StreetDirection invert(StreetDirection a) {
    if (a == BIDIRECTIONAL)
      return a;
    return a == FORWARDS ? BACKWARDS : FORWARDS;
  }
}
