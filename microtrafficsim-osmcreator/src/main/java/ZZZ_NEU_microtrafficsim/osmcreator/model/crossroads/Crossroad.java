package ZZZ_NEU_microtrafficsim.osmcreator.model.crossroads;

import ZZZ_NEU_microtrafficsim.osmcreator.Constants;
import ZZZ_NEU_microtrafficsim.osmcreator.user.selection.ColoredSelectable;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class Crossroad extends Circle implements ColoredSelectable {

    private boolean isSelected;

    public Crossroad(double x, double y) {
        super(x, y, Constants.CROSSROAD_RADIUS);
        setSelected(false);
    }

    /*
    |================|
    | (i) Selectable |
    |================|
    */
    @Override
    public void setSelected(boolean value) {
        isSelected = value;
        if (isSelected) {
            setFill(getColorSelected());
            setStroke(getStrokeColorSelected());
            setStrokeWidth(Constants.CROSSROAD_STROKE_WIDTH);
            setStrokeType(StrokeType.INSIDE);
        } else {
            setFill(getColorUnselected());
            setStroke(getStrokeColorUnselected());
            setStrokeWidth(Constants.CROSSROAD_STROKE_WIDTH);
            setStrokeType(StrokeType.INSIDE);
        }
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }
}
