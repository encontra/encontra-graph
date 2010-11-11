package pt.inevo.encontra.graph;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.impl.DenseObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;

@SuppressWarnings("serial")
public class MatrixModuloTwo extends DenseObjectMatrix2D{

	public MatrixModuloTwo(int rows, int cols) {
		super(rows, cols);
		// Fill with zeros
		for(int c=0;c<columns();c++) {
			for(int r=0;r<rows();r++) {
				set(r,c,new Short((short) 0));
			}
		}
	}

	
	public void setByOffset(int offset,Short obj) {
		int col= offset % columns;
		int row= (int) Math.floor(offset/columns);
		set(row,col,obj);
	}
	
	public Short getByOffset (int offset) {
		int col= offset % columns;
		int row= (int) Math.floor(offset/columns);
		return get(row,col);
	}


	
	@Override
	public void set(int arg0, int arg1, Object obj) {
		try {
			Short val=(Short) obj;
			super.set(arg0, arg1, val);
		} catch (Exception e) {
			return;
		} 
		
	}


	@Override
	public Short get(int arg0, int arg1) {
		Object obj=super.get(arg0, arg1);
		try {
			Short res=(Short) obj;
			return res;
		} catch (Exception e) {
			return null;
		} 
	}


	/***
	* @desc swaps the matrix row <row_a> with row <row_b>
	* @param size_t row_a indicates the number of one row  
	* @param size_t row_b indicates the number of other row  
	*/
	void SwapMatrixRows(int row_a, int row_b)
	{
		ObjectMatrix1D a_row=viewRow(row_a).copy();
		ObjectMatrix1D b_row=viewRow(row_b).copy();

		for(int i=0;i<this.columns();i++) {
			set(row_b,i,a_row.get(i));
			set(row_a,i,b_row.get(i));
		}
	}
	
	/***
	* @desc performs gaussian elimination of first <rows> of the matrix
	*/
	void GaussianElimination(int rows)
	{
		int c, r, k, max; //, address;

		int pivot_row=0;


			for(c=0;c<columns();c++) {		
			max = pivot_row;

			// in this case no substitution is needed
			if (get(pivot_row,c)!=0x01) {
			
				// otherwise, lets found if its needed any substitution
				for (r=pivot_row+1;r<rows;r++) {
					if (get(r,c)==1) {
						max = r;
						break;
					}
				}

				// if pivot row is zero and other column is one
				// then they must change			
				if (max!=pivot_row)
					SwapMatrixRows(max,pivot_row);
			}

			// now lets make the elimination
			if (get(pivot_row,c)==0x01) {
				for (r=pivot_row+1; r<rows; r++)
					if (get(r,c)!= null && get(r,c)==0x01) {
						for (k=c;k<columns();k++) {
							set(r,k, new Short((short) (get(r,k)^get(pivot_row,k))));
						}
					}
				pivot_row++;
			}
		}

	}
}
