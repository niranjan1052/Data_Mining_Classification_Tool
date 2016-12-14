package ridge_reg;
import javax.swing.text.Position; 

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class regression {

	static Map<Integer, HashMap<Integer, Integer>> docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Integer>>();
	//stores the document term frequency represented vectors
	static Map<Integer, HashMap<Integer, Double>> unit_docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
	//stores the document term frequency represented vectors normalised to unit length
		static double lambda = 0.2;
	static LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> train_unit_docid_TF_Vecotr = new LinkedHashMap<Integer, LinkedHashMap<Integer,Double>>();
	//stores the training set document term frequency represented vectors normalised to unit length
	
	static Map<Integer, HashMap<Integer, Double>> test_unit_docid_TF_Vecotr = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
	//stores the test set document term frequency represented vectors normalised to unit length
	
	
	static Map<Integer, HashMap<Integer,Double>> docID_TF_IDF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
   //stores the TF*IDF representation of the document vectors
	
	static Map<Integer,Double> docID_length = new LinkedHashMap<Integer,Double>();
	//stores the document ID and the length of its vector to be used in creation of unit document vectors
	static Map<Integer,Double > term_IDf_map =  	new HashMap<Integer,Double>();
	// stores the IDf value of each term to be used in creation of IDF repesentation vector
	
	static HashMap <Integer,Integer> docID_outpput_docID = new LinkedHashMap<Integer,Integer>();
	
	static HashMap<Integer,String> docID_classlabel = new LinkedHashMap<Integer,String>();
	//data from rlabel file each docid and its label
	static HashMap<String,HashMap<Integer,Double> >  class_docid_wscores = new LinkedHashMap<String, HashMap<Integer,Double> > ();   
	// stores the classlabel and a hashmap of wscores for each of the document ids
	
	static int no_of_attributes = 71944;
	static HashMap<Integer,String> predicted_docID_classlabels = new LinkedHashMap<Integer,String>();
	
	static HashMap<String,ArrayList<Integer>> label_docids = new LinkedHashMap<String, ArrayList<Integer>>();
	
	static ArrayList<Integer> trainset = new ArrayList<Integer>();
	static ArrayList<Integer> testset = new ArrayList<Integer>();
	
	static HashMap<String,Integer> classlabelmap = new LinkedHashMap<String,Integer>();
	
	static int feature_representation_option = 1 ;                   // 1 for Term frequency
	static HashMap<String , Double>  class_maxf1value = new LinkedHashMap<String,Double> ();
	
	static String outputfile="out.txt";
	static String trainfile = "./20newsgroups.train";
	static String testfile = "./20newsgroups.test";
	static String feature_labelfile;
	static String input_rlabelfile= "./20newsgroups.rlabel";
	static String wordlabelfile = "./20newsgroups_word.clabel";
	
	static ArrayList<Integer> feature_labels = new ArrayList<Integer>();
	static String inputfile= "./20newsgroups_word.ijv";
	
	static each_class[] global_clusters = new each_class[20];
	
	public static void readinputfile () {
		// read file to populate training set
				try {
					BufferedReader br = new BufferedReader(new FileReader(trainfile));
					String line = null;
					int docid ;
					while((line=br.readLine())!=null){
						docid = Integer.parseInt(line);
						trainset.add(docid);
					}
					
				}catch (Exception e){
					System.out.println("error reading trainfile");
				}
				
		
		
		 System.out.println("\n Readling input file \n");
		try{
			BufferedReader br = new BufferedReader( new FileReader(inputfile));
			String line = null;
			int docid,termid,termfreq;
	        int vectorlength =0;
			while((line=br.readLine())!=null){
				docid = Integer.parseInt(line.split(" ")[0]);
				termid = Integer.parseInt(line.split(" ")[1]);
				if( feature_representation_option ==1) {
					termfreq = Integer.parseInt(line.split(" ")[2]);
				}
				else
				{
					termfreq= 1;
				}
				
				if(!docID_TF_Vector.containsKey(docid)){
					HashMap<Integer,Integer> term_freq_vector = new HashMap<Integer,Integer>();
					term_freq_vector.put(termid, termfreq);
					docID_TF_Vector.put(docid, term_freq_vector);
				    
			    	vectorlength=0;
				 }
				else{
			     	HashMap<Integer,Integer> word_vector = docID_TF_Vector.get(docid);
			    	word_vector.put(termid,termfreq);
			    	docID_TF_Vector.put(docid,word_vector);
			  
				}
				 vectorlength+=(termfreq*termfreq);
				 docID_length.put(docid, (double)Math.sqrt(vectorlength));
			
			}
			
			for(int documentid : docID_TF_Vector.keySet() ){                // Normalise the document vectors
				HashMap<Integer,Integer> wordvector = docID_TF_Vector.get(documentid);
				LinkedHashMap<Integer,Double> unitwordvector = new LinkedHashMap<Integer,Double>();
				Double docvectorlength = docID_length.get(documentid);
				for ( int wordid : wordvector.keySet()){
					unitwordvector.put(wordid, wordvector.get(wordid)/docvectorlength);
				}
				if(trainset.contains(documentid)){
					train_unit_docid_TF_Vecotr.put(documentid,unitwordvector);
				}
				else if( testset.contains(documentid))
				{
				   test_unit_docid_TF_Vecotr.put(documentid,unitwordvector);
				}
				unit_docID_TF_Vector.put(documentid,unitwordvector);
			
			}
			
		  	docID_TF_Vector.clear();	
			unit_docID_TF_Vector.clear();
		}
		catch(Exception e){
			System.out.println("file read error "+e.getMessage());
		}
		
		
		 System.out.println("\n Readling rlabel file \n");
		try {
			BufferedReader br = new BufferedReader(new FileReader(input_rlabelfile));
			String line = null;
			int docid ;
			String classlabel;
			int outputdocid ;
			while((line=br.readLine())!=null){
				docid = Integer.parseInt(line.split(" ")[0]);
				classlabel = line.split(" ")[1];
				outputdocid = Integer.parseInt(line.split(" ")[2]);
				docID_classlabel.put(docid,classlabel);
				docID_outpput_docID.put(docid,outputdocid);
				if(!label_docids.containsKey(classlabel)){             // map classlabel to the list of corresponding doc ids
					ArrayList<Integer> docidlist = new ArrayList<Integer>();
					docidlist.add(docid);
					label_docids.put(classlabel,docidlist);
					
				}else{
					ArrayList<Integer> docids = label_docids.get(classlabel);
					docids.add(docid);
					label_docids.put(classlabel, docids);
				}
				
				if(!classlabelmap.containsKey(classlabel)){       // map of class labels to numerical numbers
					classlabelmap.put(classlabel, (classlabelmap.size()+1));
				}
			}
			
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		
		
		
		// read file to populate test set
				try {
					BufferedReader br = new BufferedReader(new FileReader(testfile));
					String line = null;
					int docid ;
					while((line=br.readLine())!=null){
						docid = Integer.parseInt(line);
						testset.add(docid);
					}
					
				}catch (Exception e){
					System.out.println(e.getMessage());
				}
		
				//populate all the feauture labels
		  try{
			   BufferedReader br = new BufferedReader (new FileReader(wordlabelfile));
			   String line = null;
				int termid=1 ;
				while((line=br.readLine())!=null){
					  termid++;
					  feature_labels.add(termid);
				}	
					
		  }catch (Exception e){
				System.out.println(e.getMessage());
		  }		
				
				
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

       long startTime = System.nanoTime();
			
	//	inputfile = "./20newsgroups_word.ijv";
	//	outputfile = "classification_colution";
		readinputfile();
		
		
		System.out.println("Done reading input files .. Calucating w .. ");
		regression ridgeobj = new regression();
		
		System.out.println(" Calucalting W  ");
		
		ridgeobj.calc_w_regression();
		
		System.out.println("Training phase done.. cetnroids found for 20 binary classifiers ");
		System.out.println("testset len "+ testset.size());
		
		HashMap<Integer,Double> docid_wscore = new HashMap<Integer,Double>(); 
		
		
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime)/1000000000) + " seconds"); 
		
	

	
	}   // End of main 
	
	public  void calc_w_regression()
	{
	   System.out.println("insisde calc_W function");
	   int classno=0;
	   LinkedHashMap<Integer,Double> ymatrix = new LinkedHashMap<Integer,Double>();
		
	   for( String classname : label_docids.keySet())
	   {
			if(classno>=1) break;
			//populate y matrix for current +ve class
			
			for(int recid : trainset){
				if(docID_classlabel.get(recid).equals(classname))
					ymatrix.put(recid,1.0);
				else
					ymatrix.put(recid,0.0);
				
			}
			
			each_class obj = new each_class();
			obj.classlabel = classname; 
			
			int alt_pos_vlen1=0;
			obj.documentIDs = label_docids.get(classname);
			LinkedHashMap<Integer,Double> xitranspose = new LinkedHashMap<Integer,Double>();
	     	LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> X_i = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();
	     	
			LinkedHashMap<Integer,Double> W = new LinkedHashMap<Integer,Double>();
			//stores the term or column number and its w value	
			int t=1;
			for(t=1;t<=feature_labels.size();t++){
				
				W.put(t,0.001);
				
			}
			
			System.out.println("done populating initial w");
			double wdenominator,wnumerator,finalwi;
			LinkedHashMap<Integer,Double> w_i = new LinkedHashMap<Integer,Double>();
			int featurecount =0;
			double total_error=0,prev_error=0;
			double currentwvalue ;
			X_i.putAll(train_unit_docid_TF_Vecotr);
			w_i.putAll(W);
			System.out.println("feature_labels.size() "+w_i.size());
			int iterationcount =0; 
			LinkedHashMap<Integer,Double>  xi_dot_wi = matrix_vector_multiplication(X_i,w_i);    
			
			LinkedHashMap<Integer,Double> Xitransdotcurrentwi = new LinkedHashMap<Integer,Double>();
			LinkedHashMap<Integer,Double> prevxidotwi = new LinkedHashMap<Integer,Double>();
			for(Map.Entry<Integer, Double> m : xi_dot_wi.entrySet()){
				prevxidotwi.put(m.getKey(), 0.0);
			}
			
			for( iterationcount =0;iterationcount<10;iterationcount++)
			{
				  featurecount=0;
			     for(int i=1;i<=feature_labels.size();i++ )
			      {     
			    	 
				       for(int j : train_unit_docid_TF_Vecotr.keySet())   // j is the document id, so for each roc id i.e each row in matrix
					       {  
							HashMap<Integer,Double> row = train_unit_docid_TF_Vecotr.get(j);
							if(row.get(i)!=null  )
						      {	
								xitranspose.put(j, row.get(i));
							  }else{
								xitranspose.put(j, 0.0);
							  }
				 		
					       }      // end of looping all rows for a column i
						
						if(i==71944){
							System.out.println("  hh");
						}
						 currentwvalue = w_i.get(i);
						 
						 for(Map.Entry<Integer,Double> m : xitranspose.entrySet()){
							 Xitransdotcurrentwi.put(m.getKey(), (m.getValue()*currentwvalue));
						 }
						double newval;
						 for(Map.Entry<Integer,Double> m : xi_dot_wi.entrySet()){
							 newval = m.getValue()- Xitransdotcurrentwi.get(m.getKey()) + prevxidotwi.get(m.getKey());
							 xi_dot_wi.put(m.getKey(), (newval));
						 }
					
				          
			             LinkedHashMap<Integer,Double> yminusxiwi = vector_minus_vector(ymatrix,xi_dot_wi);
			         	           
			             wnumerator = vector_vector_multiplication(xitranspose,yminusxiwi);
			             wdenominator = vector_vector_multiplication(xitranspose,xitranspose);        
			             wdenominator += lambda;
			             finalwi  =  (double)wnumerator/wdenominator ; 
			           
			             W.put(i, finalwi);
			          
			             w_i.put(i, finalwi);
			             
			             for(Map.Entry<Integer,Double> m : prevxidotwi.entrySet()){
			            	 newval = xitranspose.get(m.getKey())*finalwi ;
			            	 prevxidotwi.put(m.getKey(),newval);
			             }
			             
					     X_i.clear();
					     xitranspose.clear();
					     //calculation of least square error 
					    
					     featurecount++;
					    if(featurecount%1000==0){
					    	System.out.println(" columns done "+ featurecount);
					    }
				   
			        }   // end of loop calculation of new W for kth iteration
		    
		   
		         
				  LinkedHashMap<Integer,Double> XdotW  = matrix_vector_multiplication(train_unit_docid_TF_Vecotr, W); 
			     
				
			      HashMap<Integer,Double> XdotWminusY = vector_minus_vector(XdotW,ymatrix);
				  total_error=0;
				  for(Map.Entry<Integer,Double> m : XdotWminusY.entrySet()){
					
					  total_error+= (m.getValue()*m.getValue());
				    }
				  double wl2norm=0;
				  for(Map.Entry<Integer, Double> m : W.entrySet()){
					   
					   wl2norm += ( m.getValue()*m.getValue());
				    }
				   wl2norm *=lambda;
				   total_error += total_error+wl2norm;
				   System.out.println("\n Total least square error for iteration of i = "+ iterationcount+ " is "+ total_error);
				   System.out.println("change in error is "+ (total_error-prev_error));
				   if(Math.abs(prev_error-total_error)<0.001 && Math.abs(prev_error-total_error)>0){
					  System.out.println("Coverged ");
				    	  break;
				    }
				    
				    
				   prev_error= total_error;
			   
		     
	       }   // end of loop for k iterations to converge
 		     
		   global_clusters[classno++]= obj;
			
		}  // end of for each classlabel classifier
	}


	
	
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
	
	class each_class{
		ArrayList<Integer> documentIDs ;
		HashMap<Integer,Double> wscores = new HashMap<Integer,Double>() ;
	    String classlabel;
	   
	}

}


