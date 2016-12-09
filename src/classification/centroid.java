package classification;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.*;
import java.io.*;

public class centroid {

	static Map<Integer, HashMap<Integer, Integer>> docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Integer>>();
	//stores the document term frequency represented vectors
	static Map<Integer, HashMap<Integer, Float>> unit_docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Float>>();
	//stores the document term frequency represented vectors normalised to unit length
	
	static Map<Integer, HashMap<Integer,Integer>> docID_BR_Vector = new LinkedHashMap<Integer, HashMap<Integer,Integer>>();
	//stores the binary representation of document vectors
	static Map<Integer, HashMap<Integer, Integer>> unit_docID_BR_Vector = new LinkedHashMap<Integer, HashMap<Integer,Integer>>();
	//stores the document term frequency represented vectors normalised to unit length
	
	static Map<Integer, HashMap<Integer,Float>> docID_TF_IDF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Float>>();
   //stores the TF*IDF representation of the document vectors
	
	static Map<Integer,Float> docID_length = new LinkedHashMap<Integer,Float>();
	//stores the document ID and the length of its vector to be used in creation of unit document vectors
	static Map<Integer,Float > term_IDf_map =  	new HashMap<Integer,Float>();
	// stores the IDf value of each term to be used in creation of IDF repesentation vector
	
	static HashMap <Integer,Integer> docID_outpput_docID = new LinkedHashMap<Integer,Integer>();
	static HashMap<Integer,String> docID_classlabel = new LinkedHashMap<Integer,String>();
	
	
	static HashMap<Integer,String> predicted_docID_classlabels = new LinkedHashMap<Integer,String>();
	
	static HashMap<String,ArrayList<Integer>> label_docids = new HashMap<String, ArrayList<Integer>>();
	
	static ArrayList<Integer> trainset = new ArrayList<Integer>();
	static ArrayList<Integer> testset = new ArrayList<Integer>();
	
	static HashMap<String,Integer> classlabelmap = new LinkedHashMap<String,Integer>();
	
	static int feature_representation_option =0;
	
	static String outputfile;
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
				 docID_length.put(docid, (float)Math.sqrt(vectorlength));
			
			}
			
			for(int documentid : docID_TF_Vector.keySet() ){                // Normalise the document vectors
				HashMap<Integer,Integer> wordvector = docID_TF_Vector.get(documentid);
				HashMap<Integer,Float> unitwordvector = new HashMap<Integer,Float>();
				Float docvectorlength = docID_length.get(documentid);
				for ( int wordid : wordvector.keySet()){
					unitwordvector.put(wordid, wordvector.get(wordid)/docvectorlength);
				}
				unit_docID_TF_Vector.put(documentid,unitwordvector);
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
		
		//for(String classname : label)
	//	for(Map.Entry m : label_docids.entrySet()){
		//	System.out.println(m.getKey() + " - "+ m.getValue());
	//	}
		System.out.println("Done reading input files .. Calucating centroids .. ");
		int i=0;
		
		for( String classname : label_docids.keySet()){
			
			each_class obj = new each_class();
			obj.classlabel = classname;
			obj.documentIDs = label_docids.get(classname);
			int restdocsize = docID_TF_Vector.size() - obj.documentIDs.size();
			for ( int docid : trainset ) 
			{
				if( obj.documentIDs.contains(docid)){
				HashMap<Integer,Float> termvector = unit_docID_TF_Vector.get(docid);
				for( int termid : termvector.keySet()){
					if(!(obj.positivecentroid.containsKey(termid))){
						obj.positivecentroid.put(termid, (termvector.get(termid)/obj.documentIDs.size()));
					}else{
						float sum = obj.positivecentroid.get(termid);
						sum+= (termvector.get(termid)/obj.documentIDs.size());
						obj.positivecentroid.put(termid,sum);
					}
				}           // end of loop to find positive centroid
				
			}
				else {
					HashMap<Integer,Float> termvector = unit_docID_TF_Vector.get(docid);
					for( int termid : termvector.keySet()){
						if(!(obj.negativecentroid.containsKey(termid))){
							obj.negativecentroid.put(termid, (termvector.get(termid)/restdocsize));
						}else{
							float sum = obj.negativecentroid.get(termid);
							sum+= (termvector.get(termid)/restdocsize);
							obj.negativecentroid.put(termid,sum);
						}
					}           // end of loop to find positive centroid
								
				}
			}
			global_clusters[i++]= obj;
		}
		
		System.out.println("Training phase done.. cetnroids found for 20 binary classifiers ");
		
		for ( int docid : testset){
			float positive_similarity=0;
			float negative_similarity=0;
			System.out.println(docid );
			HashMap<Integer,Float> termvector =  unit_docID_TF_Vector.get(docid);
			float similarity_diff, max_similarity_diff=-2;
			for(each_class obj : global_clusters ){
				positive_similarity = find_similarity(termvector, obj.positivecentroid) ;
				negative_similarity = find_similarity(termvector, obj.negativecentroid) ;
				similarity_diff = positive_similarity - negative_similarity;
				System.out.println(similarity_diff );
				if(similarity_diff>max_similarity_diff){
					max_similarity_diff = similarity_diff;
					//docID_classlabel.put(docid, obj.classlabel);
					predicted_docID_classlabels.put(docid,obj.classlabel);
				}
				
			}
		}
		
		System.out.println(" each test instance ran");
		
		for( Map.Entry m : predicted_docID_classlabels.entrySet()){
			System.out.println(m.getKey() + " - "+m.getValue());
		}
		
		
	}   // End of main 
	
	public static float find_similarity(HashMap<Integer,Float> x , HashMap<Integer,Float> y){
		HashMap<Integer,Float> small,large;
		if(x.size()<y.size()){
			small = x;
			large= y;
		}else{
			small=y;
			large=x;
		}
		float sum =0;
		for(int termid : small.keySet()){
			
			if(large.containsKey(termid)){
				sum += small.get(termid)*large.get(termid);
			}
		}
		
		
		return sum;
		
	}

}


class each_class{
	ArrayList<Integer> documentIDs ;
    HashMap<Integer,Float> positivecentroid ;
    HashMap<Integer,Float> negativecentroid;
   String classlabel;
   
}