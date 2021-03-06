package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.map.area.polygons.RectangleArea;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * <p>
 * {@link #getScoutFactory()} returns a bidirectional A-star algorithm.
 *
 * @author Dominic Parga Cacheiro
 */
public class EndOfTheWorldScenario extends BasicRandomScenario {

    private static Logger logger = new EasyMarkableLogger(EndOfTheWorldScenario.class);

    // matrix
    private final ArrayList<Node> nodes, leftNodes, bottomNodes, rightNodes, topNodes;

    public EndOfTheWorldScenario(long seed,
                                 ScenarioConfig config,
                                 Graph graph,
                                 VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    public EndOfTheWorldScenario(Random random,
                                 ScenarioConfig config,
                                 Graph graph,
                                 VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);

        /* prepare building matrix */
        nodes       = new ArrayList<>(graph.getNodes());
        leftNodes   = new ArrayList<>();
        bottomNodes = new ArrayList<>();
        rightNodes  = new ArrayList<>();
        topNodes    = new ArrayList<>();
        // define areas for filling node lists
        float latLength   = Math.min(0.01f, 0.1f * (graph.getMaxLat() - graph.getMinLat()));
        float lonLength   = Math.min(0.01f, 0.1f * (graph.getMaxLon() - graph.getMinLon()));
        Area leftBorder   = new RectangleArea(
                graph.getMinLat(),
                graph.getMinLon(),
                graph.getMaxLat(),
                graph.getMinLon() + lonLength);
        Area bottomBorder = new RectangleArea(
                graph.getMinLat(),
                graph.getMinLon(),
                graph.getMinLat() + latLength,
                graph.getMaxLon());
        Area rightBorder  = new RectangleArea(
                graph.getMinLat(),
                graph.getMaxLon() - lonLength,
                graph.getMaxLat(),
                graph.getMaxLon());
        Area topBorder    = new RectangleArea(
                graph.getMaxLat() - latLength,
                graph.getMinLon(),
                graph.getMaxLat(),
                graph.getMaxLon());

        // fill node lists
        for (Node node : nodes) {
            if (leftBorder.contains(node))
                leftNodes.add(node);

            if (bottomBorder.contains(node))
                bottomNodes.add(node);

            if (rightBorder.contains(node))
                rightNodes.add(node);

            if (topBorder.contains(node))
                topNodes.add(node);
        }

        /* init */
        fillMatrix();
    }

    /**
     * <p>
     * Until enough vehicles (defined in {@link ScenarioConfig}) are created, this method is doing this:<br>
     * &bull get random origin <br>
     * &bull calculate its position relative to the graph's center <br>
     * &bull get a random destination out of the border field (of nodes) being closest to the chosen origin
     * &bull increase the route count for the found origin-destination-pair
     */
    @Override
    protected void fillMatrix() {
        // note: the directions used in this method's comments are referring to Europe (so the northern hemisphere)
        logger.info("BUILDING ODMatrix started");

        Random random = new Random(getSeed());
        Function<List<Node>, Node> getRandomNode = nodes -> nodes.get(random.nextInt(nodes.size()));

        odMatrix.clear();
        // build matrix
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            Node origin = getRandomNode.apply(nodes);

            // get end node depending on start node's position
            Graph graph    = getGraph();
            final float
                    minlat = graph.getMinLat(),
                    maxlat = graph.getMaxLat(),
                    minlon = graph.getMinLon(),
                    maxlon = graph.getMaxLon();
            Coordinate
                    center      = new Coordinate((maxlat + minlat) / 2, (maxlon + minlon) / 2),
                    originCoord = origin.getCoordinate();
            // set data relevant for distance calculation
            Coordinate
                    latProjection = new Coordinate(0, originCoord.lon),
                    lonProjection = new Coordinate(originCoord.lat, 0);
            ArrayList<Node> latNodes, lonNodes;

            if (center.lat - originCoord.lat > 0) {
                // origin is below from center
                latProjection.lat = minlat;
                latNodes          = bottomNodes;
            } else {
                // origin is over center
                latProjection.lat = maxlat;
                latNodes          = topNodes;
            }
            if (center.lon - originCoord.lon > 0) {
                // origin is left from center
                lonProjection.lon = minlon;
                lonNodes          = leftNodes;
            } else {
                // origin is right from center
                lonProjection.lon = maxlon;
                lonNodes          = rightNodes;
            }

            double latDistance = HaversineDistanceCalculator.getDistance(originCoord, latProjection);
            double lonDistance = HaversineDistanceCalculator.getDistance(originCoord, lonProjection);
            Node destination = latDistance > lonDistance ? getRandomNode.apply(lonNodes) : getRandomNode.apply(latNodes);
            odMatrix.inc(origin, destination);
        }

        logger.info("BUILDING ODMatrix finished");
    }
}
