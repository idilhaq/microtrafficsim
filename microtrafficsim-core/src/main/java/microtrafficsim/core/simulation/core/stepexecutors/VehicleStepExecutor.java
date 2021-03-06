package microtrafficsim.core.simulation.core.stepexecutors;

import microtrafficsim.core.simulation.scenarios.Scenario;


/**
 * This interface serves methods for executing one simulation step of vehicles etc. (e.g. nodes).
 *
 * @author Dominic Parga Cacheiro
 */
public interface VehicleStepExecutor {

    /*
    |==========================|
    | vehicle simulation steps |
    |==========================|
     */
    /**
     * This method prepares the moving step, e.g. accelerating.
     *
     * @param scenario The scenario holding an iterator over all spawned vehicles getting prepared for moving.
     */
    void willMoveAll(final Scenario scenario);

    /**
     * This method moves all spawned vehicles.
     *
     * @param scenario The scenario holding an iterator over all spawned vehicles getting prepared for moving.
     */
    void moveAll(final Scenario scenario);

    /**
     * After moving all spawned vehicles, some calculations has to be done, e.g. registration at crossroads.
     *
     * @param scenario The scenario holding an iterator over all spawned vehicles getting prepared for moving.
     */
    void didMoveAll(final Scenario scenario);

    /**
     * After executing tasks for spawned vehicles, there is space for not spawned ones => spawn them.
     *
     * @param scenario The scenario holding an iterator over all not-spawned vehicles getting prepared for moving.
     */
    void spawnAll(final Scenario scenario);

    /**
     * After executing all vehicle tasks, the nodes has to update their priority lists and other logical stuff (e.g.
     * traffic lights if implemented).
     *
     * @param scenario The scenario holding the {@code StreetGraph} and {@code SimulationConfig}
     */
    void updateNodes(final Scenario scenario);
}