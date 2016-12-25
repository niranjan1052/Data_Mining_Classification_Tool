//array implementation
package ridge_reg;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;


class f1obj
{
	int id;
	double pscore;
}
public class regression {

	static Map<Integer, HashMap<Integer, Double>> docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
	//stores the document term frequency represented vectors
	static Map<Integer, HashMap<Integer, Double>> unit_docID_TF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
	//stores the document term frequency represented vectors normalised to unit length
		static double lambda ; // = 1.0;
	static LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> train_unit_docid_TF_Vecotr = new LinkedHashMap<Integer, LinkedHashMap<Integer,Double>>();
	//stores the training set document term frequency represented vectors normalised to unit length

	static LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> test_unit_docid_TF_Vecotr = new LinkedHashMap<Integer, LinkedHashMap<Integer,Double>>();
	//stores the test set document term frequency represented vectors normalised to unit length
 static double[] lambda_arr = {0.1,1.0};

	static Map<Integer, HashMap<Integer,Double>> docID_TF_IDF_Vector = new LinkedHashMap<Integer, HashMap<Integer,Double>>();
        //stores the TF*IDF representation of the document vectors

	static Map<Integer,Double> docID_length = new LinkedHashMap<Integer,Double>();
	//stores the document ID and the length of its vector to be used in creation of unit document vectors
	static Map<Integer,Double > term_IDf_map =  	new HashMap<Integer,Double>();
	// stores the IDf value of each term to be used in creation of IDF repesentation vector

	static HashMap <Integer,Integer> docID_outpput_docID = new LinkedHashMap<Integer,Integer>();

	static HashMap<Integer,String> docID_classlabel = new LinkedHashMap<Integer,String>();
	//data from rlabel file each docid and its label
	static HashMap<String,LinkedHashMap<Integer,Double> >  class_docid_wscores = new LinkedHashMap<String, LinkedHashMap<Integer,Double> > ();
	// stores the classlabel and a hashmap of wscores for each of the document ids

	  static int no_of_attributes;
	static HashMap<Integer,String> predicted_docID_classlabels = new LinkedHashMap<Integer,String>();

	static HashMap<String,ArrayList<Integer>> label_docids = new LinkedHashMap<String, ArrayList<Integer>>();

	static ArrayList<Integer> trainset = new ArrayList<Integer>();
	static ArrayList<Integer> testset = new ArrayList<Integer>();

	static HashMap<String,Integer> classlabelmap = new LinkedHashMap<String,Integer>();

	static int feature_representation_option = 1 ;                   // 1 for Term frequency
	static HashMap<String , Double>  class_maxf1value = new LinkedHashMap<String,Double> ();
    static HashMap<String,ArrayList<Integer>> label_termids = new LinkedHashMap<String,ArrayList<Integer>>();
	static double[] termindex_IDF ;
	static String outputfile="out.txt";
	static String trainfile = "./20newsgroups_ridge.train";
	static String testfile = "./20newsgroups_ridge.val";
	static String feature_labelfile;
	static String input_rlabelfile= "./20newsgroups.rlabel";
	static String wordlabelfile = "./20newsgroups_word.clabel";

	static ArrayList<Integer> feature_labels = new ArrayList<Integer>();
	static String inputfile= "./20newsgroups_word.ijv";

	static each_class[] global_clusters = new each_class[20];

	public static void readfile1(){
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


  no_of_attributes = feature_labels.size();


	}

	public static void readinputfile () {


		 System.out.println("\n Readling input file \n");
		try{
			BufferedReader br = new BufferedReader( new FileReader(inputfile));
			String line = null;
			int docid,termid;
			double termfreq;
	        int vectorlength =0;
	        int doccount=0;
	        int[] term_doccount = new int [no_of_attributes+1];
			while((line=br.readLine())!=null){
				docid = Integer.parseInt(line.split(" ")[0]);
				termid = Integer.parseInt(line.split(" ")[1]);
				if(trainset.contains(docid))
				{
			    	    doccount = term_doccount[termid];
				    doccount+=1;
				    term_doccount[termid]=doccount;
				}
				if( feature_representation_option ==1|| feature_representation_option ==2) {
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

		  //	docID_TF_Vector.clear();
			//unit_docID_TF_Vector.clear();
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

	}

	public static void calc_accuracy(){

		String original_class,pred_class;
		int TP=0,TN=0,FP=0,FN=0;
	//	String str ="alt.atheism";
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


	public static void main(String[] args) {
		// TODO Auto-generated method stub

       long startTime = System.nanoTime();

	//	inputfile = "./20newsgroups_word.ijv";
	//	outputfile = "classification_colution";
       System.out.println(" train file  is "+ trainfile + "  test file is  "+ testfile);

       readfile1();

       termindex_IDF = new double [no_of_attributes+1];
	   	readinputfile();


		System.out.println("Done reading input files .. Calucating w .. ");
		no_of_attributes = feature_labels.size();
		regression ridgeobj = new regression();

   for( String classname : label_docids.keySet()){
     HashMap<Integer,Double> term_freq = new LinkedHashMap<Integer,Double>();
		 ArrayList<Integer> docids = new ArrayList<Integer> ();
		 		 ArrayList<Integer> termids = new ArrayList<Integer> ();
             docids= label_docids.get(classname);
             
			// termids = label_termids.get(classname);
			 int termindex=1;
			 termids.add(0);
			for( int did : docids){
			term_freq = unit_docID_TF_Vector.get(did);
		 for(Map.Entry<Integer,Double> m : term_freq.entrySet()){
			 if(!(termids.contains(m.getKey())) ){
				 termids.add(termindex, m.getKey());
				 termindex++;
			 }
		 }
 	
       }
			 label_termids.put(classname,termids);
	 }

		System.out.println(" Calucalting W  ");
		double [] avg = new double[lambda_arr.length];

       for(int d =0 ;d<lambda_arr.length;d++){
    	 
    	   lambda = lambda_arr[d];
    	   ridgeobj.calc_w_regression();
    	   
    	   int classno=0;
   		LinkedHashMap<Integer,LinkedHashMap<Integer,Double> > X_test = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();
   		X_test.putAll(test_unit_docid_TF_Vecotr);

   		ArrayList<Double> xdotw = new ArrayList<Double>();
   		LinkedHashMap<Integer,Double> docid_class_wscore = new LinkedHashMap<Integer,Double>() ; // stores the max wscore value for each docid
   		for(int k : X_test.keySet()){
   			docid_class_wscore.put(k,-1.0);
   		}

   		 for( String classname : label_docids.keySet())
   		 {
   			 LinkedHashMap<Integer,Double> docid_wscore = new LinkedHashMap<Integer,Double>();
   			// if(classno>=1){
   			//	 break;
   			// }
   			each_class obj= global_clusters[classno];
   			//System.out.println(" for loop classname "+ classname + " gloalcluster array name "+ obj.classlabel);
   			int rowcount=0;
   			xdotw = matrix_vector_multiplication(X_test,obj.W);
   			ArrayList<Integer> rowkeys = new ArrayList<Integer>();
   			for(int k: X_test.keySet()){
   				rowkeys.add(k);
   			}
   			int docid;
   			double classwscore;
   			for( double  yi : xdotw){
   				//if(rowcount<500)
   				//System.out.println(" row "+ rowcount+ " - y value  "+ yi);
   				docid = rowkeys.get(rowcount);
   				docid_wscore.put(docid, yi);
   			    classwscore = docid_class_wscore.get(docid);
   			    if(yi > classwscore){
   			    	classwscore = yi;
   			    	docid_class_wscore.put(docid,classwscore);
   			    	predicted_docID_classlabels.put(docid,obj.classlabel);
   			    }
   			    rowcount++;
   			}
   			class_docid_wscores.put(classname, docid_wscore);
   			
   			obj.wscores.putAll(docid_wscore);
   		  	classno++;
   		 }

   		calc_f1value();
   		double sum =0;
   		for ( Map.Entry<String,Double> m : class_maxf1value.entrySet()){

			 sum+= m.getValue();
   			System.out.println("MAXF1: "+m.getKey() +"  "+m.getValue());
		}
   		 avg[d] = (double)sum/class_maxf1value.size();
   		 System.out.println(" avg for d = "+d + "is "+ avg[d]);
    	   
       }
		
       double maxavg=0.0;
       double bestlambda=1.0;
       for(int d = 0;d<lambda_arr.length;d++){
    	   if(avg[d] > maxavg)
    	   {
    		   maxavg=avg[d];
    		   bestlambda = lambda_arr[d];
    	   }
       }
		
      trainset.clear();
      testset.clear();
      feature_labels.clear();
     
      train_unit_docid_TF_Vecotr.clear();
      test_unit_docid_TF_Vecotr.clear();
      
      trainfile = "./20newsgroups.train";
      testfile = "./20newsgroups.test";
      System.out.println(" train file  is "+ trainfile + "  test file is  "+ testfile);
      readfile1();
      
       System.out.println("best lambda is "+ bestlambda);
        lambda = bestlambda;

    	for(int documentid : docID_TF_Vector.keySet() ){                // Normalise the document vectors
			HashMap<Integer,Double> wordvector = docID_TF_Vector.get(documentid);
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
        
        
       
        ridgeobj.calc_w_regression();
     	   
     	   int classno=0;
    		LinkedHashMap<Integer,LinkedHashMap<Integer,Double> > X_test = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();
    		X_test.putAll(test_unit_docid_TF_Vecotr);

    		ArrayList<Double> xdotw = new ArrayList<Double>();
    		LinkedHashMap<Integer,Double> docid_class_wscore = new LinkedHashMap<Integer,Double>() ; // stores the max wscore value for each docid
    		for(int k : X_test.keySet()){
    			docid_class_wscore.put(k,-1.0);
    		}

    		 for( String classname : label_docids.keySet())
    		 {
    			 LinkedHashMap<Integer,Double> docid_wscore = new LinkedHashMap<Integer,Double>();
    			// if(classno>=1){
    			//	 break;
    			// }
    			each_class obj= global_clusters[classno];
    			System.out.println(" for loop classname "+ classname + " gloalcluster array name "+ obj.classlabel);
    			int rowcount=0;
    			xdotw = matrix_vector_multiplication(X_test,obj.W);
    			ArrayList<Integer> rowkeys = new ArrayList<Integer>();
    			for(int k: X_test.keySet()){
    				rowkeys.add(k);
    			}
    			int docid;
    			double classwscore;
    			for( double  yi : xdotw){
    				//if(rowcount<500)
    				//System.out.println(" row "+ rowcount+ " - y value  "+ yi);
    				docid = rowkeys.get(rowcount);
    				docid_wscore.put(docid, yi);
    			    classwscore = docid_class_wscore.get(docid);
    			    if(yi > classwscore){
    			    	classwscore = yi;
    			    	docid_class_wscore.put(docid,classwscore);
    			    	predicted_docID_classlabels.put(docid,obj.classlabel);
    			    }
    			    rowcount++;
    			}
    			class_docid_wscores.put(classname, docid_wscore);
    			
    			obj.wscores.putAll(docid_wscore);
    		  	classno++;
    		 }

    		calc_f1value();
    		
    		 
         
         

		System.out.println("Training phase done.. cetnroids found for 20 binary classifiers ");
		//System.out.println("testset len "+ testset.size());

		calc_accuracy();

		System.out.println("Printing the classification output to file... ");
		System.out.println("\nPrinting the max F1 values for each class \n");

		calc_f1value();

		//HashMap<Integer,Double> docid_wscore = new HashMap<Integer,Double>();

		ridgeobj.write_classification_outputfile(outputfile);


		for ( Map.Entry<String,Double> m : class_maxf1value.entrySet()){

			System.out.println("MAXF1: "+m.getKey() +"  "+m.getValue());
		}

		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime)/1000000000) + " seconds");




	}   // End of main


public void write_classification_outputfile(String outfile){

		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter (outfile));
			String line="";
			int outdocid;
			for(Map.Entry<Integer,String> m : predicted_docID_classlabels.entrySet()){

				outdocid = docID_outpput_docID.get(m.getKey());
				//line = m.getKey()+ " "+m.getValue() +" "+ outdocid;
				line = outdocid + " , "+m.getValue();
				bw.write(line);
				bw.newLine();
			}

			bw.close();
		}
		catch( Exception e){
			System.out.println(e.getMessage());
		}
	}


public static void calc_f1value(){


	for ( String classname: class_docid_wscores.keySet()){
             List<f1obj> objlist = new ArrayList<f1obj>();
           double maxf1=0.0;  
		HashMap<Integer,Double> docid_wscore = class_docid_wscores.get(classname);
		for(Map.Entry<Integer,Double> m : docid_wscore.entrySet()){
			f1obj temp = new f1obj();
			temp.id = m.getKey();
			temp.pscore = m.getValue();
			objlist.add(temp);
		}
		
		for (f1obj o : objlist)
		{ double f1score;
		  double tp = 1, tn = 0, fp = 0, fn = 0;
		  double prec, recall;
		  
		  
			for (f1obj o2: objlist)
			{
				if (o.id != o2.id)
				{
					if (o.pscore < o2.pscore)
					{
						if (predicted_docID_classlabels.get(o2.id).equals(classname))
						{
							//fn++;
							tp++;
						}
						else 
						{
							//tn++;
							fp++;
						}
					}
					
					else if (o.pscore > o2.pscore)
					{
						if (predicted_docID_classlabels.get(o2.id).equals(classname))
						{
							//tp++;
							fn++;
						}
						else
						{
							//fp++;
							tn++;
						}
					}
					
				}
			}
			
			prec = tp/(tp+fp);
			recall = tp/(tp+fn);
			
			if(prec == 0 && recall == 0)
			{
				f1score = 0;
			}
			
			else
			{
				f1score = (2*prec*recall/(prec+recall));
				
			}
			if(f1score > maxf1)
			{
				maxf1 = f1score;
			}
			
		}
		 class_maxf1value.put(classname,maxf1);
		
	

	}    //end of loop to find max f1 value for individual class


}   // end of function to calculate f1 values for all class



	public  void calc_w_regression()
	{
	   System.out.println("insisde calc_W function");
	   int classno=0;
	   LinkedHashMap<Integer,Double> ymatrix = new LinkedHashMap<Integer,Double>();
		ArrayList<Double> ymatrix1 = new ArrayList<Double>();
	   for( String classname : label_docids.keySet())
	   {
			//if(classno>=1) break;
			//populate y matrix for current +ve class

			for(int recid : train_unit_docid_TF_Vecotr.keySet()){
				if(docID_classlabel.get(recid).equals(classname))
					{

				     ymatrix1.add(1.0); }
				else
				{

					 ymatrix1.add(0.0);
					 }
			}

			each_class obj = new each_class();
			obj.classlabel = classname;

			int alt_pos_vlen1=0;
			obj.documentIDs = label_docids.get(classname);
			//LinkedHashMap<Integer,Double> xitranspose = new LinkedHashMap<Integer,Double>();
			ArrayList<Double> xitranspose = new ArrayList<Double>();

			LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> X_i = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();

			LinkedHashMap<Integer,Double> W = new LinkedHashMap<Integer,Double>();
			//stores the term or column number and its w value
			int t=1;
			for(t=1;t<=feature_labels.size();t++){

				W.put(t,0.0);
			}

			System.out.println("done populating initial w");
			double wdenominator,wnumerator,finalwi;
			LinkedHashMap<Integer,Double> w_i = new LinkedHashMap<Integer,Double>();
			ArrayList<Integer> w_i_1 = new ArrayList<Integer>();
			int featurecount =0;
			double total_error=0,prev_error=0;
			double currentwvalue ;
			X_i.putAll(train_unit_docid_TF_Vecotr);
			w_i.putAll(W);
			System.out.println("feature_labels.size() "+w_i.size());
			int iterationcount =0;
			//LinkedHashMap<Integer,Double>  xi_dot_wi = matrix_vector_multiplication(X_i,W);
			ArrayList<Double> xi_dot_wi1 = new ArrayList<Double>();
			xi_dot_wi1 = matrix_vector_multiplication(X_i,w_i);


			//LinkedHashMap<Integer,Double> Xitransdotcurrentwi = new LinkedHashMap<Integer,Double>();

			ArrayList<Double> Xitransdotcurrentwi = new ArrayList<Double>();

			//LinkedHashMap<Integer,Double> prevxitransdotprevwi = new LinkedHashMap<Integer,Double>();
			ArrayList<Double> prevxitransdotprevwi = new ArrayList<Double>();

			for(int m =0;m<xi_dot_wi1.size();m++){

				prevxitransdotprevwi.add(0.0);
				Xitransdotcurrentwi.add(0.0);
			}
	//		 double  total_error=0.0;   // first part of error equation
			ArrayList<Integer> term_ids = new ArrayList<Integer> ();
			term_ids = label_termids.get(classname);
            double change_of_error;

			for( iterationcount =0;iterationcount<1;iterationcount++)    // maximum i have given 60 iterations to converge but it breaks the moment it converges with error difference less than 0.001

			{
				  featurecount=0;
			     for(int i=1;i<=feature_labels.size();i++ )
			      {
			    	     if(!(term_ids.contains(i))){
			    	    	 continue;
			    	     }

				       for(int j : train_unit_docid_TF_Vecotr.keySet())   // j is the document id, so for each roc id i.e each row in matrix
					       {
							HashMap<Integer,Double> row = train_unit_docid_TF_Vecotr.get(j);
							if(row.get(i)!=null  )
						      {
								xitranspose.add( row.get(i));
							  }else{
								xitranspose.add( 0.0);
							  }

					       }      // end of looping all rows for a column i


						 currentwvalue = w_i.get(i);


						 for(int m =0; m<xitranspose.size();m++){
							 Xitransdotcurrentwi.set(m,((xitranspose.get(m))*currentwvalue));

						 }
					    double newval;



						 for(int m =0; m < xitranspose.size();m++ ){

							 newval = xi_dot_wi1.get(m)- Xitransdotcurrentwi.get(m) + prevxitransdotprevwi.get(m);
							 // xi_dot_wi.put(m.getKey(), (newval));
							 xi_dot_wi1.set(m,newval );
						 }


			         	 ArrayList<Double> yminusxiwi = vector_minus_vector(ymatrix1,xi_dot_wi1);

			             wnumerator = vector_vector_multiplication(xitranspose,yminusxiwi);
			             wdenominator = vector_vector_multiplication(xitranspose,xitranspose);
			             wdenominator += lambda;
			             finalwi  =  (double)wnumerator/wdenominator ;
			           if(finalwi <0){
			        	 //  System.out.println(finalwi);
			        	   finalwi=0;
			           }
			             W.put(i, finalwi);

			             w_i.put(i, finalwi);

			         //  prevxitransdotprevwi.clear();

			             for(int m =0;m<xi_dot_wi1.size();m++){

			            	 newval = xitranspose.get(m)*finalwi ;
			            	 prevxitransdotprevwi.set(m,newval);
			 			}


					     X_i.clear();
					     xitranspose.clear();
					     //calculation of least square error

					     featurecount++;
					    if(featurecount%1000==0){
					    //	System.out.println(" columns done "+ featurecount);
					    }

			        }   // end of loop calculation of new W for kth iteration

			     System.out.println(" columns done "+ featurecount);

				 // LinkedHashMap<Integer,Double> XdotW  = matrix_vector_multiplication(train_unit_docid_TF_Vecotr, W);

			    ArrayList<Double> XdotW  = matrix_vector_multiplication(train_unit_docid_TF_Vecotr, W);

			    ArrayList<Double> XdotWminusY = vector_minus_vector(XdotW,ymatrix1);


				  for(int m=0;m<XdotWminusY.size();m++){
					  total_error+= (XdotWminusY.get(m)*XdotWminusY.get(m));
				  }


				  double wl2norm=0;   // second part of error equation
				  for(Map.Entry<Integer, Double> m : W.entrySet()){

					   wl2norm += ( m.getValue()*m.getValue());
				    }
				   wl2norm *=lambda;


				   total_error += total_error+wl2norm;       // total error

				   if(iterationcount==0)
                     {
						 change_of_error= total_error -prev_error;
					 }
					 else{
						 change_of_error = prev_error-total_error;
					 }
				   System.out.println("\n Total least square error for iteration of i = "+ iterationcount+ " is "+ total_error);
				   System.out.println("change in error is "+ change_of_error);
				   int nonzerow=0;
				   int negw=0,posw=0;
				   if(change_of_error<0.001){
					  System.out.println("Converged ");
					  for(Map.Entry<Integer,Double> m: w_i.entrySet()){
						if(m.getValue()<0)
							negw++;
						else if(m.getValue()>0) posw++;
					  }
					  System.out.println("number of non zero values in w "+ (posw+negw)+ " \n total negative w values "+ negw+"\n positive w values "+ posw);
				    	  break;
				    }

				   prev_error= total_error;
				   total_error=0;

	       }   // end of loop for k iterations to converge

		   obj.W.putAll(W);
		   global_clusters[classno++]= obj;
		   ymatrix1.clear();


		}  // end of for each classlabel classifier
	}



	public static double vector_vector_multiplication(ArrayList<Double> X , ArrayList<Double>  Y )
	{
		double result;
		double xval;
		double yval;
		double sum=0;
		for(int m =0;m<X.size();m++){
			xval = X.get(m);
			yval = Y.get(m);
			sum+= (xval*yval);
		}
		result = sum;
		return result;
	}


	/*public static double vector_vector_multiplication(LinkedHashMap<Integer,Double> X ,LinkedHashMap<Integer,Double> Y )
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
	*/


	public static ArrayList<Double> vector_minus_vector (ArrayList<Double> X , ArrayList<Double>  Y){
		ArrayList<Double>  result = new ArrayList<Double> ();
		double xval,yval;

		for(int m =0;m<X.size();m++){
			xval = X.get(m);
			yval = Y.get(m);
			result.add(m,(xval-yval));
		}

		return result;
	}


/*	public static LinkedHashMap<Integer,Double> vector_minus_vector (LinkedHashMap<Integer,Double> X , LinkedHashMap<Integer,Double> Y){
		LinkedHashMap<Integer,Double> result = new LinkedHashMap<Integer,Double>();
		for( int rowkey : X.keySet()){
			double xval = X.get(rowkey);
			double yval = Y.get(rowkey);
			result.put(rowkey, (xval-yval));
		}
		return result;
	}

	*/

	public static ArrayList<Double>  matrix_vector_multiplication(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> X ,LinkedHashMap<Integer,Double> W)
	{
		ArrayList<Double> result =  new ArrayList<Double>();
	//	System.out.println("-- "+X.keySet().size());
		int flagcnt=0;
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
				flagcnt++;
				result.add(sum);

			 }

			return result;
	}


/*	public static LinkedHashMap<Integer,Double>  matrix_vector_multiplication(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> X ,LinkedHashMap<Integer,Double> W)
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
	*/
	class each_class{
		ArrayList<Integer> documentIDs ;
		HashMap<Integer,Double> wscores = new HashMap<Integer,Double>() ;
	    String classlabel;
	    LinkedHashMap<Integer,Double> W = new LinkedHashMap<Integer,Double>();

	}

}