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
        return Constants.STREET_COLOR_SEL;
    }

    @Override
    public Color getStrokeColorSelected() {
        return Constants.STREET_STROKE_COLOR_SEL;
    }

    @Override
    public Color getColorUnselected() {
        return Constants.STREET_COLOR_UNSEL;
    }

    @Override
    public Color getStrokeColorUnselected() {
        return Constants.STREET_STROKE_COLOR_UNSEL;
    }
}
