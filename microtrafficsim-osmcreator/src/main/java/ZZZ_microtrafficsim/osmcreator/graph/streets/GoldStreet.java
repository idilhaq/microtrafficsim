package ZZZ_microtrafficsim.osmcreator.graph.streets;

import ZZZ_microtrafficsim.osmcreator.Constants;
import ZZZ_microtrafficsim.osmcreator.graph.Crossroad;
import ZZZ_microtrafficsim.osmcreator.graph.Street;
import ZZZ_microtrafficsim.osmcreator.user.controller.UserInputController;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldStreet extends Street {

  private boolean selected;

  public GoldStreet(UserInputController userController, Crossroad origin, Crossroad destination) {
    super(origin, destination);
    setSelected(false);
  }

  /*
  |=======================|
  | (i) ColoredSelectable |
  |=======================|
  */
  @Override
  public void setLook() {
    setFill(Constants.STREET_COLOR_UNSEL);
    setStroke(Constants.STREET_COLOR_UNSEL);
    setStrokeWidth(Constants.STREET_STROKE_WIDTH);
    setStrokeType(StrokeType.OUTSIDE);
  }

  @Override
  public void setSelected(boolean selected) {
    this.selected = selected;
    Color color = selected ? Constants.STREET_COLOR_SEL : Constants.STREET_COLOR_UNSEL;
    setFill(color);
    setStroke(color);
  }

  @Override
  public boolean isSelected() {
    return selected;
  }
}
