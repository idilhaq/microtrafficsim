package microtrafficsim.core.logic;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This graph just saves all @Node#s and all @DirectedEdge#s in a @HashSet. All
 * dependencies between nodes and edges are saved in these classes, not in this
 * graph.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class StreetGraph {

    private Set<Node> nodes;
    private HashSet<DirectedEdge> edges;
    public final float minlat, maxlat, minlon, maxlon;

    /**
     * Just a standard constructor.
     */
    public StreetGraph(float minlat, float maxlat, float minlon, float maxlon) {
        this.minlat = minlat;
        this.maxlat = maxlat;
        this.minlon = minlon;
        this.maxlon = maxlon;
        nodes = new HashSet<>();
        edges = new HashSet<>();
    }

    public int getNumberOfNodes() {
        return nodes.size();
    }

    /**
     * // todo remove?!
     * @return Iterator over all nodes
     */
    public Iterator<Node> getNodeIterator() {
        return nodes.iterator();
    }

    public Set<Node> getNodeSet() {
        // todo unmodifiable!
        return nodes;
    }

    // |==================|
    // | change structure |
    // |==================|
    /**
     * Adds the {@link Node}s of the given {@link DirectedEdge} to the node set
     * and the edge to the edge set.
     *
     * @param edge
     *            This edge will be added to the graph.
     */
    public void registerEdgeAndNodes(DirectedEdge edge) {
        nodes.add(edge.getOrigin());
        nodes.add(edge.getDestination());
        edges.add(edge);
    }

    /**
     * Calculates the edge indices for all nodes in this graph.
     * <p>
     * IMPORTANT:<br>
     * This method must be called after all nodes and edges are added to the
     * graph. Otherwise some edges would have no index and crossing logic would
     * be unpredictable.
     * </p>
     */
    public void calcEdgeIndicesPerNode() {
        nodes.forEach(Node::calculateEdgeIndices);
    }

    @Override
    public String toString() {
        String output = "|==========================================|\n";
        output += "| Graph\n";
        output += "|==========================================|\n";

        output += "> Nodes\n";
        for (Node node : nodes) {
            output += node + "\n";
        }

        output += "> Edges\n";
        for (DirectedEdge edge : edges) {
            output += edge + "\n";
        }

        return output;
    }

    /**
     * This method just calls "vehicle.spawn()". For more information, see
     * {@link AbstractVehicle}.{@link AbstractVehicle#spawn spawn()}.
     *
     * @param vehicle
     *            This vehicle should be added to the StreetGraph.
     * @return True, if spawning was successful; False if despawned.
     */
    public boolean addVehicle(AbstractVehicle vehicle) {
        return vehicle.register();
    }

    public void reset() {
        nodes.forEach(Node::reset);
        edges.forEach(DirectedEdge::reset);
    }
}