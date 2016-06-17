package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.SimplePolygon;

import java.util.ArrayList;
import java.util.HashMap;


public class RandomScenarioBuilder extends StartEndScenarioBuilder {

    @Override
    public StartEndScenarioDescription createDescription(StreetGraph graph) {
        // TODO

        ArrayList<Node> start = new ArrayList<>();
        ArrayList<Node> end = new ArrayList<>();

        graph.getNodeIterator().forEachRemaining(node -> {
            double rnd = Math.random();
            if (rnd < 0.1)
                start.add(node);
            else if (rnd > 0.9)
                end.add(node);
        });

        SimplePolygon p = new SimplePolygon(new Coordinate[] {
                new Coordinate(graph.minLat, graph.minLon),
                new Coordinate(graph.minLat, graph.maxLon),
                new Coordinate(graph.maxLat, graph.maxLon),
                new Coordinate(graph.maxLat, graph.minLon)
        });

        HashMap<SimplePolygon, ArrayList<Node>> startMap = new HashMap<>();
        HashMap<SimplePolygon, ArrayList<Node>> endMap = new HashMap<>();

        startMap.put(p, start);
        endMap.put(p, end);

        return new StartEndScenarioDescription(startMap, endMap);
    }

    @Override
    public String toString() {
        return "random";
    }
}
