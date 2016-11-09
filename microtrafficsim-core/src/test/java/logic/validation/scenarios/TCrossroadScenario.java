package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;


/**
 * This class defines a concrete scenarios of a simulation. This means it
 * contains an extension of {@link SimulationConfig} to define simulation
 * parameters. Furthermore, this class serves with a list of uniformly randomly
 * chosen start and end nodes for the vehicles and an age that counts the
 * finished simulation steps.
 *
 * @author Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public class TCrossroadScenario extends ValidationScenario {

    private static final String OSM_FILENAME = "T_crossroad.osm";
    private Node                bottom       = null;
    private Node                topRight     = null;
    private Node                topLeft      = null;
    private NextScenarioState   nextScenarioState;

    /**
     * Default constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public TCrossroadScenario(SimulationConfig config, StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> vehicleFactory) {
        super(config, graph, vehicleFactory);
        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
    }

    private static void setupConfig(SimulationConfig config) {

        // super attributes
        config.longIDGenerator                         = new ConcurrentLongIDGenerator();
        config.speedup                                 = 5;
        config.maxVehicleCount                         = 3;
        config.crossingLogic.drivingOnTheRight         = true;
        config.crossingLogic.edgePriorityEnabled       = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        config.crossingLogic.friendlyStandingInJamEnabled = false;
        // own attributes
        config.ageForPause = -1;
    }

    public static void main(String[] args) throws Exception {
        File file = new PackagedResource(Main.class, OSM_FILENAME).asTemporaryFile();

        SimulationConfig config = new SimulationConfig();
        setupConfig(config);
        Main.show(config.visualization.projection, file, config, TCrossroadScenario.class);
    }

    @Override
    protected void updateScenarioState(VehicleState vehicleState) {
        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED && getVehicleContainer().getVehicleCount() == 0) {
            switch (nextScenarioState) {
            case PRIORITY_TO_THE_RIGHT:
                createAndAddCar(topRight, topLeft, 0,                    Color.fromRGB(0xCC4C1A));
                createAndAddCar(bottom,   topLeft, 1 + updateGraphDelay, Color.fromRGB(0x3EAAAB));
                nextScenarioState = NextScenarioState.NO_INTERCEPTION;
                break;

            case NO_INTERCEPTION:
                createAndAddCar(topRight, topLeft,  0,                    Color.fromRGB(0xCC4C1A));
                createAndAddCar(bottom,   topRight, 1 + updateGraphDelay, Color.fromRGB(0x3EAAAB));
                nextScenarioState = NextScenarioState.DEADLOCK;
                break;

            case DEADLOCK:
                createAndAddCar(topRight, topLeft,  0,                    Color.fromRGB(0xCC4C1A));
                createAndAddCar(topLeft,  topRight, 1 + updateGraphDelay, Color.fromRGB(0x3EAAAB));
                createAndAddCar(bottom,   topLeft,  1 + updateGraphDelay, Color.fromRGB(0x88B03F));
                nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
            }
        }
    }

    @Override
    protected void createAndAddVehicles(ProgressListener listener) {
        /* sort by lon */
        Iterator<Node>  iter        = graph.getNodeIterator();
        ArrayList<Node> sortedNodes = new ArrayList<>(4);
        while (iter.hasNext())
            sortedNodes.add(iter.next());

        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        topLeft  = sortedNodes.get(0);
        bottom   = sortedNodes.get(2);
        topRight = sortedNodes.get(3);

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }

    private enum NextScenarioState { PRIORITY_TO_THE_RIGHT, NO_INTERCEPTION, DEADLOCK }
}
