package pt.inevo.encontra.graph;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import pt.inevo.encontra.common.distance.HasDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GraphNode<T> extends SparseVertex {
	private T _data;
	

	/** Default constructor.
	 * Constructs a new Node with empty inclusion- and adjacency lists.
	 * The Node's id is set to -1.
	 */
	public GraphNode() {
		_data=null;
		
		this.id=-1;
		//, _parent(NULL), _id(-1);
		//_adjList = new CIList<Node *>();
		//_incList = new CIList<Node *>();
	}

	/** Constructor.
	 * Constructs a new Node with empty inclusion- and adjacency lists.
	 *
	 * @param id	The id of the Node.
	 */
	public GraphNode(int id) {
		this.id=id;
		_data=null;//(NULL), _parent(NULL) {
	    //_id = id;
	    //_adjList = new CIList<Node *> ();
	    //_incList = new CIList<Node *> ();
	}
	
	public int getId(){
		return id;
	}
	
	public void setData(T d) {
		_data=d;
	}
	
	public GraphAdjacencyEdge getAdjacencyTo(GraphNode no) {
		Set<Edge> edges=this.findEdgeSet(no);
		for(Edge e:edges) {
			if(e instanceof GraphAdjacencyEdge) {
				return (GraphAdjacencyEdge)e;
			}
		}
		return null;
	}
	
	public boolean isAdjentTo(GraphNode no) {
		if(no==this)
			return true;
		Set<Edge> edges=this.findEdgeSet(no);
		for(Edge e:edges) {
			if(e instanceof GraphAdjacencyEdge) {
				return true;
			}
		}
		return false;
	}
	
	public void remAdjLink(GraphNode no) {
		if (no != this && this.isAdjentTo(no)) {
			Set<Edge> edges=this.findEdgeSet(no);
			for(Edge e:edges) {
				if(e instanceof GraphAdjacencyEdge) {
					((Graph)getGraph()).removeEdge(e);
				}
			}
		}
	}
	
	public GraphAdjacencyEdge addAdjLink(GraphNode no) {
		return addAdjLink(no,false);
	}
	
	public GraphAdjacencyEdge addAdjLink(GraphNode no, boolean allowDuplicates) {
	
		if (no != this) {
			int adjCount=0;
			// Allow at most two adjency edges
			if(allowDuplicates){
				Set<Edge> edges=this.findEdgeSet(no);
				
				for(Edge e:edges) {
					if(e instanceof GraphAdjacencyEdge) {
						adjCount++;
					}
				}
			} 
			
			if (!this.isAdjentTo(no) || (allowDuplicates && adjCount<=1)) {
				GraphAdjacencyEdge edge=new GraphAdjacencyEdge(this,no);
				((Graph)getGraph()).addEdge(edge);
				return edge;
			}
		}
		
		return null;
	}
	
	/**
	 * Adds an inclusion link from this Node to the Node given as argument.
	 *
	 * @param no	The node to become this Node's child.
	 *
	 * @return	True iff the insertion was succesful, false otherwise.
	 */
	public boolean addIncLink(GraphNode no) {
		if (no != this && !this.isAdjentTo(no)) {
			((Graph)getGraph()).addEdge(new GraphInclusionEdge(this,no)); //_incList->push(no);
		    return true;
		}
		else {
			return false;
		}
	}
	
	public List<GraphNode> getAdjList() {
		List<GraphNode> list=new ArrayList<GraphNode>();
		Set<Edge> edges=this.getOutEdges();
		for(Edge e:edges) {
			if(e instanceof GraphAdjacencyEdge)
				list.add((GraphNode)e.getOpposite(this));
		}
		return list;
	}
	
	public List<GraphNode> getIncList() {
		List<GraphNode> list=new ArrayList<GraphNode>();
		Set<Edge> edges=this.getOutEdges();
		for(Edge e:edges) {
			if(e instanceof GraphInclusionEdge)
				list.add((GraphNode)((GraphInclusionEdge)e).getDest());
		}
		return list;
	}
	
	public GraphNode getParent() {
		Set<Edge> inEdges=this.getInEdges();
		for(Edge e:inEdges){
			if(e instanceof GraphInclusionEdge)
				return (GraphNode)((GraphInclusionEdge)e).getSource();
		}
		return null;
	}
	/**
	 * Returns this Node's level. It does so by asking its parent's level, which
	 * in turn asks his parent's level, which ...
	 */
	public int getLevel() {
		GraphNode parent=getParent();
		if (parent!=null) 
			return parent.getLevel()+1; 
		return 0;
	}

	public T getData() {
		return _data;
	}


	public void remIncLink(GraphNode child) {
		Set <Edge> edges=findEdgeSet(child);
		for(Edge e:edges) {
			if(e instanceof GraphInclusionEdge)
				((Graph)getGraph()).removeEdge(e);
		}
	}

	public int  getInclusionLinkCount() {
		return getIncList().size();
	}
	
	@Override
	public String toString() {
		return "Vertex "+getId();
	}
	
}
