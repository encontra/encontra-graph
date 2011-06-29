package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.util.EdgeType;

public class GraphAdjacencyEdge extends GraphEdge {

    public GraphAdjacencyEdge(GraphNode from, GraphNode to) {
        super(from, to);
    }

    @Override
    public EdgeType getType() {
        return EdgeType.UNDIRECTED;
    }
}
