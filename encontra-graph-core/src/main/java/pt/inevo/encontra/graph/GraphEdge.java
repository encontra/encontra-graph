package pt.inevo.encontra.graph;


public interface GraphEdge { //extends Edge{

	/*public GraphEdge(GraphNode from, GraphNode to) {
		super(from, to);
	}*/

	public int getId();
	/*{
		return id;
	}*/
	
	public void setId(int pid);
	/*{
		id=pid;
	}*/

	/*
	@Override
	public GraphNode getDest() {
		return (GraphNode) super.getDest();
	}

	@Override
	public GraphNode getSource() {
		return (GraphNode) super.getSource();
	}*/
}
