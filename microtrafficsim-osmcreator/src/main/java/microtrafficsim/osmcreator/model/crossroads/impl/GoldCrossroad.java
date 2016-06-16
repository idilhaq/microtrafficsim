package microtrafficsim.osmcreator.model.crossroads.impl;

import javafx.scene.paint.Color;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.model.crossroads.Crossroad;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldCrossroad extends Crossroad {
  public GoldCrossroad(double x, double y) {
    super(x, y);
  }

  @Override
  public Color getColorSelected() {
    return Constants.CROSSROAD_COLOR_SEL;
  }

  @Override
  public Color getStrokeColorSelected() {
    return Constants.CROSSROAD_STROKE_COLOR_SEL;
  }

  @Override
  public Color getColorUnselected() {
    return Constants.CROSSROAD_COLOR_UNSEL;
  }

  @Override
  public Color getStrokeColorUnselected() {
    return Constants.CROSSROAD_STROKE_COLOR_UNSEL;
  }
}
