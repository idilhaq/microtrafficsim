package microtrafficsim.core.logic.vehicles;

import microtrafficsim.core.frameworks.vehicle.ILogicVehicle;
import microtrafficsim.core.frameworks.vehicle.VehicleEntity;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.logic.Lane;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.interesting.emotions.Hulk;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.id.LongIDGenerator;

import java.util.Random;

/**
 * <p>
 * This class represents a vehicle for the logic based on the
 * Nagel-Schreckenberg-model. It extends this model by a dash factor and some
 * additional information.
 * </p>
 * <p>
 * Additional information: <br>
 * - angry factor: This factor represents the current mood of the vehicle. It
 * increases by one for each time the vehicle has a velocity of 0 for more than
 * one simulation step in following. In opposition to the total angry factor,
 * this factor gets reduced exponentially and is set to 0 after a time given by
 * {@link SimulationConfig}. <br>
 * - total angry factor: this factor represents the number of simulation steps
 * when the vehicle has had a velocity of 0 for more than one simulation step in
 * following.
 * </p>
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public abstract class AbstractVehicle implements ILogicVehicle, Hulk {

    // general
    public final long ID;
    private Random random;
    private VehicleEntity entity;
    private VehicleState state;
    private VehicleStateListener stateListener;
    // routing
    private Route route;
    private int age;
    private int spawnDelay;
    // traffic
    private Lane lane;
    private int cellPosition;
    private int velocity;
    private AbstractVehicle vehicleInFront;
    private AbstractVehicle vehicleInBack;
    private boolean hasDashed; // for simulation
    private byte priorityCounter; // for crossing logic
    private final Object lock_priorityCounter = new Object();
    // angry factor
    private boolean lastVelocityWasZero;

    public AbstractVehicle(LongIDGenerator longIDGenerator, VehicleStateListener stateListener, Route route) {

        this(longIDGenerator, stateListener, route, 0);
    }

    public AbstractVehicle(LongIDGenerator longIDGenerator,
                           VehicleStateListener stateListener,
                           Route route,
                           int spawnDelay) {

        this.ID = longIDGenerator.next();
        this.stateListener = stateListener;
        setState(VehicleState.NOT_SPAWNED);
        // routing
        this.route = route;
        age = 0;
        this.spawnDelay = spawnDelay;
        resetPriorityCounter();
        // traffic
        cellPosition = -1;
        velocity = 0;
        hasDashed = false;
        priorityCounter = 0;
        // interesting stuff
        lastVelocityWasZero = false;

        try {
            validateDashAndDawdleFactors(getDashFactor(), getDawdleFactor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(ID).getHash();
    }

    @Override
    public String toString() {
        String output = ID + "\n";
        output += "has spawned = " + (state == VehicleState.SPAWNED) + "\n";
        output += "route size = " + route.size() + "\n";
        output += "has front vehicle = " + (vehicleInFront != null) + "\n";
        output += "velocity = " + velocity + "\n";
        output += "cell position = " + cellPosition + "\n";
        output += "prio = " + priorityCounter + "\n";
        output += "route.getStart().permission = " + route.getStart().permissionToCross(this) + "\n";
        output += "route.getStart().hashCode() = " + route.getStart().hashCode() + "\n";
        if (state == VehicleState.SPAWNED) {
            output += "lane length = " + lane.getAssociatedEdge().getLength() + "\n";
            output += "permission to cross = " + lane.getAssociatedEdge().getDestination().permissionToCross(this)
                    + "\n";
//			output += "lane index at node = " + lane.getAssociatedEdge().getDestination().incomingEdges.get(lane.getAssociatedEdge()) + "\n";
//			output += "next node maxLaneIndex = " + lane.getAssociatedEdge().getDestination().maxLaneIndex + "\n";
//			output += "GEILER SHIT incoming size = " + lane.getAssociatedEdge().getDestination().incomingEdges.size() + "\n";
        }
        if (!route.isEmpty()) {
//			output += "route.peek().lane index at node = " + route.peek().getOrigin().leavingEdges.get(route.peek()) + "\n";
            output += "route-peek max insertion index = " + route.peek().getLane(0).getMaxInsertionIndex() + "\n";
            output += "route.peek().hashCode() = " + route.peek().hashCode() + "\n";
        }
        return output;
    }

    public int getAge() {
        return age - spawnDelay;
    }

    public Route getRoute() {
        return route;
    }

    public void setStateListener(VehicleStateListener listener) {
        if (listener != null)
            this.stateListener = listener;
    }

    public VehicleState getState() {
        return state;
    }

    private void setState(VehicleState state) {
        this.state = state;
        stateListener.stateChanged(this);
    }

    public DirectedEdge peekNextRouteSection() {
        return route.peek();
    }

    private synchronized void addVehicleInFront(AbstractVehicle vehicle) {
        this.vehicleInFront = vehicle;
        vehicle.vehicleInBack = this;
    }

    private synchronized void removeVehicleInBack() {
        if (vehicleInBack != null) {
            vehicleInBack.vehicleInFront = vehicleInFront;
            vehicleInBack = null;
        }
    }

    /*
    |===================|
    | (i) ILogicVehicle |
    |===================|
    */
    @Override
    public VehicleEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(VehicleEntity entity) {
        this.entity = entity;
        random = new Random(entity.getConfig().seed);
    }

    @Override
    public int getCellPosition() {
        return cellPosition;
    }

    @Override
    public DirectedEdge getDirectedEdge() {
        if (lane == null)
            return null;
        return lane.getAssociatedEdge();
    }

    /*
    |================|
    | crossing logic |
    |================|
    */
    public void resetPriorityCounter() {
        synchronized (lock_priorityCounter) {
            priorityCounter = 0;
        }
    }

    public void incPriorityCounter() {
        synchronized (lock_priorityCounter) {
            byte old = priorityCounter;
            priorityCounter++;
            if (old > priorityCounter) {
                try {
                    throw new Exception("Vehicle.incPriorityCounter() - byte overflow");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void decPriorityCounter() {
        synchronized (lock_priorityCounter) {
            byte old = priorityCounter;
            priorityCounter--;
            if (old < priorityCounter) {
                try {
                    throw new Exception("Vehicle.incPriorityCounter() - byte underflow");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte getPriorityCounter() {
        return priorityCounter;
    }

    // |========================|
    // | world/node interaction |
    // |========================|
    /**
     * This method registers the vehicle in the start node of the vehicle's
     * route. If the route is empty, the vehicle does not register itself and
     * despawns instantly.
     *
     * @return True, if spawning was successful; False if despawned.
     */
    public boolean register() {
        if (!route.isEmpty()) {
            route.getStart().registerVehicle(this);
            return true;
        } else {
            try {
                despawn();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * If this vehicle is not spawned yet, this method checks if the route is
     * empty. If yes, the vehicle will despawn instantly. If no, it has to
     * check, if it can cross the node.
     */
    public void spawn() {
        if (state == VehicleState.NOT_SPAWNED && age >= spawnDelay) {
            if (!route.isEmpty()) {
                if (!route.getStart().permissionToCross(this)) {
                    velocity = 0;
                } else { // allowed to spawn
                    if (route.peek().getLane(0).getMaxInsertionIndex() < 0) {
                        velocity = 0;
                    } else {
                        velocity = 1;
                        route.getStart().deregisterVehicle(this);
                        enterNextRoad();
                        setState(VehicleState.SPAWNED);
                    }
                }
            } else { // route is empty
                velocity = 0;

                try {
                    despawn();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        didOneSimulationStep();
    }

    private void despawn() {
        lane = null;
        setState(VehicleState.DESPAWNED);
    }

    /*
    |============|
    | simulation |
    |============|
    */
    public void accelerate() {
        if (velocity < getMaxVelocity())
            velocity++;
    }

    public void dash() {
        if (velocity < getMaxVelocity()) {
//            if (!hasDashed) TODO better dashing
                hasDashed = random.nextFloat() < getDashFactor();
            if (hasDashed)
                velocity++;
        }
    }

    public void brake() {
        if (state == VehicleState.SPAWNED) {
            if (vehicleInFront != null) {
                // brake for front vehicle
                int distance = vehicleInFront.cellPosition - cellPosition;
                velocity = Math.min(velocity, distance - 1);
            } else { // this vehicle is first in lane
                DirectedEdge edge = lane.getAssociatedEdge();
                int distance = edge.getLength() - cellPosition;
                // Would cross node?
                if (velocity >= distance)
                    if (route.isEmpty()) {
                        // brake for end of road
                        velocity = Math.min(velocity, distance - 1);
                    } else {
                        if (edge.getDestination().permissionToCross(this)) {
                            // if next road has vehicles => brake for this
                            // else => brake for end of next road
                            int maxInsertionIndex = route.peek().getLane(0).getMaxInsertionIndex();
                            velocity = Math.min(velocity, distance + maxInsertionIndex);
                        } else {
                            // brake for end of road
                            velocity = Math.min(velocity, distance - 1);
                        }
                    }
            }

            // brake for edges max velocity
            velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity());
            // better dashing
            //            if (hasDashed)
            //                velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity() + 1);
            //            else
            //                velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity());
        }

        if (velocity < 0)
            try {
                throw new Exception("velocity after brake < 0");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void dawdle() {
        if (!hasDashed) {
            if (velocity > 0 && state == VehicleState.SPAWNED) {
                //     P[dawdle] = P[dawdle | not dash] * P[not dash]
                // <=> P[dawdle | not dash] = P[dawdle] / (1 - P[dash])
                float p = getDawdleFactor() / (1 - getDashFactor());
                if (random.nextFloat() < p)
                    velocity--;
            }
        }
    }

    /**
     * Moves the vehicle depending on its position:<br>
     * &bull If it stand's at a node, it will leave the current road and enter
     * the next road.<br>
     * &bull If it stand's in the lane, it just drives at the next position depending on its velocity.
     */
    public void move() {

        if (state == VehicleState.SPAWNED) {
            DirectedEdge edge = lane.getAssociatedEdge();
            int distance = edge.getLength() - cellPosition;
            // Will cross node?
            if (velocity >= distance)
                if (!route.isEmpty()) {
                    leaveCurrentRoad();
                    enterNextRoad();
                } else {
                    leaveCurrentRoad();
                    try {
                        despawn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            else {
                // if standing at the end of the road
                // and route is empty
                // => despawn
                if (velocity == 0 && distance == 1 && route.isEmpty()) {
                    leaveCurrentRoad();
                    try {
                        despawn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    drive();
                }
            }
        }
    }

    public void didMove() {
        if (state == VehicleState.SPAWNED) {
            didOneSimulationStep();

            if (!route.isEmpty()) {
                int distance = lane.getAssociatedEdge().getLength() - cellPosition;
                int maxVelocity = Math.min(getMaxVelocity(), lane.getAssociatedEdge().getMaxVelocity());
                if (maxVelocity >= distance && vehicleInFront == null) {
                    lane.getAssociatedEdge().getDestination().registerVehicle(this);
                }
            }
        }
    }

    private void didOneSimulationStep() {
        // anger
        if (velocity == 0) {
            if (lastVelocityWasZero) {
                becomeMoreAngry();
            }
            lastVelocityWasZero = true;
        } else {
            if (!lastVelocityWasZero) {
                calmDown();
            }
            lastVelocityWasZero = false;
        }
        // age
        age++;
    }

    /**
     * @return Max velocity this vehicle is able to have.
     */
    protected abstract int getMaxVelocity();

    /**
     * @return The probability to dash in one simulation step.
     */
    protected abstract float getDashFactor();

    /**
     * @return The probability to dawdle in one simulation step.
     */
    protected abstract float getDawdleFactor();

    public static void validateDashAndDawdleFactors(float dashFactor, float dawdleFactor) throws Exception {
        if (dashFactor + dawdleFactor > 1)
            throw new Exception("(dash factor + dawdle factor) has to be <= 1");
        if (dashFactor < 0)
            throw new Exception("Dash factor has to be positive.");
        if (dawdleFactor < 0)
            throw new Exception("Dawdle factor has to be positive.");
    }

    /*
    |===============|
    | driving logic |
    |===============|
    */
    private void leaveCurrentRoad() {
        lane.getAssociatedEdge().getDestination().deregisterVehicle(this);
        synchronized (lane.lock) {
            lane.removeVehicle(this);
            removeVehicleInBack();
        }

        // -1 * distance to end of road
        cellPosition = cellPosition - lane.getAssociatedEdge().getLength();
    }

    private void enterNextRoad() {
        lane = route.poll().getLane(0);
        synchronized (lane.lock) {
            AbstractVehicle lastVehicle = lane.getLastVehicle();
            if (lastVehicle != null) {
                addVehicleInFront(lastVehicle);
            }
        }
        // in case that the vehicle is crossing a node:
        // cellPosition has to be negative
        cellPosition = cellPosition + velocity;
        lane.insertVehicle(this, cellPosition);
        entity.getVisualization().updatePosition();
    }

    private void drive() {
        lane.moveVehicle(this, velocity);
        cellPosition = cellPosition + velocity;
        entity.getVisualization().updatePosition();
    }
}