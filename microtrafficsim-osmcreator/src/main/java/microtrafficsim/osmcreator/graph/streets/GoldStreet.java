package microtrafficsim.osmcreator.graph.streets;

import javafx.scene.paint.Color;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.Street;

/**
 * @author Dominic Parga Cacheiro
 */
public class GoldStreet extends Street {

    public GoldStreet(Crossroad origin, Crossroad destination) {
        super(origin, destination);
        setSelected(false);
    }

    /*
    |=======================|
    | (i) ColoredSelectable |
    |=======================|
    */
    @Override
    public Color getColorSelected() {
        return getColorUnselected().deriveColor(1, 1.1, 0.9, 1);
    }

    @Override
    public Color getStrokeColorSelected() {
        return getColorSelected();
    }

    @Override
    public Color getColorUnselected() {
        switch (getStreetType()) {
            case MOTORWAY:
                return Constants.STREET_COLOR_MOTORWAY;
            case PRIMARY:
                return Constants.STREET_COLOR_PRIMARY;
            case SECONDARY:
                return Constants.STREET_COLOR_SECONDARY;
            case RESIDENTIAL:
                return Constants.STREET_COLOR_RESIDENTIAL;
        }
        return null;
    }

    @Override
    public Color getStrokeColorUnselected() {
        return getColorUnselected();
    }
}
