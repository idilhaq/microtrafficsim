package microtrafficsim.ui.tdw.simulations;

import microtrafficsim.core.frameworks.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.FastestWayAStar;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.AbstractSimulation;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.math.Distribution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;


public class StartEndScenario extends AbstractSimulation {

    private final Random random;

    private ArrayList<Node> start;
    private ArrayList<Node> end;

    private int orderIdx;

    private int lastPercentage = 0;
    private final Integer percentageDelta = 5;

    private final Supplier<ShortestPathAlgorithm> scoutFactory;


    public StartEndScenario(SimulationConfig config, StreetGraph graph,
                            Supplier<IVisualizationVehicle> vehicleSupplier,
                            ArrayList<Node> start, ArrayList<Node> end) {
        this(config, graph, vehicleSupplier, createScoutFactory(config), start, end);
    }

    public StartEndScenario(SimulationConfig config, StreetGraph graph,
                            Supplier<IVisualizationVehicle> vehicleFactory,
                            Supplier<ShortestPathAlgorithm> scoutFactory,
                            ArrayList<Node> start, ArrayList<Node> end) {
        super(config, graph, vehicleFactory);
        this.scoutFactory = scoutFactory;
        this.start = start;
        this.end = end;
        this.random = new Random(config.seed);
    }

    @Override
    protected void prepareScenario() {}

    @Override
    protected void createAndAddVehicles(ProgressListener listener) {
        if (getConfig().multiThreading.nThreads > 1)
            multiThreadedVehicleCreation(listener);
        else
            singleThreadedVehicleCreation(listener);
    }

    private void singleThreadedVehicleCreation(ProgressListener listener) {

        final int maxVehicleCount = getConfig().maxVehicleCount;

        // calculate routes and create vehicles
        int successfullyAdded = 0;
        while (successfullyAdded < maxVehicleCount) {
            Node[] bla = findRouteNodes();
            Node start = bla[0];
            Node end = bla[1];
            // create route
            @SuppressWarnings("unchecked")
            Route route = new Route(start, end,
                    (Queue<DirectedEdge>) scoutFactory.get().findShortestPath(start, end));
            // create and add vehicle
            // has permission to create vehicle
            if (!route.isEmpty()) {
                // add route to vehicle and vehicle to graph
                createAndAddVehicle(new Car(getConfig(), this, route));
                successfullyAdded++;
            }

            logProgress(successfullyAdded, maxVehicleCount, listener);
        }
    }

    private void multiThreadedVehicleCreation(ProgressListener listener) {

        final int maxVehicleCount = getConfig().maxVehicleCount;
        final int nThreads = getConfig().multiThreading.nThreads;
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        ArrayList<Callable<Object>> todo = new ArrayList<>(nThreads);

        // deterministic/pseudo-random route + vehicle generation needs
        // variables for synchronization:
        orderIdx = 0;
        final int[] addedVehicles = {0};
        final Object lock_random = new Object();
        final ReentrantLock lock = new ReentrantLock(true);
        final boolean[] permission = new boolean[nThreads];
        Condition[] getPermission = new Condition[nThreads];
        for (int i = 0; i < getPermission.length; i++) {
            getPermission[i] = lock.newCondition();
            permission[i] = i == 0;
        }
        // distribute vehicle generation uniformly over all threads
        Iterator<Integer> bucketCounts = Distribution.uniformly(maxVehicleCount, nThreads);
        while (bucketCounts.hasNext()) {
            int bucketCount = bucketCounts.next();

            todo.add(Executors.callable(() -> {
                // calculate routes and create vehicles
                int successfullyAdded = 0;
                while (successfullyAdded < bucketCount) {
                    Node start, end;
                    int idx;
                    synchronized (lock_random) {
                        idx = orderIdx % nThreads;
                        orderIdx++;
                        Node[] bla = findRouteNodes();
                        start = bla[0];
                        end = bla[1];
                    }
                    // create route
                    Route route = new Route(start, end,
                            (Queue<DirectedEdge>) scoutFactory.get().findShortestPath(start, end));
                    // create and add vehicle
                    lock.lock();
                    // wait for permission
                    if (!permission[idx]) {
                        try {
                            getPermission[idx].await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // has permission to create vehicle
                    if (!route.isEmpty()) {
                        // add route to vehicle and vehicle to graph
                        createAndAddVehicle(new Car(getConfig(), this, route));
                        successfullyAdded++;
                        addedVehicles[0]++;
                    }
                    // let next thread finish its work
                    permission[idx] = false;
                    int nextIdx = (idx + 1) % nThreads;
                    if (lock.hasWaiters(getPermission[nextIdx]))
                        getPermission[nextIdx].signal();
                    else
                        permission[nextIdx] = true;

                    lock.unlock();

                    // nice output
                    logProgress(addedVehicles[0], maxVehicleCount, listener);
                }
            }));
        }
        try {
            pool.invokeAll(todo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logProgress(int finished, int total, ProgressListener listener) {

        int percentage = (100 * finished) / total;
        synchronized (percentageDelta) {
            if (percentage - lastPercentage >= percentageDelta) {
                getConfig().logger.info(percentage + "% vehicles created.");
                if (listener != null)
                    listener.didProgress(percentage);
                lastPercentage += percentageDelta;
            }
        }
    }

    private Node[] findRouteNodes() {
        return new Node[] { random(start), random(end) };
    }

    private Node random(ArrayList<Node> list) {
        return list.get(random.nextInt(list.size()));
    }

    private static Supplier<ShortestPathAlgorithm> createScoutFactory(SimulationConfig config) {
        return new Supplier<ShortestPathAlgorithm>() {
            private Random random = new Random(config.seed);

            @Override
            public ShortestPathAlgorithm get() {
                if (random.nextFloat() < .5f) {
                    return new FastestWayAStar(config.metersPerCell);
                } else {
                    return new LinearDistanceAStar(config.metersPerCell);
                }
            }
        };
    }
}
