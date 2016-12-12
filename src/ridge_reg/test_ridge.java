package ridge_reg;


import java.util.*;
import java.util.Map.Entry;
import java.io.*;


public class test_ridge {

	static LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> matrix = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();
	static double lambda =0.2;
	static	ArrayList<Double> global_w = new ArrayList<Double>();
	
	public static LinkedHashMap<Integer,Double> normalise_vector( LinkedHashMap<Integer,Double> inputvector ){
	LinkedHashMap<Integer,Double> unit_vector = new LinkedHashMap<Integer,Double>();
	double sum=0,val;
	double unitvalue;
	for(Map.Entry<Integer,Double> m : inputvector.entrySet() ){
		val = m.getValue();
		sum+= (val*val);
	}
	double length= Math.sqrt(sum);
	System.out.println("sum and length "+ sum+" "+ length);
	for(Map.Entry<Integer,Double> m : inputvector.entrySet() ){
		unitvalue = m.getValue();
		unitvalue/= length;
		unit_vector.put(m.getKey(),unitvalue);
		
	}
	
	return unit_vector;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//LinkedHashMap<Integer,Integer> row0 = new LinkedHashMap<Integer,Integer>();
	
		LinkedHashMap<Integer,Double> row1 = new LinkedHashMap<Integer,Double>();
		row1.put(0, 1.0);
		row1.put(1, 2.0);
		row1 = normalise_vector(row1);
		for(Map.Entry<Integer,Double> m : row1.entrySet() ){
			System.out.println(m);
		}
		
		
		LinkedHashMap<Integer,Double> row2 = new LinkedHashMap<Integer,Double>();
		row2.put(1, 30.0);
		row2.put(3, 40.0);
		row2 = normalise_vector(row2);
		for(Map.Entry<Integer,Double> m : row2.entrySet() ){
			System.out.println(m);
		}
		
		LinkedHashMap<Integer,Double> row3 = new LinkedHashMap<Integer,Double>();
		row3.put(2, 50.0);
		row3.put(3, 60.0);
		row3.put(4, 70.0);
		row3 = normalise_vector(row3);
		for(Map.Entry<Integer,Double> m : row3.entrySet() ){
			System.out.println(m);
		}
		LinkedHashMap<Integer,Double> row4 = new LinkedHashMap<Integer,Double>();
		row4.put(5, 80.0);
		row4 = normalise_vector(row4);
		for(Map.Entry<Integer,Double> m : row4.entrySet() ){
			System.out.println(m);
		}
		LinkedHashMap <Integer,Double> ymatrix = new LinkedHashMap<Integer,Double>();
		ymatrix.put(0, 1.0);
		ymatrix.put(1, 0.0);
		ymatrix.put(2, 1.0);
		ymatrix.put(3, 0.0);
		
		
		matrix.put(0,row1);
		matrix.put(1,row2);
		matrix.put(2,row3);
		matrix.put(3,row4);
		
		LinkedHashMap<Integer,Double> xitranspose = new LinkedHashMap<Integer,Double>();
     	LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> X_i = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();
		LinkedHashMap<Integer,Double> W = new LinkedHashMap<Integer,Double>();
		
	//stores the term or column number and its w value	
    W.put(0,0.5);
	W.put(1,0.5);
	W.put(2,0.5);
	W.put(3,0.5);
	W.put(4,0.5);
	W.put(5,0.5);
	double wdenominator,wnumerator,finalwi;
	LinkedHashMap<Integer,Double> w_i = new LinkedHashMap<Integer,Double>();
		for(int i =0 ;i<6;i++ ){
			System.out.println("\n----");
			w_i.putAll(W);
			for(int j=0;j<4;j++)
			{
				if(matrix.get(j).get(i)!=null  )
			      {	
					//System.out.print(matrix.get(j).get(i) +" ");
					xitranspose.put(j, matrix.get(j).get(i));
				  }
										
				LinkedHashMap<Integer,Double> row = matrix.get(j);
				LinkedHashMap<Integer,Double> rowwithoutcoli = new LinkedHashMap<Integer,Double>();
			
			    for(Map.Entry<Integer,Double> m : row.entrySet()){
			    	if(m.getKey()!=i)
			    	{	
			    	//	System.out.print(m.getValue());
			    	    rowwithoutcoli.put(m.getKey(), m.getValue());
			    	}
			     }
			   // System.out.println();
			    X_i.put(j, rowwithoutcoli);
					
			}  // end of looping all rows for a column i
			
			w_i.remove(i);
			
			System.out.println("transpose of xi ");
			for(Map.Entry<Integer,Double> m1 : xitranspose.entrySet()){
				
				System.out.print( m1);
				
			}
			// X_i is X subscript of i
			
			System.out.println("\n value of Xsubscript column i for  " + i +"are ");
			for(Map.Entry<Integer,LinkedHashMap<Integer,Double>> m1 : X_i.entrySet()){
				
				System.out.println( m1.getValue());
				
			}
			
			System.out.println("value of wsubscript column i for  " + i +"are ");
           for(Map.Entry<Integer,Double> m1 : w_i.entrySet()){
				
				System.out.print(m1.getValue()+" ");
				
			}
           
           LinkedHashMap<Integer,Double>  xi_dot_wi = matrix_vector_multiplication(X_i,w_i);
           
           System.out.println();
           System.out.println("value of xi_dot_wi for i/column  " + i +"are ");
           for(Map.Entry<Integer,Double> m : xi_dot_wi.entrySet()){
        	   System.out.println(m.getValue());
           }
           
           System.out.println("value of y - xi_dot_wi for i/column  " + i +"are ");
           
           LinkedHashMap<Integer,Double> yminusxiwi = vector_minus_vector(ymatrix,xi_dot_wi);
           System.out.println(" ");
           
           for(Map.Entry<Integer,Double> m : yminusxiwi.entrySet()){
        	   System.out.println(m.getValue());
           }
           
            wnumerator = vector_vector_multiplication(xitranspose,yminusxiwi);
           
            wdenominator = vector_vector_multiplication(xitranspose,xitranspose);
           
           wdenominator += lambda;
           finalwi  =  (double)wnumerator/wdenominator ; 
           global_w.add(finalwi);
           System.out.println("w value is  "+ finalwi);
           
           
           W.put(i, finalwi);
			w_i.clear();
			X_i.clear();
			xitranspose.clear();
		
			LinkedHashMap<Integer,Double> XdotW  = matrix_vector_multiplication(matrix, W); 
			
			HashMap<Integer,Double> XdotWminusY = vector_minus_vector(XdotW,ymatrix);
			double total_error=0;
			for(Map.Entry<Integer,Double> m : XdotWminusY.entrySet()){
				
				total_error+= (m.getValue()*m.getValue());
			}
			
			System.out.println("\n Total least square error is "+ total_error);
		}   // end of calculation of wi for column i 
		
	}   // end of main
	
	
	public static double vector_vector_multiplication(LinkedHashMap<Integer,Double> X ,LinkedHashMap<Integer,Double> Y )
	{
		
		double result; 
		double xval;
		double yval;
		double sum=0;
		for ( int key : X.keySet()){
			 xval = X.get(key);
			 yval = Y.get(key);
			sum += (xval*yval);
					
		}
		result = sum;
		return result;
	}
	
	public static LinkedHashMap<Integer,Double> vector_minus_vector (LinkedHashMap<Integer,Double> X , LinkedHashMap<Integer,Double> Y){
		LinkedHashMap<Integer,Double> result = new LinkedHashMap<Integer,Double>();
		
		for( int rowkey : X.keySet()){
			
			double xval = X.get(rowkey);
			double yval = Y.get(rowkey);

			result.put(rowkey, (xval-yval));
			
		}
		
		return result;
	}
	
public static LinkedHashMap<Integer,Double>  matrix_vector_multiplication(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> X ,LinkedHashMap<Integer,Double> W)
{
	
	LinkedHashMap<Integer,Double> result =  new LinkedHashMap<Integer,Double>();	
		for(int rowno : X.keySet()){
			
			HashMap <Integer,Double> row = X.get(rowno);
			double sum =0;
			for ( Map.Entry<Integer,Double> m : row.entrySet()){
				int termid = m.getKey();
			    double	xtermvalue = m.getValue();
			//if(Y.containsKey(termid)){
				double wvalue = W.get(termid);
		//}
			
			sum+= xtermvalue*wvalue;
			
			}
			
			result.put(rowno,sum);
			
			
		}
		return result;
		
		
	}

}
