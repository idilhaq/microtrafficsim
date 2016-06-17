package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.SimplePolygon;
import microtrafficsim.core.simulation.configs.SimulationConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;


public abstract class StartEndScenarioBuilder {

    public static class StartEndScenarioDescription {
        public final HashMap<SimplePolygon, ArrayList<Node>> start;
        public final HashMap<SimplePolygon, ArrayList<Node>> end;

        public StartEndScenarioDescription(HashMap<SimplePolygon, ArrayList<Node>> start,
                                           HashMap<SimplePolygon, ArrayList<Node>> end) {
            this.start = start;
            this.end = end;
        }
    }

    public StartEndScenario create(StartEndScenarioDescription description,
                                   SimulationConfig config, StreetGraph graph,
                                   Supplier<IVisualizationVehicle> vehicleSupplier) {
        ArrayList<Node> start = merge(description.start.values());
        ArrayList<Node> end = merge(description.end.values());
        return new StartEndScenario(config, graph, vehicleSupplier, start, end);
    }

    public abstract StartEndScenarioDescription createDescription(StreetGraph graph);


    protected static ArrayList<Node> getNodesFrom(StreetGraph graph, SimplePolygon polygon) {
        ArrayList<Node> nodes = new ArrayList<>();

        graph.getNodeIterator().forEachRemaining(node -> {
            if (polygon.contains(node.getCoordinate()))
                nodes.add(node);
        });

        return nodes;
    }

    private static ArrayList<Node> merge(Collection<ArrayList<Node>> c) {
        ArrayList<Node> all = new ArrayList<>();
        for (ArrayList<Node> list : c)
            all.addAll(list);
        return all;
    }
}
