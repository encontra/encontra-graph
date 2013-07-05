package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.Map;

public class GraphInclusionEdge extends GraphEdge {

	public GraphInclusionEdge(GraphNode from, GraphNode to) {
		super(from, to);
	}

    @Override
    public EdgeType getType() {
        return EdgeType.DIRECTED;
    }

    @Override
    public GraphInclusionEdge clone() {
        GraphInclusionEdge edge = new GraphInclusionEdge(from, to);
        edge.setId(id);

        for (Map.Entry<String, Object> entry : userDatum.entrySet()) {
            edge.setUserDatum(entry.getKey(), entry.getValue());
        }

        return edge;
    }
}
