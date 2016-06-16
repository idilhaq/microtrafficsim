package microtrafficsim.osmcreator.model;

import microtrafficsim.osmcreator.model.crossroads.Crossroad;
import microtrafficsim.osmcreator.model.crossroads.impl.GoldCrossroad;

/**
 * @author Dominic Parga Cacheiro
 */
public class GraphModel {

    public GraphModel() {
    }

    public Crossroad createCrossroad(double x, double y) {
        Crossroad crossroad = new GoldCrossroad(x, y);
        /* add */
//    crossroadGroup.getChildren().add(crossroad);
//    model.getSelectables().add(crossroad);
        return crossroad;
    }
}
