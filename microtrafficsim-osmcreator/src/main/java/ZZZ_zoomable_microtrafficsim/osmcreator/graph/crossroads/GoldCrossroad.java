package ZZZ_zoomable_microtrafficsim.osmcreator.graph.crossroads;

import ZZZ_zoomable_microtrafficsim.osmcreator.Constants;
import ZZZ_zoomable_microtrafficsim.osmcreator.graph.Crossroad;
import ZZZ_zoomable_microtrafficsim.osmcreator.user.controller.UserInputController;
import javafx.scene.paint.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldCrossroad extends Crossroad {

    public GoldCrossroad(UserInputController userController, double x, double y) {
        super(x, y);
        setSelected(false);
    }

    /*
    |=======================|
    | (i) ColoredSelectable |
    |=======================|
    */
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
