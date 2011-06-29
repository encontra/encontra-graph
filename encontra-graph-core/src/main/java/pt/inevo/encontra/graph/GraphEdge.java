package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.util.EdgeType;
import pt.inevo.encontra.storage.IEntity;

import java.util.HashMap;
import java.util.Map;

public abstract class GraphEdge implements IEntity<Long> { //extends Edge{

	/*public GraphEdge(GraphNode from, GraphNode to) {
		super(from, to);
	}*/

    private GraphNode from;
    private GraphNode to;
    private Long id;
    private Map<String, Object> userDatum;

    public GraphEdge(GraphNode from, GraphNode to) {
        this.setSource(from);
        this.setDest(to);
        userDatum = new HashMap<String, Object>();
    }

    @Override
	public Long getId() {
        return id;
    }
	/*{
		return id;
	}*/

    @Override
	public void setId(Long pid) {
        this.id = pid;
    }
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

    public GraphNode getSource() {
        return from;
    }

    public void setSource(GraphNode from) {
        this.from = from;
    }

    public GraphNode getDest() {
        return to;
    }

    public void setDest(GraphNode to) {
        this.to = to;
    }

    public Object getUserDatum(String key) {
        return userDatum.get(key);
    }

    public Object setUserDatum(String key, Object datum) {
        return userDatum.put(key, datum);
    }

    public abstract EdgeType getType();
}
