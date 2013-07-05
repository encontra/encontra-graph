package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.util.EdgeType;
import pt.inevo.encontra.storage.IEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic GraphEdge (Edge).
 */
public abstract class GraphEdge implements IEntity<Long>, Cloneable {

    protected GraphNode from, to;
    protected Long id;
    protected Map<String, Object> userDatum;

    public GraphEdge(GraphNode from, GraphNode to) {
        this.setSource(from);
        this.setDest(to);
        userDatum = new HashMap<String, Object>();
    }

    @Override
	public Long getId() {
        return id;
    }

    @Override
	public void setId(Long pid) {
        this.id = pid;
    }

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

    @Override
    abstract public GraphEdge clone();


   public boolean isIncident(GraphNode n) {
       if (getType().equals(EdgeType.UNDIRECTED)) {
         return n.equals(from) || n.equals(to);
       } else {
           return n.equals(to);
       }
   }
}
