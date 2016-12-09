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
	
	static ArrayList<Integer> trainset = new ArrayList<Integer>();
	static ArrayList<Integer> testset = new ArrayList<Integer>();
	
	
	
	static int feature_representation_option =1;
	
	static String inputfile,outputfile, trainfile, testfile, feature_labelfile;
	static String input_rlabelfile= "./20newsgroups.rlabel";
	
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
			}
			
		}catch (Exception e){
			
		}
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		inputfile = "./20newsgroups_word.ijv";
		outputfile = "classification_colution";
		readinputfile();
		
	}

}
