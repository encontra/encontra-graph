package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;


public class GraphInclusionEdge extends DirectedSparseEdge{

	public GraphInclusionEdge(GraphNode from, GraphNode to) {
		super(from, to);
	}

	public int getId()
	{
		return id;
	}
	
	public void setId(int pid)
	{
		id=pid;
	}
}
