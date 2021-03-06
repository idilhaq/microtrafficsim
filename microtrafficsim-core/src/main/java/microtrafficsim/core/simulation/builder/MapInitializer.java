package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.logic.streetgraph.Graph;

/**
 * @author Dominic Parga Cacheiro
 */
public interface MapInitializer {

    /**
     * After the {@code StreetGraph} has been created, there are some tasks to be done before the graph can be used.
     * This includes {@link #postprocessGraph(Graph, long)}. This method can be called twice, but it is
     * not necessary. {@link #postprocessGraph(Graph, long)} should be called to postprocess a already
     * prepared graph.
     *
     * @param protoGraph the generated, raw graph
     * @param seed this parameter can be used for initializing random variables etc.
     * @return the same graph instance as the parameter; just for practical purposes
     */
    Graph postprocessFreshGraph(Graph protoGraph, long seed);

    /**
     * Resets the graph and prepares the graph independent of the fact whether it has just been created or not, e.g.
     * setting seeds.
     *
     * @param protoGraph the created/reset graph
     * @param seed this parameter can be used for initializing random variables etc.
     * @return the same graph instance as the parameter; just for practical purposes
     *
     * @see #postprocessFreshGraph(Graph, long) postprocessFreshGraph(...) for more information
     */
    Graph postprocessGraph(Graph protoGraph, long seed);
}
