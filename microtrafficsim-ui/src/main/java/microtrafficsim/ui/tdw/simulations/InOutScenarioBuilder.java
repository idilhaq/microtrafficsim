package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.*;
import microtrafficsim.math.HaversineDistanceCalculator;

import java.util.*;


public class InOutScenarioBuilder extends StartEndScenarioBuilder {

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
        ISimplePolygon[] out = new ISimplePolygon[4];
        HashSet<Node> alreadyAddedNodes = new HashSet<>();
        ArrayList<Node> start = new ArrayList<>();
        ArrayList<ArrayList<Node>> ends = new ArrayList<>(out.length);
        for (ISimplePolygon ignored : out) ends.add(new ArrayList<>());
        boolean finished;

        double percentage = 0.1;
        boolean firstRun = true;
        do {
            /* set start/end polygons */
            in = new RectangleArea(
                    graphCenter.lat - 2*percentage * graphHeight,
                    graphCenter.lon - 2*percentage * graphWidth,
                    graphCenter.lat + 2*percentage * graphHeight,
                    graphCenter.lon + 2*percentage * graphWidth
            );
            // left
            out[0] = new RectangleArea(
                    graph.minlat,
                    graph.minlon,
                    graph.maxlat,
                    graph.minlon + percentage * graphWidth
            );
            // bottom
            out[1] = new RectangleArea(
                    graph.minlat,
                    graph.minlon + percentage * graphWidth,
                    graph.minlat + percentage * graphHeight,
                    graph.maxlon - percentage * graphWidth
            );
            // right
            out[2] = new RectangleArea(
                    graph.minlat,
                    graph.maxlon - percentage * graphWidth,
                    graph.maxlat,
                    graph.maxlon
            );
            // top
            out[3] = new RectangleArea(
                    graph.maxlat - percentage * graphHeight,
                    graph.minlon + percentage * graphWidth,
                    graph.maxlat,
                    graph.maxlon - percentage * graphWidth
            );


            for (Node node : graph.getNodeSet()) {
                if (!alreadyAddedNodes.contains(node)) {
                    boolean added = false;
                    if ((firstRun || start.isEmpty()) && in.contains(node)) {
                        start.add(node);
                        added = true;
                    }
                    for (int i = 0; i < out.length; i++) {
                        if ((firstRun || ends.get(i).isEmpty()) && out[i].contains(node)) {
                            ends.get(i).add(node);
                            added = true;
                            break;
                        }
                    }
                    if (added)
                        alreadyAddedNodes.add(node);
                }
            }


            /* prepare next run */
            finished = !start.isEmpty();
            for (ArrayList<Node> end : ends) {
                finished = finished && !end.isEmpty();
            }
            percentage = percentage + 0.1;
            finished = finished || (percentage > 1);
            firstRun = false;
        } while (!finished);


        /* return */
        HashMap<ISimplePolygon, ArrayList<Node>> startMap = new HashMap<>();
        HashMap<ISimplePolygon, ArrayList<Node>> endMap = new HashMap<>();

        startMap.put(in, start);
        for (int i = 0; i < ends.size(); i++) {
            endMap.put(out[i], ends.get(i));
        }

        return new StartEndScenarioDescription(startMap, endMap);
    }

    @Override
    public String toString() {
        return "in->out";
    }
}