package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.core.simulation.utils.UnmodifiableODMatrix;

/**
 * This class should only implement the basic stuff for children classes.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicScenario implements Scenario {

    private final ScenarioConfig    config;
    private final Graph graph;
    private final VehicleContainer  vehicleContainer;
    private       boolean           isPrepared;
    protected     ODMatrix          odMatrix;

    /**
     * Default constructor
     *
     * @param config this config is used for internal settings and should be set already
     * @param graph used for route definitions etc.
     * @param vehicleContainer stores and manages vehicles running in this scenario
     */
    protected BasicScenario(ScenarioConfig config,
                            Graph graph,
                            VehicleContainer vehicleContainer) {
        this.config = config;
        this.graph = graph;
        this.vehicleContainer = vehicleContainer;
        this.isPrepared = false;

        this.odMatrix = new SparseODMatrix();
    }

    protected BasicScenario(ScenarioConfig config, Graph graph) {
        this(config, graph, new ConcurrentVehicleContainer());
    }

    @Override
    public final ScenarioConfig getConfig() {
        return config;
    }

    @Override
    public final Graph getGraph() {
        return graph;
    }

    @Override
    public final VehicleContainer getVehicleContainer() {
        return vehicleContainer;
    }

    @Override
    public final void setPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    @Override
    public final boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public final UnmodifiableODMatrix getODMatrix() {
        return new UnmodifiableODMatrix(odMatrix);
    }
}
