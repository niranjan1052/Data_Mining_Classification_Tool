package classification;
import javax.swing.text.Position; 

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class centroid {

	static Map<Integer, HashMap<Integer, Double>> docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
	//stores the document term frequency represented vectors
	static Map<Integer, HashMap<Integer, Double>> unit_docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
	//stores the document term frequency represented vectors normalised to unit length
		
	static Map<Integer, HashMap<Integer,Double>> docID_TF_IDF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
   //stores the TF*IDF representation of the document vectors
	
	static Map<Integer,Double> docID_length = new LinkedHashMap<Integer,Double>();
	//stores the document ID and the length of its vector to be used in creation of unit document vectors
	static Map<Integer,Double > term_IDf_map =  	new HashMap<Integer,Double>();
	// stores the IDf value of each term to be used in creation of IDF repesentation vector
	
	static HashMap <Integer,Integer> docID_outpput_docID = new LinkedHashMap<Integer,Integer>();
	static HashMap<Integer,String> docID_classlabel = new LinkedHashMap<Integer,String>();
	static HashMap<String,HashMap<Integer,Double> >  class_docid_wscores = new LinkedHashMap<String, HashMap<Integer,Double> > ();   
	// stores the classlabel and a hashmap of wscores for each of the document ids
	
	static HashMap<Integer,String> predicted_docID_classlabels = new LinkedHashMap<Integer,String>();
	
	static HashMap<String,ArrayList<Integer>> label_docids = new LinkedHashMap<String, ArrayList<Integer>>();
	static int no_of_attributes = 291780;    //71944 / 291780
	
	static ArrayList<Integer> trainset = new ArrayList<Integer>();
	static ArrayList<Integer> testset = new ArrayList<Integer>();
	
	static HashMap<String,Integer> classlabelmap = new LinkedHashMap<String,Integer>();
	static double[] termindex_IDF = new double [no_of_attributes];
	static int feature_representation_option = 2 ;
	static HashMap<String , Double>  class_maxf1value = new LinkedHashMap<String,Double> ();
	
	static String outputfile="out.txt";
	static String trainfile = "./20newsgroups.train";
	static String testfile = "./20newsgroups.test";
	static String feature_labelfile;
	static String input_rlabelfile= "./20newsgroups.rlabel";
	
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
		
		
		 System.out.println("Readling input file ");
		try{
			BufferedReader br = new BufferedReader( new FileReader(inputfile));
			String line = null;
			int docid,termid;
			double termfreq;
	        int vectorlength =0;
	        int doccount=0;
	        int[] term_doccount = new int [no_of_attributes];
			while((line=br.readLine())!=null){
				docid = Integer.parseInt(line.split(" ")[0]);
				termid = Integer.parseInt(line.split(" ")[1]);
				if(trainset.contains(docid))
				{ 
			    	doccount = term_doccount[termid];
				    doccount+=1;
				    term_doccount[termid]=doccount;
				}
				   
				if( feature_representation_option ==1  || feature_representation_option ==2  ) {
					termfreq = Integer.parseInt(line.split(" ")[2]);
				}
				else
				{
					termfreq= 1;
				}
				
				if(!docID_TF_Vector.containsKey(docid)){
					HashMap<Integer,Double> term_freq_vector = new HashMap<Integer,Double>();
					term_freq_vector.put(termid, termfreq);
				    docID_TF_Vector.put(docid, term_freq_vector);
				    
			    	vectorlength=0;
				 }
				else{
			     	HashMap<Integer,Double> word_vector = docID_TF_Vector.get(docid);
			    	word_vector.put(termid,termfreq);
			    	docID_TF_Vector.put(docid,word_vector);
			  
				}
				 vectorlength+=(termfreq*termfreq);
				 docID_length.put(docid, (double)Math.sqrt(vectorlength));
			
			}
			
			
			if(feature_representation_option==2){
				
				Arrays.fill(termindex_IDF, 1);
				int totalnoofdocs = trainset.size();
				int  D_t;
				double IDF_t;
				for(int i=0;i<no_of_attributes;i++){
					D_t = term_doccount[i];
					if( D_t != 0)
					{ 
						IDF_t =(  Math.log10((double)(totalnoofdocs/D_t))/ Math.log10(2));
					    termindex_IDF[i]=IDF_t;
					}
				}
				
				
				for(int documentid : docID_TF_Vector.keySet() ){                // multiply Tf by IDf
					HashMap<Integer,Double> wordvector = docID_TF_Vector.get(documentid);
					HashMap<Integer,Double> wordTFIDvector = new HashMap<Integer,Double>();
				//	Double docvectorlength = docID_length.get(documentid);
					for ( int wordid : wordvector.keySet()){
						IDF_t = termindex_IDF[wordid];
						
				    	wordTFIDvector.put(wordid, wordvector.get(wordid)*IDF_t);
						
					}
					docID_TF_Vector.put(documentid,wordTFIDvector);
			
				}
				
				double vlength=0;
				double sum=0,val;         // find new length
				HashMap<Integer,Double > temp = new LinkedHashMap<Integer,Double>();
				for(Map.Entry<Integer,HashMap<Integer,Double>> m : docID_TF_Vector.entrySet()){
					
					temp = m.getValue();
					sum=0;
					vlength=0;
					for(int wordid : temp.keySet()){
						val= temp.get(wordid);
						sum+= (val*val);
						
					}
					vlength = Math.sqrt(sum);
					docID_length.put(m.getKey(), vlength);
				}
				
			}
			
			
			
			
			for(int documentid : docID_TF_Vector.keySet() ){                // Normalise the document vectors
				HashMap<Integer,Double> wordvector = docID_TF_Vector.get(documentid);
				HashMap<Integer,Double> unitwordvector = new HashMap<Integer,Double>();
				Double docvectorlength = docID_length.get(documentid);
				for ( int wordid : wordvector.keySet()){
					unitwordvector.put(wordid, wordvector.get(wordid)/docvectorlength);
				}
				unit_docID_TF_Vector.put(documentid,unitwordvector);
		
			}
			
			
		}
		catch(Exception e){
			System.out.println("input file read error "); e.printStackTrace();
		}
		
		
		 System.out.println("\nReadling rlabel file \n");
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
		
		
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	//	inputfile = "./20newsgroups_word.ijv";
	//	outputfile = "classification_colution";
		long startTime = System.nanoTime();
		
		readinputfile();
		
		
		System.out.println("Done reading input files .. Calucating centroids .. ");
		centroid centroidobj = new centroid();
		
		centroidobj.calc_centroid();
		
		System.out.println("Training phase done.. cetnroids found for 20 binary classifiers ");
		
		System.out.println("Performing classification on the test set.. ");
		
		HashMap<Integer,Double> docid_wscore = new HashMap<Integer,Double>(); 
		
		for ( int docid : testset){
			double positive_similarity=0;
			double negative_similarity=0;
		
			HashMap<Integer,Double> termvector =  unit_docID_TF_Vector.get(docid);
			double similarity_diff, max_similarity_diff=-2;
			HashMap< String, Double> class_wscores = new LinkedHashMap<String,Double>();   // stores the document id and wscores for a particular class 
			for(each_class obj : global_clusters ){
				
				positive_similarity = find_similarity(termvector, obj.unit_positivecentroid) ;
				negative_similarity = find_similarity(termvector, obj.unit_negativecentroid) ;
				similarity_diff = positive_similarity - negative_similarity;
				 class_wscores.put(obj.classlabel,similarity_diff);
					
				if(similarity_diff>max_similarity_diff){
					max_similarity_diff = similarity_diff;
					predicted_docID_classlabels.put(docid,obj.classlabel);
				}
								
			}
		 //   System.out.println("assigned class to docid "+ docid);
		
			for(Map.Entry<String,Double> m:  class_wscores.entrySet()){
				
				
				if( class_docid_wscores.containsKey(m.getKey())){    // check if the class entry is present in the global mapping 
					
					docid_wscore = class_docid_wscores.get(m.getKey());          //if present extract the hashmap of wscores for this class 
					
					docid_wscore.put(docid, m.getValue());     // updates the global class_docid_wscores for the document id of testfile for each class 
					class_docid_wscores.put(m.getKey(), docid_wscore);       //insert back the updated wscores data for current document id. 
				
				}
				else {
					
					HashMap<Integer,Double > document_wscore = new HashMap<Integer,Double> ();             // else create a new entry for the class with a new document_wscore hashmap
					document_wscore.put(docid, m.getValue());
					class_docid_wscores.put(m.getKey(),document_wscore );
					
				}
						
			}
			
			
		} //   End of loop to run for each test instance
	
		
		
		System.out.println("Completed classifying Test data .. writing output file ");
		
	//	for( Map.Entry m : predicted_docID_classlabels.entrySet()){
	//		 System.out.println(m.getKey() + " - "+m.getValue());
	//	}
		
		centroidobj.write_classification_outputfile(outputfile);
				
		calc_accuracy();
		System.out.println("\nPrinting the max F1 values for each class \n");
		
		calc_f1value();
		
		
	for ( Map.Entry<String,Double> m : class_maxf1value.entrySet()){
		
		System.out.println(m.getKey() +" - "+m.getValue());
	}
		
	long endTime = System.nanoTime();
	System.out.println("Took "+((endTime - startTime)/1000000000) + " seconds"); 	
		
		
	}   // End of main 
	
	
	
	
	public static void calc_f1value(){
		
		
		for ( String classname: class_docid_wscores.keySet()){
			
			HashMap<Integer,Double> docid_wscore = class_docid_wscores.get(classname);
			
			HashMap<Integer,Double> sorted_docid_wscore =  sortByValue(docid_wscore);
		ArrayList<Double> f1values = new ArrayList<Double>();
			 int flagcount=0;
			ArrayList<Integer> positiveids  = new ArrayList<Integer>();
			for ( int docid : sorted_docid_wscore.keySet()){            
				double score= sorted_docid_wscore.get(docid);
				int TP=0, FP=0,FN=0;
				positiveids.add(docid);
				
				for(int i =0;i<positiveids.size();i++){
					if( classname.equals(docID_classlabel.get(positiveids.get(i)))  ){
					    // if the current positive is +ve in ground truth
						TP++;
						
					}
					else {
						FP++;
					}
									
				}
				
				FN= label_docids.get(classname).size() - TP;
				
			  double prec_positive = (double)TP/(TP+FP);
			  double rec_positive = (double) TP/(TP+FN);
			  
			  double F1_positive =0.0;
			  if(prec_positive!=0.0 && rec_positive!=0.0)
			  {
				  F1_positive= 2 * (prec_positive * rec_positive) /   (prec_positive + rec_positive);
			  }
			  
			  f1values.add(F1_positive);
			  
			  if(classname.equalsIgnoreCase("comp.sys.ibm.pc.hardware") && flagcount <10 ){
				 // System.out.println("docid - "+ docid+ "wscore "+" "+score+" f1value "+ F1_positive);
				//  System.out.println("docid - "+ docid+ "wscore "+" "+score+" f1value "+ F1_positive+ " prec "+prec_positive+ " rec "+rec_positive);
			     // System.out.println("TP "+TP+" FP "+ FP +" FN "+ FN);
			  }
			  flagcount++;
			}
			
			double max_f1value = Collections.max(f1values);
		  class_maxf1value.put(classname,max_f1value);	
			
		}    //end of loop to find max f1 value for individual class
		
				
	}   // end of function to calculate f1 values for all class
	
	
	public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> unsortMap)
    {

        List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Double>>()
        {
            public int compare(Entry<Integer, Double> o1,
                    Entry<Integer, Double> o2)
            {
               
                    return o2.getValue().compareTo(o1.getValue());

                
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Entry<Integer, Double> entry : list)
        {
        	int key = entry.getKey();
        	double val = entry.getValue();
            sortedMap.put(key, val);
        }

        return sortedMap;
    }
	
	
	
	public void write_classification_outputfile(String outfile){
		
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter (outfile));
			String line="";
			int outdocid;
			for(Map.Entry<Integer,String> m : predicted_docID_classlabels.entrySet()){
				
				outdocid = docID_outpput_docID.get(m.getKey());
				line = m.getKey()+ " "+m.getValue() +" "+ outdocid;
				bw.write(line);
				bw.newLine();
			}
			
			
			
			bw.close();
		}
		catch( Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void calc_accuracy(){
		
		String original_class,pred_class;
		int TP=0,TN=0,FP=0,FN=0;
		String str ="alt.atheism";
		int x=0;
		for(int docid : predicted_docID_classlabels.keySet() ){
			x++;
			//if( docID_classlabel.get(docid).equals(str) && docid >=1 && docid <=74) 
			{
				// x++;
			if ( docID_classlabel.get(docid).equals(predicted_docID_classlabels.get(docid)) ){
				TP++;
			}
			else{
				FP++;
			}
			
			}
		}
		
		int accuracy =(int) (((double)TP/x) *100);
		System.out.println("The accuracy is " + accuracy + " % ");
	}
	
	
	
	public static double find_similarity(HashMap<Integer,Double> x , HashMap<Integer,Double> y){
		HashMap<Integer,Double> small,large;
		if(x.size()<y.size()){
			small = x;
			large= y;
		}else{
			small=y;
			large=x;
		}
		double sum =0;
		for(int termid : small.keySet()){
			
			if(large.containsKey(termid)){
				sum += small.get(termid)*large.get(termid);
			}
		}
		
		
		return sum;
		
	}
	
	public  void calc_centroid(){
		System.out.println("insisde calc_centroid function");
		int i=0;
	for( String classname : label_docids.keySet()){
			
			each_class obj = new each_class();
			obj.classlabel = classname; 
			
			int alt_pos_vlen1=0;
			obj.documentIDs = label_docids.get(classname);
			//System.out.println(obj.classlabel);
		   int poscount=0,negcount=0;
		
			for ( int docid : trainset ) 
			{
			
				if( docID_classlabel.get(docid).equals(obj.classlabel)){
					poscount++;
				HashMap<Integer,Double> termvector = unit_docID_TF_Vector.get(docid);
			
				for( int termid : termvector.keySet()){
					
					if(!(obj.positivecentroid.containsKey(termid))){
						obj.positivecentroid.put(termid, (termvector.get(termid)));
					}else{
						double sum = obj.positivecentroid.get(termid);
						sum= (Math.round(sum * 100000000) / 100000000.0);
						sum+= (termvector.get(termid));
						sum= (Math.round(sum * 100000000) / 100000000.0);
						obj.positivecentroid.put(termid,sum);
					}
				}           // end of loop to find positive centroid
				
			}
				else {
				//	System.out.println(docid);
					negcount++;
					HashMap<Integer,Double> termvector = unit_docID_TF_Vector.get(docid);
					
					for( int termid : termvector.keySet()){
						
						if(!(obj.negativecentroid.containsKey(termid))){
							obj.negativecentroid.put(termid, (termvector.get(termid)));
						}else{
							double sum = obj.negativecentroid.get(termid);
							sum= (Math.round(sum * 100000000) / 100000000.0);
							sum+= (termvector.get(termid));
							sum= (Math.round(sum * 100000000) / 100000000.0);
							obj.negativecentroid.put(termid,sum);
						}
					}           // end of loop to find positive centroid
								
				}
			}
			
			
			double centroid_length = 0;
			// divide by no of +ve documents
			for( int termid : obj.positivecentroid.keySet()){
				double sum = obj.positivecentroid.get(termid);
				sum=sum/poscount;
				sum= (Math.round(sum * 100000000) / 100000000.0);
				obj.positivecentroid.put(termid,sum);
				centroid_length += (sum*sum);
			}
			
			centroid_length = (double)Math.sqrt(centroid_length);
			// normailzie the centroid vectors
			for( int termid : obj.positivecentroid.keySet()){
				double sum = obj.positivecentroid.get(termid);
				sum=sum/centroid_length;
				sum= (Math.round(sum * 100000000) / 100000000.0);
				obj.unit_positivecentroid.put(termid,sum);
				
			}
			
			
			double neg_centroid_length = 0;
			// divide by no of +ve documents
					
			for( int termid : obj.negativecentroid.keySet()){
				double sum = obj.negativecentroid.get(termid);
				sum=sum/negcount;
				sum= (Math.round(sum * 100000000) / 100000000.0);
				obj.negativecentroid.put(termid,sum);
				neg_centroid_length += (sum*sum);
				
				
			}
			
			neg_centroid_length = (double)Math.sqrt(neg_centroid_length);
			// normailzie the centroid vectors
			for( int termid : obj.negativecentroid.keySet()){
				double sum = obj.negativecentroid.get(termid);
				sum=sum/neg_centroid_length;
				sum= (Math.round(sum * 100000000) / 100000000.0);
				obj.unit_negativecentroid.put(termid,sum);
				
				
			}
			
			
		
			global_clusters[i++]= obj;
				
		}
	}
	
	class each_class{
		ArrayList<Integer> documentIDs ;
		
	    HashMap<Integer,Double> positivecentroid = new HashMap<Integer,Double>() ;
	    HashMap<Integer,Double> unit_positivecentroid = new HashMap<Integer,Double>();
	    HashMap<Integer,Double> negativecentroid = new HashMap<Integer,Double>();
	    HashMap<Integer,Double> unit_negativecentroid = new HashMap<Integer,Double>();
	   String classlabel;
	   
	}

}


