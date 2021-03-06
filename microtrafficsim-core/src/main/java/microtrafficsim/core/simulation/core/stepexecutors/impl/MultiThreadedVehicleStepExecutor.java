package microtrafficsim.core.simulation.core.stepexecutors.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.core.stepexecutors.VehicleStepExecutor;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.exceptions.core.logic.NagelSchreckenbergException;
import microtrafficsim.utils.concurrency.delegation.StaticThreadDelegator;
import microtrafficsim.utils.concurrency.delegation.ThreadDelegator;

import java.util.concurrent.ExecutorService;


/**
 * A multi-threaded implementation of {@link VehicleStepExecutor} using a thread pool of {@link ExecutorService}.
 *
 * @author Dominic Parga Cacheiro
 */
public class MultiThreadedVehicleStepExecutor implements VehicleStepExecutor {

    // multithreading
    private final ThreadDelegator delegator;

    public MultiThreadedVehicleStepExecutor(int nThreads) {
        this(new StaticThreadDelegator(nThreads));
    }

    public MultiThreadedVehicleStepExecutor(ThreadDelegator delegator) {
        this.delegator = delegator;
    }

    @Override
    public void willMoveAll(final Scenario scenario) {
        try {
            delegator.doTask(
                    (Vehicle v) -> {
                        v.accelerate();
                        try {
                            v.brake();
                        } catch (NagelSchreckenbergException e) {
                            e.printStackTrace();
                        }
                        v.dawdle();
                    },
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveAll(final Scenario scenario) {
        try {
            delegator.doTask(Vehicle::move,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didMoveAll(final Scenario scenario) {
        try {
            delegator.doTask(Vehicle::didMove,
                    scenario.getVehicleContainer().getSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void spawnAll(final Scenario scenario) {
        try {
            delegator.doTask(Vehicle::spawn,
                    scenario.getVehicleContainer().getNotSpawnedVehicles().iterator(),
                    scenario.getConfig().multiThreading.vehiclesPerRunnable
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNodes(final Scenario scenario) {
        try {
            delegator.doTask(
                    Node::update,
                    scenario.getGraph().getNodes().iterator(),
                    scenario.getConfig().multiThreading.nodesPerThread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
