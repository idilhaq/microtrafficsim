package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.logic.StreetGraph;

import java.util.HashMap;


public class RandomScenarioBuilder extends StartEndScenarioBuilder {

    @Override
    public StartEndScenarioDescription createDescription(StreetGraph graph) {
        return new StartEndScenarioDescription(new HashMap<>(), new HashMap<>());
    }

    @Override
    public String toString() {
        return "random";
    }
}
