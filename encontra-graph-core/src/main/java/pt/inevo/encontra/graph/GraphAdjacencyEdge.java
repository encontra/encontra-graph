package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.Map;

public class GraphAdjacencyEdge extends GraphEdge {

    public GraphAdjacencyEdge(GraphNode from, GraphNode to) {
        super(from, to);
    }

    @Override
    public EdgeType getType() {
        return EdgeType.UNDIRECTED;
    }

    @Override
    public GraphAdjacencyEdge clone() {
        GraphAdjacencyEdge edge = new GraphAdjacencyEdge(from, to);
        edge.setId(id);

        for (Map.Entry<String, Object> entry : userDatum.entrySet()) {
            edge.setUserDatum(entry.getKey(), entry.getValue());
        }

        return edge;
    }
}
