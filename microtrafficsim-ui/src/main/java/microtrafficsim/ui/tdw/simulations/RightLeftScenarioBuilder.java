package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.ISimplePolygon;
import microtrafficsim.core.map.area.RectangleArea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class RightLeftScenarioBuilder extends StartEndScenarioBuilder {

    @Override
    public StartEndScenarioDescription createDescription(StreetGraph graph) {

        /* init help variables */
        Coordinate graphCenter = new Coordinate(
                (graph.maxlat + graph.minlat) / 2,
                (graph.maxlon + graph.minlon) / 2
        );
        double graphWidth = graph.maxlon - graph.minlon;
        double graphHeight = graph.maxlon - graph.minlon;
        ISimplePolygon in;
        ISimplePolygon out;
        HashSet<Node> alreadyAddedNodes = new HashSet<>();
        ArrayList<Node> start = new ArrayList<>();
        ArrayList<Node> end = new ArrayList<>();
        boolean finished;

        double percentage = 0.25;
        boolean firstRun = true;
        do {
            /* set start/end polygons */
            // left
            out = new RectangleArea(
                    graph.minlat,
                    graph.minlon,
                    graph.maxlat,
                    graph.minlon + percentage * graphWidth
            );
            // right
            in = new RectangleArea(
                    graph.minlat,
                    graph.maxlon - percentage * graphWidth,
                    graph.maxlat,
                    graph.maxlon
            );


            for (Node node : graph.getNodeSet()) {
                if (!alreadyAddedNodes.contains(node)) {
                    boolean added = false;
                    if ((firstRun || start.isEmpty()) && in.contains(node)) {
                        start.add(node);
                        added = true;
                    }
                    if ((firstRun || end.isEmpty()) && out.contains(node)) {
                        end.add(node);
                        added = true;
                    }
                    if (added)
                        alreadyAddedNodes.add(node);
                }
            }


            /* prepare next run */
            finished = !start.isEmpty() && !end.isEmpty();
            percentage = percentage + 0.1;
            finished = finished || (percentage > 1);
            firstRun = false;
        } while (!finished);


        /* return */
        HashMap<ISimplePolygon, ArrayList<Node>> startMap = new HashMap<>();
        HashMap<ISimplePolygon, ArrayList<Node>> endMap = new HashMap<>();


        startMap.put(in, start);
        endMap.put(out, end);

        return new StartEndScenarioDescription(startMap, endMap);
    }

    @Override
    public String toString() {
        return "right->left";
    }
}
