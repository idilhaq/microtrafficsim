package ZZZ_microtrafficsim.osmcreator.graph;

import ZZZ_microtrafficsim.osmcreator.Constants;
import ZZZ_microtrafficsim.osmcreator.user.gestures.draggable.DragDelta;
import javafx.scene.shape.Circle;
import ZZZ_microtrafficsim.osmcreator.user.controller.UserInputController;
import ZZZ_microtrafficsim.osmcreator.user.gestures.draggable.Draggable;
import ZZZ_microtrafficsim.osmcreator.user.gestures.selection.ColoredSelectable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A draggable anchor displayed around a point.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class Crossroad extends Circle implements Draggable, ColoredSelectable {

  private UserInputController userController;
  private Map<Street, Street> streets;

  public Crossroad(UserInputController userController, double x, double y) {
    super(x, y, Constants.CROSSROAD_RADIUS);
    this.userController = userController;
    streets = new HashMap<>();
    dragDelta = new DragDelta();

    setLook();
  }

  /**
   *
   * @return {@code true} if street didn't exist before
   */
  public boolean add(Street street) {
    Street original = streets.get(street);
    if (original != null) {
      StreetDirection other = street.getStreetDirectionFrom(this);
      // if this crossroad should add itself as origin (destination) when it is already destination (origin)
      if (original.origin != this)
        other = StreetDirection.invert(other);
      original.mergeStreetDirection(other);
      return false;
    } else {
      streets.put(street, street);
      return true;
    }
  }

  void remove(Street street) {
    streets.remove(street);
  }

  public Set<Street> getStreets() {
    return new HashSet<Street>(streets.keySet());
  }

  /*
  |===============|
  | (i) Draggable |
  |===============|
  */
  private DragDelta dragDelta;
  @Override
  public DragDelta getDragDelta() {
    return dragDelta;
  }

  @Override
  public void setDragDelta(double x, double y) {
    dragDelta.x = x;
    dragDelta.y = y;
  }

  @Override
  public double getDragX() {
    return getCenterX();
  }

  @Override
  public void setDragX(double x) {
    setCenterX(x);
  }

  @Override
  public double getDragY() {
    return getCenterY();
  }

  @Override
  public void setDragY(double y) {
    setCenterY(y);
  }
}
