package ZZZ_microtrafficsim.osmcreator;

import ZZZ_microtrafficsim.osmcreator.graph.Street;
import ZZZ_microtrafficsim.osmcreator.graph.Crossroad;
import ZZZ_microtrafficsim.osmcreator.user.gestures.selection.SelectionModel;

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

  Set<Street> getStreets() {
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
