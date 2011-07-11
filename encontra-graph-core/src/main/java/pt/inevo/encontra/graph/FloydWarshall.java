package pt.inevo.encontra.graph;


import java.util.*;

public class FloydWarshall {
	int numVertices; // Number of vertices (when initialized).

	int[][] Dk, pred; // Matrices used in dynamic programming.

	int diameter = 0;

	boolean printFirst = true;

	Graph g;

	ArrayList <GraphNode> nodes = new ArrayList<GraphNode>();
	
	public void initialize(Graph graph) {
		List <GraphNode> cilst = new ArrayList(graph.getVertices());
		List <GraphNode> lst=new ArrayList<GraphNode>();
		for(int i=0;i<cilst.size();i++)
			lst.add(cilst.get(i));
		initialize(lst,graph);
	}
	/**
	 * Initialize algorithm. nod is the group of nodes to find the diameter of.
	 * @param nod
	 * @param graph
	 */
	public void initialize(List nod, Graph graph) {
		g = graph;
		Iterator it = nod.iterator();
		while (it.hasNext()) {
			GraphNode o = (GraphNode) it.next();
			if (o != null) {
				nodes.add(o);
			}
		}
		this.numVertices = nodes.size();

		// Initialize Dk matrices.

		Dk = new int[numVertices][numVertices];

		pred = new int[numVertices][numVertices];

	}
	
	/**
	 * Finds and returns the diameter of the group
	 * @return the diameter of the group passed to the constructor
	 */
	public int allPairsShortestPaths() {

		// Dk_minus_one = weights when k = -1
		for (int i = 0; i < numVertices; i++) {
			for (int j = 0; j < numVertices; j++) {

				
				if (((GraphNode) nodes.get(i)).isAdjentTo((GraphNode) nodes.get(j))) {
					Dk[i][j] = 1;
					pred[i][j] = i;

				} else {
					Dk[i][j] = Integer.MAX_VALUE;
					pred[i][j] = Integer.MAX_VALUE;
				}
				
				
				/*
				if (((GraphNode) nodes.get(i)).isAdjentTo((GraphNode) nodes.get(j))) {
					

				} else {
					pred[i][j] = Integer.MAX_VALUE;
				}*/
				
			}
		}

		// Now iterate over k.

		for (int k = 0; k < numVertices; k++) {

			// Compute Dk[i][j], for each i,j

			for (int i = 0; i < numVertices; i++) {
				for (int j = 0; j < numVertices; j++) {
					if (i != j) {
						int directPath = Dk[i][j];
						int kPath = Dk[i][k] + Dk[k][j];
						if (directPath == Integer.MAX_VALUE) {
							if (kPath > 0 && kPath < Integer.MAX_VALUE) {
								Dk[i][j] = kPath;
								pred[i][j] = pred[k][j];

							}

						} else if (directPath < Integer.MAX_VALUE && kPath > 0
								&& kPath < Integer.MAX_VALUE) {
							if (directPath > kPath) {
								pred[i][j] = pred[k][j];
								Dk[i][j] = kPath;

							}
						}

					}
				}
			}
		}

		for (int i = 0; i < numVertices; i++) {
			for (int j = 0; j < numVertices; j++) {

				if (Dk[i][j] > diameter && Dk[i][j] < Integer.MAX_VALUE)
					diameter = Dk[i][j];
			}
		}
		return diameter;
	}

	/**
	 * Uses recursive helper function print_path to find the 
	 * paths that make up a diameter.
	 * @param length
	 * @return a List of paths that are diameters
	 */
	public List getPaths(int length) {
		List nodes = new ArrayList();
		List ret = new ArrayList();
		List nodesSeen = new ArrayList();
		for (int i = 0; i < numVertices; i++) {
			for (int j = i+1; j < numVertices; j++) {
				if (Dk[i][j] >= length) {

					nodes.add(print_path(i, j));
				}
			}
		}

		List edges;

		for (int k = 0; k < nodes.size(); k++) {
			edges = new ArrayList();
			for (int j = 0; j < ((List) nodes.get(k)).size() - 1; j++) {
				GraphNode node1 = ((GraphNode) ((List) nodes.get(k)).get(j));
				GraphNode node2 = ((GraphNode) ((List) nodes.get(k)).get(j + 1));
				GraphEdge edge = (GraphEdge) g.findEdge(node1, node2);
				edges.add(edge);
			}
			ret.add(edges);
		}
		return ret;
	}

	/**
	 * Recursive function that takes the predecessor matrix, pred, and builds the path from i to j.
	 * @param i
	 * @param j
	 * @return
	 */
	public List <GraphNode>print_path(int i, int j) {
		List <GraphNode>ret=new ArrayList<GraphNode>();
		while(i != j){
			if(j==Integer.MAX_VALUE)
				return null;
			ret.add((GraphNode) nodes.get(j));
			j=pred[i][j];
		}
		
		ret.add((GraphNode) nodes.get(j));
		
		Collections.reverse(ret);
		
		return ret;

	}

	public List <GraphNode>GetShortestPath(int i, int j) {

		List <GraphNode> nod_ret=print_path(i,j);
		
		return nod_ret;
		/*
		if(nod_ret==null)
			return null;
		
		List<Integer> ret=new ArrayList<Integer>();
		
		for(GraphNode n:nod_ret) {
			ret.add(n.getId());
		}
		
		return ret;*/

	}
}
