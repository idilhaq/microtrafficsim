package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.ISimplePolygon;
import microtrafficsim.core.map.area.RectangleArea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class OutInScenarioBuilder extends StartEndScenarioBuilder {

    @Override
    public StartEndScenarioDescription createDescription(StreetGraph graph) {

// TODO TODO
        /* init help variables */
        Coordinate graphCenter = new Coordinate(
                (graph.maxlat + graph.minlat) / 2,
                (graph.maxlon + graph.minlon) / 2
        );
        double graphWidth = graph.maxlon - graph.minlon;
        double graphHeight = graph.maxlon - graph.minlon;
        ISimplePolygon out;
        ISimplePolygon[] in = new ISimplePolygon[4];
        HashSet<Node> alreadyAddedNodes = new HashSet<>();
        ArrayList<Node> end = new ArrayList<>();
        ArrayList<ArrayList<Node>> starts = new ArrayList<>(in.length);
        for (ISimplePolygon ignored : in) starts.add(new ArrayList<>());
        boolean finished;

        double percentage = 0.1;
        boolean firstRun = true;
        do {
            /* set start/end polygons */
            out = new RectangleArea(
                    graphCenter.lat - 2*percentage * graphHeight,
                    graphCenter.lon - 2*percentage * graphWidth,
                    graphCenter.lat + 2*percentage * graphHeight,
                    graphCenter.lon + 2*percentage * graphWidth
            );
            // left
            in[0] = new RectangleArea(
                    graph.minlat,
                    graph.minlon,
                    graph.maxlat,
                    graph.minlon + percentage * graphWidth
            );
            // bottom
            in[1] = new RectangleArea(
                    graph.minlat,
                    graph.minlon + percentage * graphWidth,
                    graph.minlat + percentage * graphHeight,
                    graph.maxlon - percentage * graphWidth
            );
            // right
            in[2] = new RectangleArea(
                    graph.minlat,
                    graph.maxlon - percentage * graphWidth,
                    graph.maxlat,
                    graph.maxlon
            );
            // top
            in[3] = new RectangleArea(
                    graph.maxlat - percentage * graphHeight,
                    graph.minlon + percentage * graphWidth,
                    graph.maxlat,
                    graph.maxlon - percentage * graphWidth
            );


            for (Node node : graph.getNodeSet()) {
                if (!alreadyAddedNodes.contains(node)) {
                    boolean added = false;
                    if ((firstRun || end.isEmpty()) && out.contains(node)) {
                        end.add(node);
                        added = true;
                    }
                    for (int i = 0; i < in.length; i++) {
                        if ((firstRun || starts.get(i).isEmpty()) && in[i].contains(node)) {
                            starts.get(i).add(node);
                            added = true;
                            break;
                        }
                    }
                    if (added)
                        alreadyAddedNodes.add(node);
                }
            }


            /* prepare next run */
            finished = !end.isEmpty();
            for (ArrayList<Node> start : starts) {
                finished = finished && !start.isEmpty();
            }
            percentage = percentage + 0.1;
            finished = finished || (percentage > 1);
            firstRun = false;
        } while (!finished);


        /* return */
        HashMap<ISimplePolygon, ArrayList<Node>> endMap = new HashMap<>();
        HashMap<ISimplePolygon, ArrayList<Node>> startMap = new HashMap<>();

        for (int i = 0; i < starts.size(); i++) {
            startMap.put(in[i], starts.get(i));
        }
        endMap.put(out, end);

        return new StartEndScenarioDescription(startMap, endMap);
    }

    @Override
    public String toString() {
        return "out->in";
    }
}