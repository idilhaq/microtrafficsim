package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.ISimplePolygon;
import microtrafficsim.core.map.area.RectangleArea;

import java.util.ArrayList;
import java.util.HashMap;


public class RandomScenarioBuilder extends StartEndScenarioBuilder {

    @Override
    public StartEndScenarioDescription createDescription(StreetGraph graph) {
        ArrayList<Node> start = new ArrayList<>();
        ArrayList<Node> end = new ArrayList<>();

        graph.getNodeIterator().forEachRemaining(node -> {
            start.add(node);
            end.add(node);
        });

        ISimplePolygon p = new RectangleArea(graph.minlat, graph.minlon, graph.maxlat, graph.maxlon);

        HashMap<ISimplePolygon, ArrayList<Node>> startMap = new HashMap<>();
        HashMap<ISimplePolygon, ArrayList<Node>> endMap = new HashMap<>();

        startMap.put(p, start);
        endMap.put(p, end);

        return new StartEndScenarioDescription(startMap, endMap);
    }

    @Override
    public String toString() {
        return "random";
    }
}
