package microtrafficsim.osmcreator.graph;

import microtrafficsim.osmcreator.user.gestures.selection.SelectionModel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class OSMGraphModel implements SelectionModel<Crossroad> {

  private Set<Crossroad> selectables;
  private Set<Street> streets;

  public OSMGraphModel() {
    selectables = new HashSet<>();
    streets = new HashSet<>();
  }

  public Set<Street> getStreets() {
    return streets;
  }

  @Override
  public Set<Crossroad> getSelectables() {
    return selectables;
  }

  @Override
  public void add(Crossroad selectable) {
    selectables.add(selectable);
  }

  @Override
  public boolean remove(Crossroad selectable) {
    return selectables.remove(selectable);
  }
}
