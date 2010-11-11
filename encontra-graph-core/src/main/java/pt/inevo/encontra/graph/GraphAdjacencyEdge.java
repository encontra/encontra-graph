package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;


public class GraphAdjacencyEdge extends UndirectedSparseEdge implements GraphEdge { 

	public GraphAdjacencyEdge(GraphNode from, GraphNode to) {
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
