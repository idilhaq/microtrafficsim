package microtrafficsim.osmcreator.model.crossroads;

import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.user.selection.ColoredSelectable;

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
