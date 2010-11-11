package pt.inevo.encontra.graph;

import java.util.ArrayList;
import java.util.List;

public class Cycle implements Comparable<Cycle>{
	int _generated_from;
	ArrayList<Integer> _p_cycle=new ArrayList<Integer>();	
	ArrayList<Integer> _p_edge_list=new ArrayList<Integer>();
	Graph _graph;
	
	void AddEdge(Integer edge_id) { if (_p_edge_list!=null) _p_edge_list.add(edge_id); }

	public Integer GetVertex(int number) {
		return (_p_cycle!=null && _p_cycle.size()>number)?_p_cycle.get(number):0;
	}
	
	public Integer GetEdge(int number) { 
		return (_p_edge_list!=null && _p_edge_list.size()>number)?_p_edge_list.get(number):0;
	}

	public void addVertexAt(int pos, int vertexId){ _p_cycle.add(pos, vertexId); }
	public int GetVertexCount() { return (_p_cycle!=null)?_p_cycle.size():0;}
	public int GetEdgeCount() { return (_p_edge_list!=null)?_p_edge_list.size():0;}

	public int GetLength() { return GetVertexCount(); };
	
	
	/***
	* @desc contructor
	*/
	public Cycle(Graph graph,List<Integer> path_vx, List<Integer> path_vy)
	{		
		_graph=graph;
		_p_cycle=null;
		_p_edge_list=null;
		_generated_from=0;
		int i;
		int item_vx = path_vx.get(0);
		int item_vy = path_vy.get(0);


		// checks if both path start at 'v'
		if (item_vx == item_vy ) {	

			_generated_from = item_vx;

			// creates a new array
			_p_cycle = new ArrayList<Integer>(/*path_vx->GetCount()+path_vy->GetCount()*/);

			for (i=0; i< path_vx.size(); i++) {
				item_vx = path_vx.get(i);
				_p_cycle.add(item_vx);
			}


			for (i=1; i<= path_vy.size(); i++) {
				item_vy = path_vy.get(path_vy.size()-i);
				_p_cycle.add(item_vy);
			}

			_p_edge_list = new ArrayList<Integer>();
		} 
	}



	public int compareTo(Cycle o) {
		if (GetLength()==o.GetLength()) {		
			return _generated_from - o._generated_from;
		}

		return GetLength() - o.GetLength();
	}
}
