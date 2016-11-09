package microtrafficsim.core.simulation;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.containers.VehicleContainer;
import microtrafficsim.interesting.progressable.ProgressListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;


/**
 * This interface serves methods to organize a simulation.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Simulation extends VehicleStateListener {
    SimulationConfig getConfig();

    /*
    |========================|
    | simulation preparation |
    |========================|
    */
    /**
     * @return True, if {@link #prepare()} has finished;
     * False otherwise
     */
    boolean isPrepared();

    /**
     * This method clears all vehicle lists and should initialize all important aspects, e.g. creating vehicles and
     * routes. After finishing, {@link #isPrepared()} will return true.
     */
    void prepare();

    /**
     * This method clears all vehicle lists and should initialize all important aspects, e.g. creating vehicles and
     * routes. After finishing, {@link #isPrepared()} will return true.
     *
     * @param listener This listener gets informed if necessary changes are made.
     */
    void prepare(ProgressListener listener);

    /*
    |==================|
    | simulation steps |
    |==================|
    */
    /**
     * @return Number of finished simulation steps.
     */
    int getAge();

    /**
     * <p>
     * This method starts calling the simulation steps repeatedly. Nothing will
     * be done if the simulation is already running.
     *
     * <p>
     * The repeated simulation steps are started by a {@link Timer}. You can
     * stop it by calling {@link #cancel()}. You can call {@link #isPaused()} to
     * ask if the simulation is paused.
     */
    void run();

    /**
     * <p>
     * This method is called before each simulation step. The default
     * implementation does nothing.
     */
    default void willRunOneStep() {}

    /**
     * Does the same as {@link #doRunOneStep()} but only if the simulation is paused
     * ({@link #isPaused()} {@code == true}). Nothing will be done if the simulation is already running.
     */
    void runOneStep();

    /**
     * <p>
     * This method does one simulation step if the simulation is prepared.
     *
     * <p>
     * Calls {@link #willRunOneStep()} before a step and {@link #didRunOneStep()} after it.
     */
    void doRunOneStep();

    /**
     * <p>
     * This method is called after each simulation step. The default
     * implementation does nothing.
     */
    default void didRunOneStep() {}

    /**
     * This method should stop iterating the simulation steps after the
     * currently running simulation step and letting {@link #isPaused()}
     * returning true. One way could be using {@link Timer#cancel()} of
     * {@link Timer}.
     */
    void cancel();

    /**
     * @return True, if the simulation does nothing; False if it is running.
     */
    boolean isPaused();

    /*
    |==========|
    | vehicles |
    |==========|
    */
    /**
     * @return An instance of the {@link VehicleContainer} used in this simulation to store and manage vehicles.
     */
    VehicleContainer getVehicleContainer();

    /**
     * This method adds the given vehicle to the graph and if success, this
     * method adds the vehicle to the list of not yet spawned vehicles, too.
     *
     * @param vehicle should be added
     * @return True, if vehicle has been added to the graph; False otherwise
     */
    boolean addVehicle(AbstractVehicle vehicle);
}