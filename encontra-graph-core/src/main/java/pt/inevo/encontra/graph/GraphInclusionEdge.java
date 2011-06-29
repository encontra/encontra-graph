package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.util.EdgeType;


public class GraphInclusionEdge extends GraphEdge {

	public GraphInclusionEdge(GraphNode from, GraphNode to) {
		super(from, to);
	}

    @Override
    public EdgeType getType() {
        return EdgeType.DIRECTED;
    }
}
