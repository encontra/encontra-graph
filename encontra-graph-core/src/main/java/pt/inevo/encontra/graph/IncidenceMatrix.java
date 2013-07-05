package pt.inevo.encontra.graph;


import java.util.ArrayList;
import java.util.logging.Logger;

public class IncidenceMatrix {
	
	Logger _log=Logger.getLogger(IncidenceMatrix.class.getName());
	int _edge_count;
	int _independent_cycle_count;

	MatrixModuloTwo _p_incidence_matrix;

	ArrayList<GraphAdjacencyEdge> _edge_pool=new ArrayList<GraphAdjacencyEdge>();
	
	/***
	* @desc contructor
	*/
	public IncidenceMatrix() 
	{
		_p_incidence_matrix = null;
		_edge_count = 0;
		_independent_cycle_count = 0;
	}


	/***	
	* @return true if cycle c is independent from previous, false otherwise
	*/
	boolean IndependentCycle(Cycle c)
	{

		boolean independent_cycle = true;
		int offset = _independent_cycle_count*(_edge_count);
		Long edge=-1l;

		// add cycle to matrix
		for (int i=0; i<c.GetEdgeCount();i++){
			edge = c.GetEdge(i);

			_p_incidence_matrix.setByOffset(new Long(offset+edge).intValue(), new Short((short) 0x1) );
		}
		_independent_cycle_count++;
		
		if (_independent_cycle_count>1) {
			// check independency

			// first perform gaussian elimination
			_p_incidence_matrix.GaussianElimination(_independent_cycle_count);

			// then see if added row is all zeros		
			independent_cycle = false;
			for (int i=0; i<_edge_count && !independent_cycle; i++) 
				independent_cycle = (_p_incidence_matrix.getByOffset(offset+i) != 0x00);
		}
		
		// if this is an independent cycle, increment the row counter
		if (!independent_cycle)
			_independent_cycle_count--;
		
		return independent_cycle;
	}

	/***
	* @desc add a cycle in order to construct the edge pool, 
	* @note this must be done before creating the matrix
	* @note IMPORTANT: creates an edge list in cycle
	*/
	void AddCycleToEdgePool(Cycle cycle)
	{
		Long current_vertex, first_vertex, previous_vertex=0l;
		boolean first = true;
		Long edge_number;

		for (Long i=0l; i<cycle.GetVertexCount();i++) {
			current_vertex = new Long(cycle.GetVertex(i.intValue()));

			// in case this is not the frst vertex, finds the edge number
			// and adds it to the edge list in cycle
			if (!first) {
				edge_number = GetEdgeNumber(cycle,previous_vertex, current_vertex);
				cycle.AddEdge(edge_number);
			} else {
				first = false;
				first_vertex = current_vertex;
			}

			previous_vertex = current_vertex;		
		}

	// the vertex list in cycle already contains the first vertex at the end of the list,
	// so we do not need the ollowing lines
//		if (i>0) {
//			edge_number = GetEdgeNumber(current_vertex, first_vertex);
//			cycle.AddEdge(edge_number);
//		}
	//
	}

	/***
	* @return the edge number of given pair of vertices
	*/
	Long GetEdgeNumber(Cycle cycle, Long vertex_a, Long vertex_b)
	{
        GraphNode node_a= cycle.getGraph().findNode(vertex_a);
        GraphNode node_b= cycle.getGraph().findNode(vertex_b);

        for (GraphAdjacencyEdge e : _edge_pool) {

            if (e.isIncident(node_a) && e.isIncident(node_b))
				return e.getId();

		}

		// if arrives here, there are no such edge in edge pool
		// so we must create a new edge

        GraphAdjacencyEdge e = new GraphAdjacencyEdge(cycle.getGraph().findNode(vertex_a), cycle.getGraph().findNode(vertex_b));
		e.setId(new Long(_edge_count++));
		// and add it to the edge pool
		_edge_pool.add(e);

		return e.getId();
	}

	/***
	* @desc creates the incidence matrix
	* @note this must be done after all cycles have been added, in order
	*      to create the edge pool
	*/
	void CreateMatrix()
	{
		_log.info("Creating incidence matrix...");
		_p_incidence_matrix = new MatrixModuloTwo(_edge_pool.size(), _edge_count);
		_log.info("...incidence matrix created!");
	}


	/***
	* @return number of first column with non-zero value in row r
	*/
	//DEL size_t IncidenceMatrix::FirstNonZeroColumn(size_t row)
	//DEL {
	//DEL 	for (size_t c=0; c<_edge_count; c++) {
	//DEL 		if (_incidence_matrix[row*_edge_count+c]==1)
	//DEL 			return c;
	//DEL 	}
	//DEL 
	//DEL 	return _edge_count;
	//DEL }



	//DEL void IncidenceMatrix::GaussianElimination()
	//DEL {
	//DEL 	size_t c, r, k, max;
	//DEL 
	//DEL 	size_t columns = _edge_count;
	//DEL 	size_t rows = _row_count;
	//DEL 	__int8 * matrix = _incidence_matrix;
	//DEL 
	//DEL 	size_t pivot_row=0;
	//DEL 
	//DEL 	for(c=0;c<columns;c++) {		
	//DEL 		max = pivot_row;
	//DEL 
	//DEL 		// in this case no substitution is needed
	//DEL 		if (matrix[pivot_row*columns+c]!=0x01) {
	//DEL 		
	//DEL 			// otherwise, lets found if its needed any substitution
	//DEL 			for (r=pivot_row+1;r<rows;r++) {
	//DEL 				if (matrix[r*columns+c]==1) {
	//DEL 					max = r;
	//DEL 					break;
	//DEL 				}
	//DEL 			}
	//DEL 
	//DEL 			// if pivot row is zero and other column is one
	//DEL 			// then they must change			
	//DEL 			if (max!=pivot_row)
	//DEL 				SwapMatrixRows(max,pivot_row);
	//DEL 		}
	//DEL 
	//DEL 		// now lets make the elimination
	//DEL 		if (matrix[pivot_row*columns+c]==0x01) {
	//DEL 			for (r=pivot_row+1; r<rows; r++)
	//DEL 				if (matrix[r*columns+c]==0x01)
	//DEL 					for (k=c;k<columns;k++)
	//DEL 						matrix[r*columns+k] ^= matrix[pivot_row*columns+k];
	//DEL 
	//DEL 			pivot_row++;
	//DEL 		}
	//DEL 	}
	//DEL }

	/***
	* @desc swaps the matrix row <row_a> with row <row_b>
	* @param size_t row_a indicates the number of one row  
	* @param size_t row_b indicates the number of other row  
	*/
	//DEL void IncidenceMatrix::SwapMatrixRows(size_t row_a, size_t row_b)
	//DEL {
	//DEL 	size_t columns = _edge_count;
	//DEL 	__int8 * matrix = _incidence_matrix;
	//DEL 	__int8 t;
	//DEL 
	//DEL 	size_t row_a_offset = row_a*columns;
	//DEL 	size_t row_b_offset = row_b*columns;
	//DEL 
	//DEL 	
	//DEL 	for (size_t k=0; k<columns; k++) {
	//DEL 		t = matrix[row_a_offset+k];
	//DEL 		matrix[row_a_offset+k] = matrix[row_b_offset+k];
	//DEL 		matrix[row_b_offset+k] = t;
	//DEL 	}
	//DEL }

}
