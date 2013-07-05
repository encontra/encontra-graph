package pt.inevo.encontra.graph;

import pt.inevo.encontra.storage.IEntity;

import java.util.*;

/**
 * EnContRA framework GraphNode (Vertex).
 * @param <T>
 */
public class GraphNode<T> implements IEntity<Long>, Cloneable  {

    private Long id;
    private T data;
    private Graph graph;
    private Map<String, Object> userDatum;

    /**
     * Default constructor.
     * Constructs a new Node with empty inclusion- and adjacency lists.
     * The Node's id is set to -1.
     */
    public GraphNode() {
        this(-1l);
    }

    /**
     * Constructor.
     * Constructs a new Node with empty inclusion- and adjacency lists.
     *
     * @param id The id of the Node.
     */
    public GraphNode(Long id) {
        this.id = id;
        data = null;
        userDatum = new HashMap<String, Object>();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setData(T d) {
        data = d;
    }

    public GraphAdjacencyEdge getAdjacencyTo(GraphNode no) {
        Collection<GraphEdge> edges = getGraph().findEdgeSet(this, no);
        for (GraphEdge e : edges) {
            if (e instanceof GraphAdjacencyEdge) {
                return (GraphAdjacencyEdge) e;
            }
        }
        return null;
    }

    public boolean isAdjentTo(GraphNode no) {
        if (no == this)
            return true;
        Collection<GraphEdge> edges = getGraph().findEdgeSet(this, no);
        for (GraphEdge e : edges) {
            if (e instanceof GraphAdjacencyEdge) {
                return true;
            }
        }
        return false;
    }

    public void remAdjLink(GraphNode no) {
        if (no != this && this.isAdjentTo(no)) {
            Collection<GraphEdge> edges = getGraph().findEdgeSet(this, no);
            for (GraphEdge e : edges) {
                if (e instanceof GraphAdjacencyEdge) {
                    getGraph().removeEdge(e);
                }
            }
        }
    }

    public GraphAdjacencyEdge addAdjLink(GraphNode no) {
        return addAdjLink(no, false);
    }

    public GraphAdjacencyEdge addAdjLink(GraphNode no, boolean allowDuplicates) {

        if (no != this) {
            int adjCount = 0;
            // Allow at most two adjency edges
            if (allowDuplicates) {
                Collection<GraphEdge> edges = getGraph().findEdgeSet(this, no);

                for (GraphEdge e : edges) {
                    if (e instanceof GraphAdjacencyEdge) {
                        adjCount++;
                    }
                }
            }

            if (!this.isAdjentTo(no) || (allowDuplicates && adjCount <= 1)) {
                GraphAdjacencyEdge edge = new GraphAdjacencyEdge(this, no);
                getGraph().addEdge(edge, this, no, edge.getType());
                return edge;
            }
        }

        return null;
    }

    /**
     * Adds an inclusion link from this Node to the Node given as argument.
     *
     * @param no The node to become this Node's child.
     * @return True iff the insertion was succesful, false otherwise.
     */
    public boolean addIncLink(GraphNode no) {
        if (no != this && !this.isAdjentTo(no)) {
            GraphEdge edge = new GraphInclusionEdge(this, no);
            getGraph().addEdge(edge, this, no, edge.getType()); //_incList->push(no);
            return true;
        } else {
            return false;
        }
    }

    public List<GraphNode> getAdjList() {
        List<GraphNode> list = new ArrayList<GraphNode>();
        Collection<GraphEdge> edges = getGraph().getOutEdges(this);
        if (edges != null) {
            for (GraphEdge e : edges) {
                if (e instanceof GraphAdjacencyEdge)
                    list.add((GraphNode) getGraph().getOpposite(this, e));
            }
        }
        return list;
    }

    public List<GraphNode> getIncList() {
        List<GraphNode> list = new ArrayList<GraphNode>();
        Collection<GraphEdge> edges = getGraph().getOutEdges(this);
        for (GraphEdge e : edges) {
            if (e instanceof GraphInclusionEdge)
                list.add(e.getDest());
        }
        return list;
    }

    public GraphNode getParent() {
        Collection<GraphEdge> inEdges = getGraph().getInEdges(this);
        for (GraphEdge e : inEdges) {
            if (e instanceof GraphInclusionEdge)
                return e.getSource();
        }
        return null;
    }

    /**
     * Returns this Node's level. It does so by asking its parent's level, which
     * in turn asks his parent's level, which ...
     */
    public int getLevel() {
        GraphNode parent = getParent();
        if (parent != null)
            return parent.getLevel() + 1;
        return 0;
    }

    public T getData() {
        return data;
    }


    public void remIncLink(GraphNode child) {
        Collection<GraphEdge> edges = getGraph().findEdgeSet(this, child);
        for (GraphEdge e : edges) {
            if (e instanceof GraphInclusionEdge)
                getGraph().removeEdge(e);
        }
    }

    public int getInclusionLinkCount() {
        return getIncList().size();
    }

    @Override
    public String toString() {
        return "Vertex " + getId();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Object getUserDatum(String key) {
        return userDatum.get(key);
    }

    public Object setUserDatum(String key, Object datum) {
        return userDatum.put(key, datum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphNode)) return false;

        GraphNode graphNode = (GraphNode) o;

        if (!id.equals(graphNode.id)) return false;

        return true;
    }

    @Override
    public GraphNode clone() {
        GraphNode node = new GraphNode(id);

        for (Map.Entry<String, Object> entry : userDatum.entrySet()) {
            node.setUserDatum(entry.getKey(), entry.getValue());
        }

        return node;
    }
}
