package classification;
import java.util.Map;

import javax.swing.text.Position;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.*;
import java.io.*;

public class centroid {

	static Map<Integer, HashMap<Integer, Integer>> docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Integer>>();
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
	
	
	static HashMap<Integer,String> predicted_docID_classlabels = new LinkedHashMap<Integer,String>();
	
	static HashMap<String,ArrayList<Integer>> label_docids = new LinkedHashMap<String, ArrayList<Integer>>();
	
	static ArrayList<Integer> trainset = new ArrayList<Integer>();
	static ArrayList<Integer> testset = new ArrayList<Integer>();
	
	static HashMap<String,Integer> classlabelmap = new LinkedHashMap<String,Integer>();
	
	static int feature_representation_option = 0 ;
	
	static String outputfile="out.txt";
	static String trainfile = "./20newsgroups.train";
	static String testfile = "./20newsgroups.test";
	static String feature_labelfile;
	static String input_rlabelfile= "./20newsgroups.rlabel";
	
	static String inputfile= "./20newsgroups_word.ijv";
	
	static each_class[] global_clusters = new each_class[20];
	
	public static void readinputfile () {
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
				HashMap<Integer,Double> unitwordvector = new HashMap<Integer,Double>();
				Double docvectorlength = docID_length.get(documentid);
				for ( int wordid : wordvector.keySet()){
					unitwordvector.put(wordid, wordvector.get(wordid)/docvectorlength);
				}
				unit_docID_TF_Vector.put(documentid,unitwordvector);
			//	if(documentid==5719){
			//		System.out.println("for 5719 ");
			//		System.out.println(wordvector);
			//		System.out.println(docvectorlength);
			//		System.out.println(unitwordvector);
			//	} 
			}
					
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
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		inputfile = "./20newsgroups_word.ijv";
		outputfile = "classification_colution";
		readinputfile();
		
		
		System.out.println("Done reading input files .. Calucating centroids .. ");
		centroid centroidobj = new centroid();
		
		centroidobj.calc_centroid();
		
		System.out.println("Training phase done.. cetnroids found for 20 binary classifiers ");
		System.out.println("testset len "+ testset.size());
		
		for ( int docid : testset){
			if(docid == 5719){
				System.out.println("inside testinf 5719 ");
			}
			double positive_similarity=0;
			double negative_similarity=0;
		
			HashMap<Integer,Double> termvector =  unit_docID_TF_Vector.get(docid);
			double similarity_diff, max_similarity_diff=-2;
			for(each_class obj : global_clusters ){
				
				positive_similarity = find_similarity(termvector, obj.unit_positivecentroid) ;
				negative_similarity = find_similarity(termvector, obj.unit_negativecentroid) ;
				similarity_diff = positive_similarity - negative_similarity;
				
				if(similarity_diff>max_similarity_diff){
					max_similarity_diff = similarity_diff;
					predicted_docID_classlabels.put(docid,obj.classlabel);
				}
								
			}
		}
		
		System.out.println(" each test instance ran mm "+ predicted_docID_classlabels.size());
		
		for( Map.Entry m : predicted_docID_classlabels.entrySet()){
			 System.out.println(m.getKey() + " - "+m.getValue());
		}
		
		centroidobj.write_classification_outputfile(outputfile);
		
		calc_accuracy();
	
	}   // End of main 
	
	public void write_classification_outputfile(String outfile){
		
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter (outfile));
			String line = "12"+" "+"class";
			for(int i=0;i<10;i++)
			{	bw.write(line);
			    bw.newLine();
			}
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
		
		double accuracy = (double)TP/x;
		System.out.println("The accuracy is " + accuracy );
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
			//obj.positivecentroid =null;
		//	obj.negativecentroid =null;
			int alt_pos_vlen1=0;
			obj.documentIDs = label_docids.get(classname);
			System.out.println(obj.classlabel);
		   int poscount=0,negcount=0;
		//	int restdocsize = trainset.size() - obj.documentIDs.size();
			for ( int docid : trainset ) 
			{
				//if(docid==75){
			//		 System.out.println("ending the positive zone "+ docID_classlabel.get(docid) + " and "+ obj.classlabel);
			//		 alt_pos_vlen1= obj.positivecentroid.size();
			//	}
				if( docID_classlabel.get(docid).equals(obj.classlabel)){
					poscount++;
				HashMap<Integer,Double> termvector = unit_docID_TF_Vector.get(docid);
			//	System.out.println("unit vector of doc "+ docid);
			//	System.out.println(termvector);
				for( int termid : termvector.keySet()){
					
					if(!(obj.positivecentroid.containsKey(termid))){
						obj.positivecentroid.put(termid, (termvector.get(termid)));
					}else{
						double sum = obj.positivecentroid.get(termid);
					//	sum= (Math.round(sum * 100000000) / 100000000.0);
						sum+= (termvector.get(termid));
					//	sum= (Math.round(sum * 100000000) / 100000000.0);
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
			
			
			if(i<1)
			{	//System.out.println(obj.positivecentroid);
			System.out.println(obj.positivecentroid.size());
			//System.out.println(obj.negativecentroid);
			System.out.println(obj.negativecentroid.size());
			  System.out.println(obj.documentIDs);
			  System.out.println(poscount+" & "+ negcount);
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


