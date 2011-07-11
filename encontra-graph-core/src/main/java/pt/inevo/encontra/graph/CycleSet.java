package pt.inevo.encontra.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class CycleSet extends ArrayList<Cycle>{

	private static Logger _log=Logger.getLogger(CycleSet.class.getName());
	
	private static String userDataKey="REMOVED_VERTEXES";
	
	Graph fullGraph;
	Graph graph;
	FloydWarshall floydWarshall;
	
	
	public CycleSet(Graph g){
		super();
		graph=g;
		/*
		GraphViewer viewer=new GraphViewer(graph);
		viewer.setVertexDataToShow("point");
		viewer.show();
		*/
		//viewer.waitUntilClosed();
		_log.info("Simplifying graph...");
		Simplify(graph);

        /*
        GraphViewer viewer=new GraphViewer(graph);
		viewer.setVertexDataToShow("point");
		viewer.setEdgeDataToShow(userDataKey);
		viewer.show();*/
        
		//viewer.waitUntilClosed();
		_log.info("Running FloyWarshall...");
		floydWarshall=new FloydWarshall();
		floydWarshall.initialize(graph);
		_log.info("Finding shortest paths...");
		floydWarshall.allPairsShortestPaths();
		_log.info("Running Horton...");
		Horton();
		
		UpdateToFullCycles();
	}
	
	/* TODO - Simplificação do grafo!
	 * 
	 * eliminar todos os vértices com duas ou menos arestas
	 * 
	 *  antes de eliminar um vertice que so tem duas arestas basta ver se os vertices 
	 *  opostos em cada uma das arestas nao estao ligados um ao outro
	 */ 
	private Graph Simplify(Graph og){
		Graph g=graph; // this isn't necessary - (Graph)og.copy();
		GraphNode other1;
		GraphNode other2;
		boolean connected=false; // Indicates if two nodes are interconnected
		//int vertexCount=g.numVertices();
		boolean removing=true; // flag to stop the loop
		while(removing){
			removing=false;
//			for(int i=0;i<g.getVertexCount();i++){
			for(Iterator<GraphNode> it = g.getVertices().iterator(); it.hasNext(); ){
//				GraphNode n=(GraphNode)g.getVerticesList().get(i);
				GraphNode n = it.next();
				List<GraphNode> adjList=n.getAdjList();
				int nAdj=adjList.size();
				if(nAdj==2){
					other1=adjList.get(0);
					other2=adjList.get(1);
					
					/* Approach 1
					 * antes de eliminar um vertice que so tem duas arestas basta veres se os 
					 * vertices opostos em cada uma das arestas nao estao ligados um ao outro 
					 */
					
					List<GraphNode> adjList1=other1.getAdjList();
					int nAdj1=adjList1.size();
					connected=false;
					for(int a=0;a<nAdj1;a++){
						if(adjList1.get(a)==other2) {
							connected=true;
						}
					}
					if(connected)
						continue;
					
					// Add adjency edges!
					
					GraphAdjacencyEdge edge=other1.addAdjLink(other2);
					
					//Object data=edge.getUserDatum(userDataKey);
					ArrayList<Long> removedVertexes;
					//if(data==null) {
					removedVertexes=new ArrayList<Long>();
					//} else {
					//	removedVertexes=(LinkedHashSet<Integer>) data;
					//}
					
					GraphAdjacencyEdge adjOther1=n.getAdjacencyTo(other1);
					GraphAdjacencyEdge adjOther2=n.getAdjacencyTo(other2);
					
					Object data1=adjOther1.getUserDatum(userDataKey);
					Object data2=adjOther2.getUserDatum(userDataKey);
					
					
					if(data1!=null){
						ArrayList<Long> arr_data1=(ArrayList<Long>)data1;
						// other1 is at the end - lets reverse this data
						if(arr_data1.get(arr_data1.size() - 1) ==other1.getId()){
							Collections.reverse(arr_data1);
						}
						arr_data1.remove(arr_data1.size()-1); // remove last one - should = n
						removedVertexes.addAll(arr_data1);	
						
					} else {
						removedVertexes.add(other1.getId());
					}
					
					removedVertexes.add(n.getId());
					
					if(data2!=null){
						ArrayList<Long> arr_data2=(ArrayList<Long>)data2;
				
						// other2 is at the end - lets reverse this data
						if( arr_data2.get(0).intValue()==other2.getId()){
							Collections.reverse(arr_data2);
						}
						arr_data2.remove(0); // remove first one - should = n
						removedVertexes.addAll(arr_data2);	
					} else {
						removedVertexes.add(other2.getId());
					}

                    // TODO removed the UserData.SHARED
//					edge.setUserDatum(userDataKey, removedVertexes, UserData.SHARED);
					edge.setUserDatum(userDataKey, removedVertexes);
					//other2.addAdjLink(other1);
					
					/* Approach 2
					 *  
					 *
					if(other1==other2){
						continue;
					}
					// Add adjency edges with duplicates!
					other1.addAdjLink(other2,true);
					other2.addAdjLink(other1,true);
					*/
					g.removeNode(n);//stashNode(n);
					
					removing=true;
					
					
				} else if (nAdj==1) {
					g.removeNode(n);//stashNode(n);
					removing=true;
					//other1.remAdjLink(n);
				}
			}
		}
		return g;
	}
	
	// Add missing vertexes to cycles
	private void UpdateToFullCycles(){
		for(Cycle c:this){
			Long startId=c.GetVertex(0);
			// The last vertex is the first one to close the cycle!
			for (int j=1; j<c.GetVertexCount();j++){
				Long endId=c.GetVertex(j);
				GraphNode startNode=graph.findNode(startId);
				GraphNode endNode=graph.findNode(endId);
				GraphAdjacencyEdge edge=startNode.getAdjacencyTo(endNode);
				if(edge!=null){
					Object data=edge.getUserDatum(userDataKey);
					if(data!=null) {
						ArrayList<Long> removedVertexes=(ArrayList<Long>) data;
						if(removedVertexes.get(0)!=startId) {// We've got this in reverse order
							Collections.reverse(removedVertexes);
						}
						for(int i=1;i<removedVertexes.size()-1;i++){ 
							c.addVertexAt(j++, removedVertexes.get(i));
						}
					}
				}
				startId=endId;
			}
		}
	}
	
	private void Horton() {
		//STARTING_PROCESS_MESSAGE("Horton algorithm");

		List<GraphNode> path_vx, path_vy;

		Cycle cycle;

		int v, x, y;

		// visit all vertices on the graph
		for(v=0; v<graph.getVertexCount() ;v++) { //&& !PolygonDetector::WasInterrupted()
			//YIELD_CONTROL();
			for(x=v+1; x<graph.getVertexCount() ;x++) { // && !PolygonDetector::WasInterrupted()
				path_vx = floydWarshall.GetShortestPath(v,x);
				//YIELD_CONTROL();
				
				for (y=x+1; y<graph.getVertexCount(); y++){ // && !PolygonDetector::WasInterrupted()
					path_vy = floydWarshall.GetShortestPath(v,y);
					//YIELD_CONTROL();			

					// if paths exists and points x and y are adjacent
                    List<GraphNode> verticesList = new ArrayList(graph.getVertices());
					if (path_vx!=null && path_vy!=null && (verticesList.get(x)).isAdjentTo(verticesList.get(y))) {
						Long v_id = verticesList.get(v).getId();
						boolean only=IsOnlyCommonPointInPaths(v_id, path_vx, path_vy);
						boolean tierman=IsTiermanCompliant(verticesList.get(v), path_vx, path_vy);
						if (IsOnlyCommonPointInPaths(v_id, path_vx, path_vy) &&
							IsTiermanCompliant(verticesList.get(v), path_vx, path_vy)){
							ArrayList<Long> ids_path_vx=new ArrayList<Long>();
							ArrayList<Long> ids_path_vy=new ArrayList<Long>();
							for(GraphNode g:path_vx)
								ids_path_vx.add(g.getId());
							for(GraphNode g:path_vy)
								ids_path_vy.add(g.getId());
							cycle = new Cycle(graph,ids_path_vx, ids_path_vy);
							if (cycle.GetLength()>0)  
								add(cycle);
							else
								cycle=null;
						}
					}
					//path_vy.clear();
					path_vy=null;
					//if (EXCEEDING_PROCESSING_TIME(wxDateTime::UNow(), start)) {
					//	PolygonDetector::Interrupt(); break;
					//}
				}
				//path_vx.clear();
				path_vx=null;
				//if (EXCEEDING_PROCESSING_TIME(wxDateTime::UNow(), start)) {
				//	PolygonDetector::Interrupt(); break;
				//}
			}
			//if (EXCEEDING_PROCESSING_TIME(wxDateTime::UNow(), start)) {
			//		PolygonDetector::Interrupt(); break;
			//}
		}	

		// sort the cycles
		Collections.sort(this);
		
		// lets apply the gaussian elimination
		SelectCycles();
		
		
		//ENDING_PROCESS_MESSAGE();

		//return p_cycle_set;
		
	}
	
	// V is the node index
	private boolean IsTiermanCompliant(GraphNode v, List<GraphNode> path_vx, List<GraphNode> path_vy) {
		int i;
		GraphNode item_vx = path_vx.get(0);
		GraphNode item_vy = path_vy.get(0);

		int v_index=floydWarshall.nodes.indexOf(v);
		
		// checks if both path start at 'v'
		if (item_vx.getId() != v.getId() || item_vy.getId() != v.getId())
			return false;

		// checks if a cycle only contains vertices that precede v 
		for (i=1; i< path_vx.size(); i++) {
			int index_vx = floydWarshall.nodes.indexOf(path_vx.get(i));
			if (index_vx<=v_index)
				return false;
		}
		for (i=1; i< path_vy.size(); i++) {
			int index_vy = floydWarshall.nodes.indexOf(path_vy.get(i));
			if (index_vy<=v_index)
				return false;
		}

		return true;
	}

	private boolean IsOnlyCommonPointInPaths(Long v, List<GraphNode> p1, List<GraphNode> p2) {
		int i, j;
		Long item_p1;
		Long item_p2;
		boolean v_exists_in_p1 = false;
		boolean v_exists_in_p2 = false;

		for (i=0; i<p1.size(); i++) {
			item_p1 = p1.get(i).getId();

			// checks if v exists in p1
			v_exists_in_p1 |= (item_p1 == v);
			
			for (j=0; j<p2.size();j++) {
				item_p2 = p2.get(j).getId();
			
				if (item_p1 == item_p2 && item_p1 != v)
					return false;

				// checks if v exists in p2
				v_exists_in_p2 |= (item_p2 == v);
			}
		}
				
		return v_exists_in_p2 && v_exists_in_p1;
	}

	/***
	* @desc Select independent cycles from cycle set
	*/
	void SelectCycles()
	{
		int c;
		boolean independent_cycle;

		// creation of incidence matrix 
		IncidenceMatrix incidence_matrix = new IncidenceMatrix();

		for (c=0; c<size();c++) 
			incidence_matrix.AddCycleToEdgePool(get(c));

		incidence_matrix.CreateMatrix();

		_log.info("Selecting independent cycles...");
		for (c=0; c<size();)  {		
			independent_cycle = incidence_matrix.IndependentCycle(get(c));
			
			if (!independent_cycle) {
				remove(c);
			}
			else 
				c++;
		}
		_log.info("...independent cycles selected!");
		return;

	}
}
