package microtrafficsim.core.logic.streets;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class represents one street lane inside a {@link DirectedEdge}.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Lane implements Resettable {

    public final ReentrantLock lock;
    private DirectedEdge associatedEdge;
    private int          index;    // index in the list of lanes from the associated edge
    private HashMap<Integer, Vehicle> cells;
    private Vehicle lastVehicle;

    Lane(DirectedEdge container, int index) {
        lock                = new ReentrantLock(true);
        this.associatedEdge = container;
        this.index          = index;
        cells               = new HashMap<>();
        lastVehicle         = null;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(associatedEdge).add(index).getHash();
    }

    /**
     * Clears all cells and sets the last vehicle to null.
     */
    @Override
    public void reset() {
        this.cells.clear();
        this.lastVehicle = null;
    }

    public synchronized int getVehicleCount() {
        return cells.size();
    }

    public DirectedEdge getAssociatedEdge() {
        return associatedEdge;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        LevelStringBuilder stringBuilder = new LevelStringBuilder();
        stringBuilder.appendln("<Lane>");
        stringBuilder.incLevel();

        stringBuilder.append(associatedEdge);
        stringBuilder.appendln("lane index = " + index);

        stringBuilder.decLevel();
        stringBuilder.appendln("<\\Lane>");
        return stringBuilder.toString();
    }

    /*
    |=======================|
    | vehicle communication |
    |=======================|
    */
    public synchronized int getMaxInsertionIndex() {
        if (lastVehicle == null)
            return associatedEdge.getLength() - 1;
        else
            return lastVehicle.getCellPosition() - 1;
    }

    public synchronized void moveVehicle(Vehicle vehicle, int delta) {
        if (delta > 0) cells.remove(vehicle.getCellPosition());
        cells.put(vehicle.getCellPosition() + delta, vehicle);
    }

    public synchronized void insertVehicle(Vehicle vehicle, int pos) {
        Vehicle removedVehicle = cells.put(pos, vehicle);
        if (removedVehicle != null) try {
                throw new Exception("Inserting (\n" + vehicle + "\n) to the lane removed vehicle (\n" + removedVehicle
                                    + "\n)");
            } catch (Exception e) { e.printStackTrace(); }
        lastVehicle = vehicle;
    }

    public synchronized void removeVehicle(Vehicle vehicle) {
        Vehicle removedVehicle = cells.remove(vehicle.getCellPosition());
        if (removedVehicle != vehicle) try {
                throw new Exception("Removed vehicle (" + removedVehicle + ") in lane is not the expected one ("
                                    + vehicle + "). ");
            } catch (Exception e) { e.printStackTrace(); }
        if (cells.size() == 0) lastVehicle = null;
    }

    public synchronized Vehicle getLastVehicle() {
        return lastVehicle;
    }
}