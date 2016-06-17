package ZZZ_zoomable_microtrafficsim.osmcreator.graph.streets;

import ZZZ_zoomable_microtrafficsim.osmcreator.Constants;
import ZZZ_zoomable_microtrafficsim.osmcreator.graph.Crossroad;
import ZZZ_zoomable_microtrafficsim.osmcreator.graph.Street;
import javafx.scene.paint.Color;

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
