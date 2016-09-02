package microtrafficsim.core.shortestpath;


/**
 * This interface guarantees a unified access to needed edge functions for a shortest path algorithm. You can specify in
 * your extension of @AbstractAStarAlgorithm which weight method you want to use. Therefore it is okay to set one of the
 * weight methods to 0 if you don't use it.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public interface ShortestPathEdge {

    /**
     * @return Length of this edge in a certain unit.
     */
    int getLength();

    /**
     * @return Current usage, e.g. current_number_of_vehicles/{@link #getLength()}
     */
    float getCurrentUsage();

    /**
     * This method is for weights using time instead of length (in milliseconds). E.g. a motorway
     * could be passed faster than a shorter road, where you have to drive more
     * slowly.
     *
     * @return Time to pass this edge in milliseconds
     */
    float getTimeCostMillis();

    ShortestPathNode getOrigin();
    ShortestPathNode getDestination();
}