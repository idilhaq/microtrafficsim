package microtrafficsim.core.map;


/**
 * Describes the boundaries of a rectangular map-segment.
 *
 * @author Maximilian Luz
 */
public class Bounds implements Cloneable {
    public double minlat, minlon, maxlat, maxlon;

    /**
     * Constructs new {@code Bounds}:
     *
     * @param minlat the minimum latitude.
     * @param minlon the minimum longitude.
     * @param maxlat the maximum latitude.
     * @param maxlon the maximum longitude.
     */
    public Bounds(double minlat, double minlon, double maxlat, double maxlon) {
        this.minlat = minlat;
        this.minlon = minlon;
        this.maxlat = maxlat;
        this.maxlon = maxlon;
    }

    /**
     * Constructs new {@code Bounds}:
     *
     * @param min the minimum latitude and longitude.
     * @param max the maximum latitude and longitude.
     */
    public Bounds(Coordinate min, Coordinate max) {
        this.minlat = min.lat;
        this.minlon = min.lon;
        this.maxlat = max.lat;
        this.maxlon = max.lon;
    }

    /**
     * Copy-constructs new {@code Bounds} from the given ones.
     *
     * @param other the bounds to copy.
     */
    public Bounds(Bounds other) {
        this.minlat = other.minlat;
        this.minlon = other.minlon;
        this.maxlat = other.maxlat;
        this.maxlon = other.maxlon;
    }


    /**
     * Sets this bounds.
     *
     * @param minlat the new minimum latitude.
     * @param minlon the new minimum longitude.
     * @param maxlat the new maximum latitude.
     * @param maxlon the new maximum longitude.
     * @return this {@code Bounds}.
     */
    public Bounds set(double minlat, double minlon, double maxlat, double maxlon) {
        this.minlat = minlat;
        this.minlon = minlon;
        this.maxlat = maxlat;
        this.maxlon = maxlon;
        return this;
    }

    /**
     * Sets this bounds.
     *
     * @param min the new minimum latitude and longitude.
     * @param max the new maximum latitude and longitude.
     * @return this {@code Bounds}.
     */
    public Bounds set(Coordinate min, Coordinate max) {
        this.minlat = min.lat;
        this.minlon = min.lon;
        this.maxlat = max.lat;
        this.maxlon = max.lon;
        return this;
    }

    /**
     * Sets this bounds from the given other bounds.
     *
     * @param other the bounds to copy.
     * @return this {@code Bounds}.
     */
    public Bounds set(Bounds other) {
        this.minlat = other.minlat;
        this.minlon = other.minlon;
        this.maxlat = other.maxlat;
        this.maxlon = other.maxlon;
        return this;
    }


    /**
     * Returns the minimum latitude and longitude of these bounds.
     *
     * @return the minimum latitude and longitude of these bounds.
     */
    public Coordinate min() {
        return new Coordinate(minlat, minlon);
    }

    /**
     * Returns the maximum latitude and longitude of these bounds.
     *
     * @return the maximum latitude and longitude of these bounds.
     */
    public Coordinate max() {
        return new Coordinate(maxlat, maxlon);
    }


    @Override
    public String toString() {
        return this.getClass().getName() + " {" + minlat + ", " + minlon + ", " + maxlat + ", " + maxlon + "}";
    }
}
