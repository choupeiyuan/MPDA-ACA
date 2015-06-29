/*==================================================================
Purpose: Main Class to Run the Attribute Clustering Algorithm

Date:   15 Dec 2009
Change: Tidy up the code.

Date:   3 Mar 2010
Change: Critical Bug Fix:
		The count of attribute value was wrong.
		
Date:   30 Mar 2010
Change: Allow to set a range of k to detect the best cluster configuration.

Date:   20 Apr 2010
Change: Allow to set the number of trials to run ACA.

To excute the program:
<k-(k+n)> means from k to k+n clusters.
Usage 1) java AttributeClustering <dataset> <output folder> <k-(k+n),trial> <iteration>
Usage 2) java AttributeClustering <dataset> <output folder> <k-(k+n),trial> <iteration> <1> <att_target>
		 Usage 2 is for finding all IR values for 1 particular attribute.
Usage 3) java AttributeClustering <dataset> <output folder> <k-(k+n),trial> <iteration> <IR Table>
		 Usage 3 is for finding attribute clusters, given a precomputed IR Table.

Example to execute the program:
Usage 1) java AttributeClustering K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\Coking_DC1.csv "K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\Output\1\\" 2-3 1
Usage 2) java AttributeClustering K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\Coking_DC1.csv "K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\Output\1\\" 2-3 1 1 21
Usage 3) java AttributeClustering K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\CokingData.csv "K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\Output\ACA\\" 2-47,50 100 K:\PQ713a\Gene\Project\Prof.Wong\Discretization\IntrinsicClassDependenceDiscretization\Dataset\MixedMode\Coking\R.csv
==================================================================*/
package mixedmodeaca;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


public class AttributeClustering {

	/**
	 * @param args
	 */
	int currentIteration;
	int numOfIteration;
	int k; //no. of clusters
	ArrayList<ArrayList<String>> cluster; //Items inside clusters
	ArrayList<ArrayList<String>> mode; //mode [0 - Attribute Index, 1 - MR Value] in clusters
	//mode.get(k).get(0) is the attribute index
	//mode.get(k).get(1) is the interdependence redundancy measure value
	ArrayList<String> oldMode; //for termination criteria use
	int numOfAttribute = 0;
	String originalFString, inFString, outFString;
	List<Integer> randomList;
	int latestAssign;
	//char[][] att; //to store all attribute values
	String[][] att; //to store all attribute values
	//ArrayList<HashSet<String>> distincyVal;
	ArrayList<ArrayList<String>> distincyVal;
	ArrayList<ArrayList<Integer>> distinctValCnt;
	double[][] attIR; //to store all Interdependence Redanduncy Measure values of all attributes
        HashMap<String, String> attName = new HashMap<String, String>();
        boolean createRTableOnly = false;
        String rTableName;
	
	
	public AttributeClustering(String originalFString, String inFString, String outFString, int k, int iteration, String IRTablePath, int m, int att_target) {
		currentIteration = 0;
		numOfIteration = iteration;
		this.k = k;
		cluster = new ArrayList<ArrayList<String>>();
		mode = new ArrayList<ArrayList<String>>();
		oldMode = new ArrayList<String>();
		this.originalFString = originalFString;
		
		if (!inFString.equals("")) { //hasSeparatedAtt = true
			File dir = new File(inFString); this.inFString = inFString;
			String[] children = dir.list(); 
			numOfAttribute = children.length;
			//System.out.println("numOfAttribute = "+numOfAttribute);
		}
		
		(new File(outFString)).mkdir(); 
		//String tNow = now();
		//(new File(outFString+tNow)).mkdir();
                //this.outFString = outFString+tNow;
                 this.outFString = outFString;
		
		if (IRTablePath.equals(""))
			makeIRTable(!inFString.equals(""), m, att_target);
		else
			loadIRTable(IRTablePath);

	}
	
	//AttributeClustering(inFString, i, IRTablePath, m, att_target);
	public AttributeClustering(String inFString, int k, String IRTablePath, int m, int att_target) {
		currentIteration = 0;
		//numOfIteration = iteration;
		this.k = k;
		cluster = new ArrayList<ArrayList<String>>();
		mode = new ArrayList<ArrayList<String>>();
		oldMode = new ArrayList<String>();
		//this.originalFString = originalFString;
		
		if (!inFString.equals("")) { //hasSeparatedAtt = true
			File dir = new File(inFString); this.inFString = inFString;
			String[] children = dir.list(); 
			numOfAttribute = children.length;
			//System.out.println("numOfAttribute = "+numOfAttribute);
		}
		
		//(new File(outFString)).mkdir(); 
		//String tNow = Motif_UPSEC.now();
		//(new File(outFString+tNow)).mkdir(); this.outFString = outFString+tNow;
		
		if (IRTablePath.equals(""))
			makeIRTable(!inFString.equals(""), m, att_target);
		else
			loadIRTable(IRTablePath);
	}

        public AttributeClustering(String inFString, int[] colID, String outFString, String outFilename) { //for creating R table only
            //String tNow = now();
            createRTableOnly = true;
            rTableName = outFilename;
            this.inFString = inFString;
            (new File(outFString)).mkdir();
            this.outFString = outFString;
            readDataset(colID);
            makeIRTable(false, 0, 0);
        }
        public void readDataset(int[] colID) {
           HashSet<Integer> colSet = new HashSet<Integer>();
            for (int i = 0 ; i < colID.length ; i++)
                colSet.add(colID[i]);
            try { //load selected attribute values
                    //System.out.println("inFString = " + inFString);
                    System.out.println("Loading Attribute");
                    FileInputStream fis = new FileInputStream(this.inFString);
                    BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
                    String line = null;
                    ArrayList<ArrayList<String>> sAtt = new ArrayList<ArrayList<String>>();
                    int cnt = 0;
                    while( (line = dataFile.readLine()) != null) {
                        int col = 0;
                        int selectedCol = 0;
                        SimpleTokenizer st = new SimpleTokenizer(line,",");
                        while (st.hasMoreTokens()) {
                            //System.out.println("Debug: " + col);
                           if (cnt == 0) {
                               //ignore 1st row since it is a label
                               //but initialize sAtt
                                if (colSet.contains(col)) {
                                    ArrayList<String> row = new ArrayList<String>();
                                    sAtt.add(row);
                                    st.nextToken();
                                } else
                                    st.nextToken();
                                col++;
                            } else {
                                if (colSet.contains(col)) {
                                    sAtt.get(selectedCol++).add(st.nextToken());
                                } else
                                    st.nextToken();
                                col++;
                            }
                        }
                        cnt++;
                    }
                    numOfAttribute = sAtt.size();
                    dataFile.close();
                    att = new String[numOfAttribute][cnt-1];
                    for ( int attX = 0; attX < sAtt.size() ; attX++)
                        for ( int rec = 0; rec < sAtt.get(attX).size() ; rec++){
                            att[attX][rec] = sAtt.get(attX).get(rec);
                        }
                    //for (int i = 0; i < att.length ; i++) {
                        //for (int j = 0; j < att[i].length ; j++) {
                          //  System.out.print(att[i][j] + ",");
                        //}
                        //System.out.println();
                    //}
                    //for ( int attX = 0; attX < sAtt.size() ; attX++)
                      //  System.out.println(sAtt.get(attX));
                    System.out.println("load all attribute values done!");
                    } catch (Exception e) {
                            System.out.println("load all attribute values err: " + e);
                    }
        }

	public void resetParam() {
		currentIteration = 0;
		cluster = new ArrayList<ArrayList<String>>();
		mode = new ArrayList<ArrayList<String>>();
		oldMode = new ArrayList<String>();
	}
        public static String now() {
            String DATE_FORMAT_NOW = "yyyy-MM-dd_HH_mm_ss";
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());
	}
	
	public void newK(int k) {
		
		this.k = k;
		
	}
	
	public void makeIRTable(boolean hasSeparatedAtt, int m, int att_target) {
		//System.out.println("Calculating IR values...");
		//load dataset into memory begins
		if (hasSeparatedAtt) {
			///*
			System.out.println("children.length (no. of attributes) = " + numOfAttribute);
			try { //load all attribute values
				//ArrayList<ArrayList<String>> attribute = new ArrayList<ArrayList<String>>();
				int record = 0;
				FileInputStream fis = new FileInputStream(this.inFString+1+".csv"); 
				BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
				String line = null;
				while( (line = dataFile.readLine()) != null) record++;
				dataFile.close();
				
				//att = new char[numOfAttribute][record]; 
				att = new String[numOfAttribute][record]; 
				for (int i=0;i<numOfAttribute;i++) {
					//System.out.println("Reading attribute " + (i+1) );
					fis = new FileInputStream(this.inFString+(i+1)+".csv"); 
					dataFile = new BufferedReader(new InputStreamReader(fis));
					line = null;	//System.out.println(this.inFString+Integer.toString(i)+".csv is read.");
					//ArrayList<String> rowToAdd = new ArrayList<String>();
					int j = 0;
					while( (line = dataFile.readLine()) != null) { 
						SimpleTokenizer st = new SimpleTokenizer(line,",");
						while (st.hasMoreTokens()) //st.nextToken();
							//att[i][j] = st.nextToken().charAt(0);
							att[i][j] = st.nextToken();
							j++;
							//rowToAdd.add(st.nextToken());		
					}
					//attribute.add(rowToAdd);
					dataFile.close();
				}
				System.out.println("load all attribute values done!");
			} catch (Exception e) {
				
			}//*/
		}
		else {
                    if ( !createRTableOnly ) {
			try { //load all attribute values
				int record = 0;
				//System.out.println("originalFString = " + originalFString);
				FileInputStream fis = new FileInputStream(this.originalFString); 
				BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
				String line = null; String tmp = null;
				while( (line = dataFile.readLine()) != null) {
					if (record == 0) {
						SimpleTokenizer st = new SimpleTokenizer(line,",");
						while (st.hasMoreTokens()) {
							numOfAttribute++; st.nextToken();
							//System.out.println(numOfAttribute);
						}
						numOfAttribute--;
					}
					record++;
				}
				record--;
				dataFile.close();
				System.out.println("(no. of attributes) = " + numOfAttribute);
				//att = new char[numOfAttribute][record];
				att = new String[numOfAttribute][record];
				fis = new FileInputStream(this.originalFString); 
				dataFile = new BufferedReader(new InputStreamReader(fis));
				dataFile.readLine();
				for (int i=0;i<record;i++) {
					line = null;
					int j = 0;
					line = dataFile.readLine();
					//System.out.println("line = " + line);
					if ( line  != null) {
						//System.out.println("j = " + j );
						SimpleTokenizer st = new SimpleTokenizer(line,",");
						st.nextToken();
						while (st.hasMoreTokens()) {
							//att[j][i] = st.nextToken().charAt(0);
							att[j][i] = st.nextToken();
							//System.out.println("att[j][i] = " + att[j][i]);
							j++;
						}
					}
				}
				dataFile.close();
				System.out.println("load all attribute values done!");				
			} catch (Exception e) {
				System.out.println("load all attribute values err: " + e);
			}
                    }
		}
		//distincyVal = new ArrayList<HashSet<String>>();
		//distinct values of each attributes
		distincyVal = new ArrayList<ArrayList<String>>();
		/*for (int indexAtt=0;indexAtt<att.length;indexAtt++) {
			HashSet<String> rowToAdd = new HashSet<String>();
			for (int indexRow=0;indexRow<att[indexAtt].length;indexRow++) {
				rowToAdd.add(Character.toString(att[indexAtt][indexRow]));
			}
			ArrayList<String> r1 = new ArrayList<String>(rowToAdd);
			distincyVal.add(r1);
		}*/
		//count distinct values of each attributes
		//ArrayList<ArrayList<String>> distinctVal = new ArrayList<ArrayList<String>>();
		distinctValCnt = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < att.length; i ++) {
			//List<String> temp = new ArrayList<String>();
			//List<Character> temp1 = new ArrayList<Character>();
			List<String> temp1 = new ArrayList<String>();
			for (int j = 0; j < att[i].length ; j ++) {
				//temp.add(Character.toString(att[i][j]));
				temp1.add(att[i][j]);
			}
			//Collections.sort(temp);
			//System.out.println("temp1 = " + temp1);
			Collections.sort(temp1); //System.out.println("Sort: i = " + i);
			ArrayList<String> rowToAdd = new ArrayList<String>();
			ArrayList<Integer> rowToAddCnt = new ArrayList<Integer>();
			int cnt = 0;
			for (int j = 0 ; j < att[i].length ; j++) {
				if (j==0) { //1st value of the attribute
					//rowToAdd.add(Character.toString(temp1.get(j)));
					rowToAdd.add(temp1.get(j));
					cnt++;
				} else if (j == att[i].length-1) { //last value of the attribute
					if (temp1.get(j).equals(rowToAdd.get(rowToAdd.size()-1))) {
						cnt++;
						rowToAddCnt.add(cnt);
					} else {
						rowToAddCnt.add(cnt);
						rowToAdd.add(temp1.get(j));
						cnt = 1;
						rowToAddCnt.add(cnt);
					}
				} else {
					//if ( Character.toString(temp1.get(j)).equals(rowToAdd.get(rowToAdd.size()-1)))
					if ( temp1.get(j).equals(rowToAdd.get(rowToAdd.size()-1)))
						cnt++;
					else {
						rowToAddCnt.add(cnt);
						//rowToAdd.add(Character.toString(temp1.get(j)));
						rowToAdd.add(temp1.get(j));
						cnt = 1;
					}
				}
			}
			//distinctVal.add(rowToAdd);
			distincyVal.add(rowToAdd);
			distinctValCnt.add(rowToAddCnt);
		}
		System.out.println("count distinct values of each attribute done!");
		//System.out.println("Load dataset into memory done!");
		//load dataset into memory ends
		
		//calculate all IR for all attributes
		attIR = new double[att.length][att.length];
		for ( int i = 0 ; i < att.length ; i++) {
			if ( m == 0 ) {
				System.out.println("calculating all IR for attribute " + (i+1) );
				for ( int j = 0 ; j < att.length-i-1 ; j++) {
					//System.out.println("calculating IR for attribute (" + (i+1) +","+(j+i+2) + ")");
					attIR[i][j+i+1] = calIR(Integer.toString(i+1), j+i+1+1);
				}
				for (int j=0;j<i;j++) {
					attIR[i][j] = attIR[j][i];
				}
			} else {
				if( i == att_target-1 ) {
					System.out.println("calculating all IR for attribute " + (i+1) );
					for ( int j = 0 ; j < att.length ; j++) {
						if (i != j) {
							attIR[i][j] = calIR(Integer.toString(i+1), j+1);
							System.out.println(attIR[i][j]);
						}
					}
				}
			}
		}
		/*
		for (int i = 0;i<attIR.length;i++) {
			for (int j=0;j<attIR[i].length;j++) {
				System.out.print(attIR[i][j] + " ");
			}
			System.out.println("");
		}*/
		//now all IR for all attributes are calculated
		//we can use these to process the assignment and the mode computation in all iterations
		
		att = null;
		
		//store the IR table for future use
		try {
                    System.out.print("Writing Result...");
                        String outname = "IR_Table.csv";
                        if ( this.createRTableOnly)
                            outname = rTableName;
			FileWriter writer = new FileWriter(this.outFString+File.separator+outname);
			BufferedWriter out = new BufferedWriter(writer);
			for ( int i = 0 ; i < attIR.length ; i ++) {
				for ( int j = 0 ; j < attIR[i].length ; j++) {
					if ( j == 0)
						out.write(Double.toString(attIR[i][j]));
					else
						out.write("," + Double.toString(attIR[i][j]));
				}
				out.newLine();
			}
			out.close();
                        System.out.println("Done!");
		System.out.println("File saved in " + this.outFString + File.separator + outname);
			if ( m != 0 ) {
				System.out.println("IR table is stored.");
				System.exit(0);
			}
		} catch (Exception e) {
			System.out.println("Store R Table Err: " + e);
		}
	}
	
	public void loadIRTable(String inVal) {
		try { //load all attribute values
				int record = 0;
				//System.out.println("originalFString = " + originalFString);
				FileInputStream fis = new FileInputStream(this.originalFString); 
				BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
				String line = null; String tmp = null;
				while( (line = dataFile.readLine()) != null) {
					if (record == 0) {
						SimpleTokenizer st = new SimpleTokenizer(line,",");
						while (st.hasMoreTokens()) {
							//numOfAttribute++;
                                                        attName.put(Integer.toString(numOfAttribute++),st.nextToken());
							//System.out.println(numOfAttribute);
						}
						numOfAttribute--;
					}
					record++;
				}
				record--;
				dataFile.close();
		} catch (Exception e) {
			System.out.println("Err: loadIRTable()");
		}
		System.out.print("Loading IR Table ... ");
		attIR = new double[numOfAttribute][numOfAttribute];
		System.out.print(inVal + " ... ");
		try {
			FileInputStream fis = new FileInputStream(inVal); 
			BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			int i=0, j=0;
			while( (line = dataFile.readLine()) != null) { 
				SimpleTokenizer st = new SimpleTokenizer(line,",");
				while (st.hasMoreTokens()) 
					attIR[i][j++] = Double.parseDouble(st.nextToken());
				i++;
				j=0;
			}
			dataFile.close();
		} catch (Exception e) {
			
		}
		
		//transpose the attIR
		for (int i = 0;i<numOfAttribute;i++)
			for (int j=0;j<numOfAttribute;j++) {
				double temp = attIR[i][j];
				attIR[i][j] = attIR[j][i];
				attIR[j][i] = temp;
			}
		/*print
		for (int i = 0;i<numOfAttribute;i++) {
			for (int j=0;j<numOfAttribute;j++) {
				System.out.print(attIR[i][j]+",");
			}
			System.out.println();
		}	
		*/
		System.out.print("Done!");
	}
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
		/************ Edit datasetPath for the data file path*******************/
		
		String datasetPath ="DiscretizedAttribute_5bins(EqualFrequency)\\2009-09-29_21_36_46";		
		String IRTablePath="";
		//IRTablePath = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\AttributeClustering\\completeIR_Table\\IR_Table.csv";
		
		/*small dataset
		String datasetPath ="test"; 
		String IRTablePath = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\AttributeClustering\\test\\IR_Table.csv";
		small dataset*/
		String originalFString = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\DiscreteAttribute\\" + datasetPath + "\\discretizedMotifTable.csv"; 
		String inFString = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\Discretization\\" + datasetPath + "\\";
		String outFString = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\AttributeClustering\\";
		
		
		int k = 50;
		int iteration = 100;
		int m = 0 ; // flag for calculating IR for a particular attribute
		int att_target = 0;
		int numTrial = 5;
		//read args
		if (args.length > 0 ) { //Memory Mode
			//System.out.println(args[0]);
			//System.out.println(args[1]);
			//System.out.println(args[2]);
			originalFString = args[0];
			inFString = ""; // This path stores each attribute as a file while in this mode all attributes are in memory
			outFString = args[1];
			k = Integer.parseInt(args[2].substring(0,args[2].indexOf("-")));
			numTrial = Integer.parseInt(args[2].substring(args[2].indexOf(",")+1));
			iteration = Integer.parseInt(args[3]);
			if (args.length == 5) {
				IRTablePath = args[4]; //This file stores all IR values in a file while in this mode all IR values are in memory
			}
			if (args.length == 6) {
				m = Integer.parseInt(args[4]);
				att_target = Integer.parseInt(args[5]);
			}
		}
		AttributeClustering ac = new AttributeClustering(originalFString,inFString, outFString, k, iteration, IRTablePath, m, att_target);	
		ArrayList<Double> sumSMR;
		int p = Integer.parseInt(args[2].substring(args[2].indexOf("-")+1,args[2].indexOf(",")));
		
		
		
		ArrayList<ArrayList<Double>> smrList = new ArrayList<ArrayList<Double>>();
		for ( int trial = 1; trial <= numTrial ; trial++) {
			sumSMR = new ArrayList<Double>();
			//ac = new AttributeClustering(originalFString,inFString, tmpOutString, k, iteration, IRTablePath, m, att_target);	
			String tmpOutString = ac.outFString;
			ac.outFString = ac.outFString + File.separator + trial + File.separator;
			(new File(ac.outFString)).mkdir();
			for ( int i=k; i <= p ; i++) {
				ac.resetParam();
				//if (i > k)
					ac.newK(i);
				ac.initialization();
				while (!ac.termination()) {
					ac.assignment();
					ac.computeMode();
				}			
				sumSMR.add(ac.writeResult(i));
			}
			ac.outFString = tmpOutString;
			System.out.println("k, Sum(SMR)");
			int maxK = 0;
			double maxR = 0.0;
			ArrayList<Double> rowToAdd = new ArrayList<Double>();
			for ( int i=0; i < sumSMR.size() ; i++) {
				if (i==0) {
					maxK = i+k;
					maxR = sumSMR.get(i);
				} else if(sumSMR.get(i) > maxR) {
					maxK = i+k;
					maxR = sumSMR.get(i);
				}
				System.out.println( (i+k) +","+sumSMR.get(i));
				rowToAdd.add(sumSMR.get(i));
			}
			smrList.add(rowToAdd);
			System.out.println("Max(k) = " + maxK + "," + maxR);
		}
		
		System.out.print("trial | k");
		for ( int i=k; i <= p ; i++) System.out.print(","+i);
		System.out.println();
		for (int i = 0; i<smrList.size();i++) {
			System.out.print((i+1));
			for (int j = 0; j<smrList.get(i).size();j++) {
				System.out.print(","+smrList.get(i).get(j));
			}
			System.out.println();
		}
		ac.writeRunReport(smrList, k, p, ac.outFString);
	}
	
	
	public void initialization() {
		Integer[] list = new Integer[numOfAttribute];
		System.out.println("\nnumOfAttribute: " + numOfAttribute);
		for (int i = 0; i < numOfAttribute;i++)
			list[i] = i+1;
		randomList = pickRandomNumbers(Arrays.asList(list),numOfAttribute);
		//Collections.sort(randomList);
        //showRandomNumbers(randomList);
		/*randomList.clear();  //testing
		for ( int i = 1; i<=10 ; i++) {
			if ( i == 2)
				randomList.add(6);
			else if ( i==6)
				randomList.add(2);
			else
				randomList.add(i);
		}*/ //testing
		System.out.println("randomList = " + randomList);
		for (int i = 0; i < k ; i++) {
			ArrayList<String> rowToAdd = new ArrayList<String>();
			rowToAdd.add(Integer.toString(randomList.get(i)));
			/*if (i==0)
				rowToAdd.add("1"); //testing
			else 
				rowToAdd.add("6"); //testing */
			rowToAdd.add("0"); //assume MR = 0
			mode.add(rowToAdd);
			ArrayList<String> c = new ArrayList<String>();
			c.add(Integer.toString(randomList.get(i)));
			/*if (i==0)
				c.add("1");
			else
				c.add("6");*/
			cluster.add(c);
			oldMode.add("");
		}
		//System.out.println(mode);;
		//System.out.println("The last cluster's attribute index = " + mode.get(mode.size()-1).get(0));
	}
	
	public void assignment() {
		System.out.print("assignment() ... ");
		/*
		double[][] iR = new double[mode.size()][2]; 
		for (int i = 0; i < mode.size() ; i++) {
			iR[i][0] = i;
			iR[i][1] = calIR(mode.get(i).get(0),randomList.get(k+this.currentIteration));
		}
		//iR[iR.length-1][1] = 9.99; //testing
		Arrays.sort(iR, new ArrayColumnComparator(1));
		//System.out.println("max R attribute index = " + randomList.get((int)iR[iR.length-1][0])); //max R index
		//System.out.println("max R value = " + iR[iR.length-1][1]); //max R value
		cluster.get((int)iR[iR.length-1][0]).add(randomList.get(k+this.currentIteration).toString());
		//System.out.println(cluster);
		latestAssign = (int)iR[iR.length-1][0];
		*/
		for (int attr = k ; attr < randomList.size(); attr++) {
			//System.out.println("Attribute "+attr);
			double[][] iR = new double[mode.size()][2]; 
			for (int i = 0; i < mode.size() ; i++) {
				iR[i][0] = i;
				//iR[i][1] = calIR(mode.get(i).get(0),randomList.get(attr));
				iR[i][1] = attIR[Integer.parseInt(mode.get(i).get(0))-1][randomList.get(attr)-1];
				//System.out.println(randomList.get(attr)+","+mode.get(i).get(0)+": "+iR[i][1]);
			}
			Arrays.sort(iR, new ArrayColumnComparator(1));
			//System.out.println("current assigning attribute = " + randomList.get(attr));
			//System.out.println("max R cluster attribute index = " + randomList.get((int)iR[iR.length-1][0])); //max R index
			//System.out.println("max R value = " + iR[iR.length-1][1]); //max R value
			cluster.get((int)iR[iR.length-1][0]).add(randomList.get(attr).toString());
		}
		/*
		int check = 0;
		for (int i = 0; i<cluster.size();i++)
			check = check + cluster.get(i).size();
		System.out.println(check);
		*/
		System.out.println(" Done!");
	}
	//int count = 0;
	private double calIR(String candidate1, int candidate2)  {
		double retVal = 0.0;
		int i = Integer.parseInt(candidate1)-1;
		int j = candidate2-1;
		//System.out.println(count++);
		//read i and j attribute into memory
		/*
		ArrayList<String> attI = new ArrayList<String>();
		for (int index=0;index<att[i].length;index++)
			attI.add(Character.toString(att[i][index]));
		ArrayList<String> attJ = new ArrayList<String>();
		for (int index=0;index<att[j].length;index++)
			attJ.add(Character.toString(att[j][index]));
		*/
		/*ArrayList<String> attGp = new ArrayList<String>();
		for (int index=0;index<att[i].length;index++)
			attGp.add(Character.toString(att[i][index])+","+Character.toString(att[j][index]));
		Collections.sort(attGp);*/
		String[][] attrGp = new String[att[i].length][2];
		for (int index=0;index<att[i].length;index++) {
			//attrGp[index][0] = Character.toString(att[i][index]);
			//attrGp[index][1] = Character.toString(att[j][index]);
			attrGp[index][0] = att[i][index];
			attrGp[index][1] = att[j][index];
		}
		Arrays.sort(attrGp, new ArrayColumnComparator1(0) );
		
		/*old method to load attribute every time
		try {
			FileInputStream fis = new FileInputStream(this.inFString+Integer.toString(i)+".csv"); 
			BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
			String line = null;	//System.out.println(this.inFString+Integer.toString(i)+".csv is read.");
			while( (line = dataFile.readLine()) != null) { 
				SimpleTokenizer st = new SimpleTokenizer(line,",");
				while (st.hasMoreTokens())
					attI.add(st.nextToken());						
			}
			dataFile.close();
			
			fis = new FileInputStream(this.inFString+Integer.toString(j)+".csv"); 
			dataFile = new BufferedReader(new InputStreamReader(fis));
			line = null;	
			while( (line = dataFile.readLine()) != null) { 
				SimpleTokenizer st = new SimpleTokenizer(line,",");
				while (st.hasMoreTokens())
					attJ.add(st.nextToken());						
			}
			dataFile.close();
			*/
		
			

			//Interdependence Redundancy Computation
			double mutualInfo = 0.0;
			double entropy = 0.0;
			double p = 0.0;
			double pp = 0.0;
			//System.out.println("attI.size() = "+attI.size());
			//System.out.println("attJ.size() = "+attJ.size());
			
			

			//double m = attI.size() * 1.0;
			//double m = attGp.size() * 1.0;
			double m = attrGp.length * 1.0;
			//HashSet<String> distincyValI = (new HashSet<String>(attI));
			//HashSet<String> distincyValJ = (new HashSet<String>(attJ));
			//Object[] theDistincyValI = distincyValI.toArray();
			//Object[] theDistincyValJ = distincyValJ.toArray();
			
			//Object[] theDistincyValI = distincyVal.get(i).toArray();
			//Object[] theDistincyValJ = distincyVal.get(j).toArray();
			
			//for (int theI=0;theI<distincyValI.size();theI++)
			//	for (int theJ=0;theJ<distincyValJ.size();theJ++) {
			for (int theI=0;theI<distincyVal.get(i).size();theI++)
				for (int theJ=0;theJ<distincyVal.get(j).size();theJ++) {
					int cntP=0;
					int cntPi=0;
					int cntPj=0;
					cntPi = distinctValCnt.get(i).get(theI);
					cntPj = distinctValCnt.get(j).get(theJ);
					for ( int tuple = 0; tuple < attrGp.length ; tuple ++) {
					//for ( int tuple = 0; tuple < attGp.size() ; tuple ++) {
					//for ( int tuple = 0; tuple < attI.size() ; tuple ++) {
					//for ( int tuple = 0; tuple < 0 ; tuple ++) {
						/*
						String iVal = attI.get(tuple);
						String jVal = attJ.get(tuple);
						String diVal = distincyVal.get(i).get(theI);
						String djVal = distincyVal.get(j).get(theJ);
						*/
						//if ( attI.get(tuple).equals(theDistincyValI[theI])) {
						//if ( attI.get(tuple).equals(distincyVal.get(i).get(theI))) {
						
						/*if ( iVal.equals(diVal) ) {
						//new method to access the attribute in the memory
						//if ( Character.toString(att[i][tuple]).equals(theDistincyValI[theI])) {
							cntPi++;
						}
						//if ( attJ.get(tuple).equals(theDistincyValJ[theJ])) {
						//if ( attJ.get(tuple).equals(distincyVal.get(j).get(theJ))) {
						if ( jVal.equals(djVal) ) {
						//if ( Character.toString(att[j][tuple]).equals(theDistincyValJ[theJ])) {	
							cntPj++;
						}*/
						
						//if ( attI.get(tuple).equals(theDistincyValI[theI]) 
						//		&& attJ.get(tuple).equals(theDistincyValJ[theJ])) {
						//if ( attI.get(tuple).equals(distincyVal.get(i).get(theI)) 
						//		&& attJ.get(tuple).equals(distincyVal.get(j).get(theJ))) {
						
						/*
						if ( iVal.equals(diVal) 
								&& jVal.equals(djVal)) {
						//if ( Character.toString(att[i][tuple]).equals(theDistincyValI[theI]) 
						//		&& Character.toString(att[j][tuple]).equals(theDistincyValJ[theJ])) {
							cntP++;
						}
						*/
						
						String iVal = attrGp[tuple][0];
						//String iVal = attGp.get(tuple).split(",")[0];
						String diVal = distincyVal.get(i).get(theI);
						if (iVal.equals(diVal)) {
							String jVal = attrGp[tuple][1];
							String djVal = distincyVal.get(j).get(theJ);
							if (jVal.equals(djVal)) {
								cntP++;
							}
						} else {
							if (cntP > 0) break;
						}
					}
					//System.out.println("distincyVal.get(i).get(theI) = " + distincyVal.get(i).get(theI) + ", distincyVal.get(j).get(theJ) = " + distincyVal.get(j).get(theJ));
					//System.out.println("cntPi = " + cntPi + ", cntPj = " + cntPj + ", cntP = " + cntP);
					p = cntP/m;
					pp = (cntPi/m) * (cntPj/m);
					//System.out.println("p = " + p + ", pp = " + pp);
					double ppxi = p*(Math.log(p/pp)/Math.log(2));
					//System.out.println("I = " + ppxi);
					if (!Double.isNaN(ppxi))
						mutualInfo += ppxi;
					ppxi = p*(Math.log(p)/Math.log(2));
					//System.out.println("H = " + ppxi);
					if (!Double.isNaN(ppxi)) 
						entropy += ppxi;
				}
			
			entropy = entropy * -1.0;
			double ir = mutualInfo/entropy;
			//System.out.println("ir = " + ir);
			if (!Double.isNaN(ir) && !Double.isInfinite(ir))
				retVal = ir;
		/*} catch (Exception e) {
			
		}*/
		return retVal;
	}
	
	public void computeMode() {
		System.out.print("computeMode() ... ");
		/*
		double[][] mr = new double[cluster.get(latestAssign).size()][2]; 
		//System.out.println("cluster.get(latestAssign).size() = " + cluster.get(latestAssign).size());
		for ( int i = 0 ; i < cluster.get(latestAssign).size();i++) {
			//System.out.println("i = " + i);
			for (int j=0;j<cluster.get(latestAssign).size()-1;j++) {
				mr[i][0] = Double.parseDouble(cluster.get(latestAssign).get(i));
				mr[i][1] = mr[i][1] + calIR(cluster.get(latestAssign).get(i),Integer.parseInt(cluster.get(latestAssign).get(j)));
				//System.out.println("i,j = " + i + "," + j);
			}
		}
		//mr[1][1] = 9.99;
		Arrays.sort(mr, new ArrayColumnComparator(1));
		//System.out.println("cluster.get(latestAssign).size() = " + cluster.get(latestAssign).size());
		//for (int i = 0; i<cluster.get(latestAssign).size();i++)
		//	System.out.println("Attribute: " + (int)mr[i][0]);
		//System.out.println("max mr attribute index = " + (int)mr[cluster.get(latestAssign).size()-1][0]);
		//System.out.println("max mr value = " + mr[cluster.get(latestAssign).size()-1][1]);
		ArrayList<String> rowToAdd = new ArrayList<String>();
		rowToAdd.add(Integer.toString((int)(mr[cluster.get(latestAssign).size()-1][0])));
		rowToAdd.add(Double.toString(mr[cluster.get(latestAssign).size()-1][1]));
		mode.set(latestAssign, rowToAdd); 
		*/
		for ( int clusterIndex = 0;clusterIndex<cluster.size();clusterIndex++) {
			//System.out.print("clusterIndex = " + clusterIndex); System.out.println(", cluster.get(clusterIndex).size() = " + cluster.get(clusterIndex).size());
			double[][] mr = new double[cluster.get(clusterIndex).size()][2]; 
			double[][] matrix = new double[cluster.get(clusterIndex).size()][cluster.get(clusterIndex).size()];
			for ( int i = 0 ; i < cluster.get(clusterIndex).size();i++) {
				//System.out.println("i = " + i);
				if (cluster.get(clusterIndex).size() ==1 ) {
					mr[i][0] = Double.parseDouble(cluster.get(clusterIndex).get(i));
					mr[i][1] = 0.0;
				} else {
					mr[i][0] = Double.parseDouble(cluster.get(clusterIndex).get(i));
					for (int j=0;j<cluster.get(clusterIndex).size()-i-1;j++) {
						//System.out.println("(i,j) = ("+i+","+(j+i+1)+")");
						//matrix[i][j+i+1] = calIR(cluster.get(clusterIndex).get(i),Integer.parseInt(cluster.get(clusterIndex).get(j+i+1)));
						matrix[i][j+i+1] = attIR[Integer.parseInt(cluster.get(clusterIndex).get(i))-1][Integer.parseInt(cluster.get(clusterIndex).get(j+i+1))-1];
						//mr[i][1] = mr[i][1] + calIR(cluster.get(clusterIndex).get(i),Integer.parseInt(cluster.get(clusterIndex).get(j+i+1)));
					}
					for (int j=0;j<i;j++) {
						//System.out.println("(i,j) = ("+i+","+j+")");
						matrix[i][j] = matrix[j][i];
					}
					for (int j=0;j<cluster.get(clusterIndex).size();j++)
						mr[i][1] += matrix[i][j];
				
				}
			}
			//mr[1][1] = 9.99;
			Arrays.sort(mr, new ArrayColumnComparator(1));
			ArrayList<String> rowToAdd = new ArrayList<String>();
			rowToAdd.add(Integer.toString((int)(mr[cluster.get(clusterIndex).size()-1][0])));
			rowToAdd.add(Double.toString(mr[cluster.get(clusterIndex).size()-1][1]));
			mode.set(clusterIndex, rowToAdd);
			//re-arrange the ranking of the items inside the cluster according to the sorted MR values
			for ( int i = 0;i<cluster.get(clusterIndex).size();i++) {
				cluster.get(clusterIndex).set(i, Integer.toString((int)(mr[cluster.get(clusterIndex).size()-1-i][0])));
			}
		}
		System.out.println("Done!");
	}
	
	public boolean termination() {
		boolean retVal = false;
		
		System.out.println("currentIteration = " + currentIteration);

		//System.out.println("oldMode = " + oldMode.toString());
		//System.out.println("mode = " + mode.toString());
		int cnt=0;
		for (int i = 0 ; i < mode.size() ;i++) {
			if ( oldMode.get(i).equals(mode.get(i).get(0))) {
				cnt++;
			}
		}
		if (cnt==mode.size()) {
			System.out.println("All modes do not change. Terminated at currentIteration = "+currentIteration);
			retVal = true;
		}
			
		if (currentIteration == numOfIteration) {
			System.out.println("Max. iterations reached. Terminated at currentIteration = "+currentIteration);
			retVal = true;
		}
		
		for (int i = 0 ; i < mode.size() ;i++)
			oldMode.set(i, mode.get(i).get(0));
		
		if (this.currentIteration > 0 && !retVal ) {
			//reset cluster to remain the current mode only
			cluster.clear();
			randomList.clear();
			for (int i=0;i<k;i++) {
				ArrayList<String> c = new ArrayList<String>();
				c.add(mode.get(i).get(0));
				cluster.add(c);
				//System.out.println(Integer.parseInt(mode.get(i).get(0)));
				randomList.add(Integer.parseInt(mode.get(i).get(0)));
			}
			//reconstruct randomList
			Collections.sort(randomList);
			//System.out.println("randomList = "+randomList);
	
			List<Integer> l = new ArrayList<Integer>();		
			cnt = 0;
			for (int i = 1; i <= numOfAttribute;i++) {
				if (i != randomList.get(cnt)) {
					l.add(i);
				} else {
					if (cnt < k-1)	cnt++;
				}
			}
			//Collections.sort(l);System.out.println(l);
			l = pickRandomNumbers(l,numOfAttribute-k);
			//Collections.sort(randomList);System.out.println(randomList);
			randomList.addAll(l);
			//Collections.sort(randomList);System.out.println(randomList);
			
		}
		currentIteration++;
		return retVal;
	}

        public int runACA(int toNumCluster, int numTrial) {
            int retVal = 0; // Return Optimal k
            int p = toNumCluster;
            int originalK = k;
            ArrayList<Double> sumSMR;
            ArrayList<ArrayList<Double>> smrList = new ArrayList<ArrayList<Double>>();
            for ( int trial = 1; trial <= numTrial ; trial++) {
                    sumSMR = new ArrayList<Double>();
                    //ac = new AttributeClustering(originalFString,inFString, tmpOutString, k, iteration, IRTablePath, m, att_target);
                    String tmpOutString = outFString;
                    outFString = outFString + File.separator + "ACA_" + trial + File.separator;
                    (new File(outFString)).mkdir();
                    for ( int i=k; i <= p ; i++) {
                            resetParam();
                            //if (i > k)
                                    newK(i);
                            initialization();
                            while (!termination()) {
                                    assignment();
                                    computeMode();
                            }
                            sumSMR.add(writeResult(i));
                    }
                    outFString = tmpOutString;
                    System.out.println("k, Sum(SMR)");
                    int maxK = 0;
                    double maxR = 0.0;
                    ArrayList<Double> rowToAdd = new ArrayList<Double>();
                    for ( int i=0; i < sumSMR.size() ; i++) {
                            if (i==0) {
                                    maxK = i+k;
                                    maxR = sumSMR.get(i);
                            } else if(sumSMR.get(i) > maxR) {
                                    maxK = i+k;
                                    maxR = sumSMR.get(i);
                            }
                            System.out.println( (i+k) +","+sumSMR.get(i));
                            rowToAdd.add(sumSMR.get(i));
                    }
                    smrList.add(rowToAdd);
                    System.out.println("Max(k) = " + maxK + "," + maxR);
            }

            System.out.print("trial | k");
            for ( int i=k; i <= p ; i++) System.out.print(","+i);
            System.out.println();
            for (int i = 0; i<smrList.size();i++) {
                    System.out.print((i+1));
                    for (int j = 0; j<smrList.get(i).size();j++) {
                            System.out.print(","+smrList.get(i).get(j));
                    }
                    System.out.println();
            }
            retVal = writeRunReport(smrList, originalK, p, outFString);
            return retVal;
        }

	public double writeResult(int k) {
		double retVal = 0.0;
		System.out.print("Writing Result...");
		ArrayList<Integer> sortedAttribute = new ArrayList<Integer>();
		for (int i = 0;i<mode.size();i++) {
			sortedAttribute.add(Integer.parseInt(mode.get(i).get(0)));
		}
		Collections.sort(sortedAttribute);
		//System.out.println(sortedAttribute);
		
		try {
			FileWriter writer = new FileWriter(this.outFString+File.separator+"ACA_ModeTable_k=" + k +".csv");
			BufferedWriter out = new BufferedWriter(writer);
	    	//read and write attribute names
			FileInputStream fis = new FileInputStream(this.originalFString); 
			BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));		
			String line = null;	
			if( (line = dataFile.readLine()) != null) { //1st row  is the heading
				SimpleTokenizer st = new SimpleTokenizer(line,",");
				int cnt=0;
				int sortedAttributeIndex=0;
				while (st.hasMoreTokens() && sortedAttributeIndex!=sortedAttribute.size())
				{
					if (cnt == 0) 
						out.append(st.nextToken());
					else {
						if(cnt == sortedAttribute.get(sortedAttributeIndex)) {
                                                        String name = st.nextToken();
                                                        //attName.put(Integer.toString(cnt), name);
							out.append(","+name);
							//System.out.println(sortedAttribute.get(sortedAttributeIndex));
							sortedAttributeIndex++; 
						} else {
							String name = st.nextToken();
                                                        //attName.put(Integer.toString(cnt), name);
                                                }
					}
					cnt++;
				}
				out.newLine();
			}
                        
                        //System.out.println("attName: " + attName.entrySet());
			//read and write attribute values
			//int rowIndex = 0;
			while ( (line = dataFile.readLine()) != null) {
				//System.out.println("rowIndex: "+ ++rowIndex);
				SimpleTokenizer st = new SimpleTokenizer(line,",");
				out.write(st.nextToken()); //row name
				//read each attribute
				int cnt=1;
				int sortedAttributeIndex=0;
				while (st.hasMoreTokens() && sortedAttributeIndex!=sortedAttribute.size())
				{	
					if(cnt == sortedAttribute.get(sortedAttributeIndex)) {
						String val = st.nextToken();
						out.append(","+val);
						//System.out.println(sortedAttribute.get(sortedAttributeIndex));
						//System.out.print( val + ",");
						sortedAttributeIndex++;
					} else
						st.nextToken();
					cnt++;
				}
				out.newLine();
				//break;
			}
			out.close();
		} catch (Exception e) {
			System.out.println("AttributeClustering \\ writeResult \\ err " + e);
		}
		
		try {
			FileWriter writer = new FileWriter(this.outFString+File.separator+"AC_ItemsInClusters_k="+k+".csv");
			BufferedWriter out = new BufferedWriter(writer);
			//AC_ItemsInClusters.csv
			//clusters, items
			//out.append("Cluster,Mode,Item");
			out.append("Cluster,Mode,Significant_MR,Item,Sum(SMR),");
			double sumSMR = 0.0;
			for ( int col=0;col<mode.size();col ++) {
				sumSMR += Double.parseDouble(mode.get(col).get(1));
			}
			retVal = sumSMR;
			out.append(Double.toString(sumSMR));
			out.newLine();
			for ( int col=0;col<mode.size();col ++) {
				out.append(col+1 + ","); //cluster name
				out.append(attName.get(mode.get(col).get(0)) + ",");
				out.append(mode.get(col).get(1) + ",");
				//System.out.println("cluster.get(col).size() = " + cluster.get(col).size());
				for (int row = 0;row < cluster.get(col).size();row++) {
					if (row == 0)
						out.append(attName.get(cluster.get(col).get(row)));
					else
						out.append("_" + attName.get(cluster.get(col).get(row)));
				}
				out.newLine();
			} 
			out.close();
				
			
		} catch (Exception e) {
			
		}
		System.out.println("Done!");
		System.out.println("Files saved in " + this.outFString);
		return retVal;
	}


	public  int writeRunReport(ArrayList<ArrayList<Double>> smrList, int k, int p, String outPath) {
            int retVal = 0;
		try {
			FileWriter writer = new FileWriter(outPath+File.separator+"ACA_ExecutionReport.csv");
			BufferedWriter out = new BufferedWriter(writer);
                        int optK = k;
                        double optKVal = smrList.get(0).get(0);
                        for ( int i = 0; i <smrList.size() ; i++) {
                            for (int j = 0 ; j < smrList.get(i).size(); j++) {
                                if ( smrList.get(i).get(j) > optKVal ) {
                                    optK = j+k;
                                    optKVal = smrList.get(i).get(j);
                                }
                            }
                        }
                        retVal = optK;
                        System.out.println("smrList: " + smrList);
                        out.append("Optimal k" + "," + optK +"," + "Max Sum of Significant MR" + "," + optKVal);
                        out.newLine();
			out.append("trial | k");
			for ( int i=k; i <= p ; i++) out.append(","+i);
			out.newLine();
			for (int i = 0; i<smrList.size();i++) {
				out.append((i+1)+"");
				for (int j = 0; j<smrList.get(i).size();j++) {
					out.append(","+smrList.get(i).get(j));
				}
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			System.out.println("writeRunReport err " + e);
		}
            return retVal;
	}
	
	// picks a set of 'howmany' numbers from the list passed in. 
	public static List<Integer> pickRandomNumbers(List<Integer> numbers, int howMany) {
       if (howMany > numbers.size())
		  throw new IllegalArgumentException("trying to pick too many numbers from the list");
	   List<Integer>  pickList = new ArrayList<Integer>(numbers);
	   List<Integer> randomList = new ArrayList<Integer>();
		
        Random rnd = new Random();
        for (int turn=0; turn < howMany; turn++) {
     	int index = (int) (rnd.nextDouble()*pickList.size());
     	randomList.add(pickList.get(index));
     	pickList.remove(index);
        }
		/*randomList = new ArrayList<Integer>(); //test
		for ( int i = 1 ; i <= howMany ; i++) //test
			randomList.add(i); //test */
	   return randomList;
	} 
	
	private static void showRandomNumbers(List<Integer> randomList) {
		for (Integer randomNumber : randomList) {
			System.out.print(randomNumber  + " ");
		}
	}
}

