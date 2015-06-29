/*==================================================================
Purpose: To Calculate the Interdependence Redundancy, R, Mutual Information, I, and
         Joint Entropy, H, between 2 continuous random variables.

Usage: 
java RCC <Dataset.csv> <output path> <alpha>


Date:   22 Mar 2010
Change: 1st Version.

Date:   
Change: 
		
Example to execute the program:
java RCC K:\PQ713a\Gene\Project\Prof.Wong\Discretization\Mixed-Mode_Data\Implementation\testDataset.csv K:\PQ713a\Gene\Project\Prof.Wong\Discretization\Mixed-Mode_Data\Implementation\ 3
        
==================================================================*/
package mixedmodeaca;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.util.HashSet;


public class RCC {

	/**
	 * @param args
	 */
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd_HH_mm_ss";
	int numOfAttribute = 0;
	String inFString, outFString;
	double[][] att; //to store all attribute values
	double[][] attIR; //to store all Interdependence Redanduncy Measure values of all attributes
	int alpha;
	
	public RCC(String inFString, String outFString, int alpha) {
	
		String tNow = now();		
		this.inFString = inFString;
		(new File(outFString+tNow)).mkdir(); 
		this.outFString = outFString+tNow;
		this.alpha = alpha;
		readDataset();
		attIR = new double[att.length][att.length];
	}

        public RCC(String inFString, int[] colID, String outFString, int alpha) {
            //String tNow = now();
            this.inFString = inFString;
            (new File(outFString)).mkdir();
            this.outFString = outFString;
            this.alpha = alpha;
            readDataset(colID);
            attIR = new double[att.length][att.length];
        }
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
		///************ Edit datasetPath for the data file path******************
		//String inFString = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\Discretization\\" + datasetPath + "\\";
		//String outFString = "K:\\PQ713a\\Gene\\Code\\Upsec\\data\\Postprocessing\\AttributeClustering\\";
		String inFString;
		String outFString;
		int alpha;

		//read args
		if (args.length > 0 ) { //Memory Mode
			//System.out.println(args[0]);
			//System.out.println(args[1]);
			//System.out.println(args[2]);
			inFString = args[0];
			outFString = args[1];
			alpha = Integer.parseInt(args[2]);
			
			RCC rcc = new RCC(inFString, outFString, alpha);
			rcc.calculate();
			rcc.writeResult("IR_Table.csv");
		} else {
			System.out.println("Usage: java RCC <Dataset.csv> <output path>");
		}		

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
                                ArrayList<ArrayList<Double>> sAtt = new ArrayList<ArrayList<Double>>();
                                //dataFile.readLine(); //ignore 1st row since it is a label
                                int cnt = 0;
                                while( (line = dataFile.readLine()) != null) {
                                    int col = 0;
                                    int selectedCol = 0;
                                    SimpleTokenizer st = new SimpleTokenizer(line,",");
                                    while (st.hasMoreTokens()) {
                                        //System.out.println("Debug: " + col);
                                       if (cnt == 0) {
                                            if (colSet.contains(col)) {
                                                ArrayList<Double> row = new ArrayList<Double>();
                                                sAtt.add(row);
                                                st.nextToken();
                                            } else
                                                st.nextToken();
                                            col++;
                                        } else {
                                            if (colSet.contains(col)) {
                                                sAtt.get(selectedCol++).add(Double.parseDouble(st.nextToken())); 
                                            } else
                                                st.nextToken();
                                            col++;
                                        }
                                    }
                                    cnt++;
                                }
                                numOfAttribute = sAtt.size();
				dataFile.close();
                                att = new double[numOfAttribute][cnt-1];
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

	public void readDataset() {
		try { //load all attribute values
				int record = 0;
				//System.out.println("inFString = " + inFString);
				FileInputStream fis = new FileInputStream(this.inFString); 
				BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
				String line = null; String tmp = null;
				while( (line = dataFile.readLine()) != null) {
					if (record == 0) {
						SimpleTokenizer st = new SimpleTokenizer(line,",");
						while (st.hasMoreTokens()) {
							numOfAttribute++; st.nextToken();
							//System.out.println(numOfAttribute);
						}
						numOfAttribute--; //since left most column is a label
					}
					record++;
				}
				record--; //since top most row is a label
				dataFile.close();
				//System.out.println("(no. of attributes) = " + numOfAttribute);
				System.out.println("Number of Continuous Attributes, |Xi'| = " + numOfAttribute);

				att = new double[numOfAttribute][record];
				fis = new FileInputStream(this.inFString); 
				dataFile = new BufferedReader(new InputStreamReader(fis));
				dataFile.readLine(); //ignore 1st row since it is a label
				for (int i=0;i<record;i++) {
					line = null;
					int j = 0;
					line = dataFile.readLine();
					//System.out.println("line = " + line);
					if ( line  != null) {
						//System.out.println("j = " + j );
						SimpleTokenizer st = new SimpleTokenizer(line,",");
						st.nextToken(); //ignore 1st column since it is a label
						while (st.hasMoreTokens()) {
							att[j][i] = Double.parseDouble(st.nextToken());
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
	
	public void calculate() {
		int s = att[0].length;
		int m = (int) Math.round(Math.sqrt(s / alpha));
		//m = 15; //Hardcode
		System.out.println("Alpha = " + alpha);
		System.out.println("Number of Samples, s = " + s);
		System.out.println("Number of Bins, m = " + m);
		for ( int i = 0 ; i < att.length ; i++) {
			System.out.println("calculating all IR for attribute " + (i+1) );
			for ( int j = 0 ; j < att.length-i-1 ; j++) {
				System.out.println("calculating IR for attribute (" + (i+1) +","+(j+i+2) + ")");
				double interval_1[] = new double[m]; //# of boundaries = # of bins + 1
				double interval_2[] = new double[m];
				double size_1 = range(att[i]) / m; //System.out.println("size_1 = " + size_1);
				double size_2 = range(att[j+i+1]) / m; //System.out.println("size_2 = " + size_2);
				for ( int k = 0 ; k < m; k++) {	 //interval size
					if ( k == 0 ) { // lower bound
						interval_1[k] = min(att[i]) + size_1; //System.out.println("min[x] = " + interval_1[k]);
						interval_2[k] = min(att[j+i+1]) + size_2; //System.out.println("min[y] = " + interval_2[k]);
					} else if ( k == m-1) { // uppper bound
						interval_1[k] = max(att[i]); //System.out.println("max[x] = " + interval_1[k]);
						interval_2[k] = max(att[j+i+1]); //System.out.println("max[y] = " + interval_2[k]);
					} else {
						interval_1[k] = interval_1[k-1] + size_1; //System.out.println("interval_1[" + (k) + "] = " + interval_1[k]);
						interval_2[k] = interval_2[k-1] + size_2; //System.out.println("interval_2[" + (k) + "] = " + interval_2[k]);
					}
				}
				int[][] count = new int[m][m];
				int sumX[] = new int[m];
				int sumY[] = new int[m];
				for ( int x = 0 ; x < m ;  x++) { //each interval of attribute 1 
					for ( int y = 0 ; y < m ;  y++) { //each interval of attribute 2
						for ( int k = 0 ; k < att[i].length; k++) { // loop thru all the tuples
							if (x==0 && y==0) {
								if (( Double.NEGATIVE_INFINITY < att[i][k] && att[i][k] <= interval_1[0]) && (Double.NEGATIVE_INFINITY < att[j+i+1][k] && att[j+i+1][k] <= interval_2[0]) )
									count[x][y] = count[x][y] + 1;
							} else if (x>0 && y==0) {
								if (( interval_1[x-1] < att[i][k] && att[i][k] <= interval_1[x]) && (Double.NEGATIVE_INFINITY < att[j+i+1][k] && att[j+i+1][k] <= interval_2[0]) )
									count[x][y] = count[x][y] + 1;
							} else if (x==0 & y>0) {
								if ((Double.NEGATIVE_INFINITY < att[i][k] && att[i][k] <= interval_1[0]) && (interval_2[y-1] < att[j+i+1][k] && att[j+i+1][k] <= interval_2[y]) )
									count[x][y] = count[x][y] + 1;
							} else {
								if (( interval_1[x-1] < att[i][k] && att[i][k] <= interval_1[x]) && (interval_2[y-1] < att[j+i+1][k] && att[j+i+1][k] <= interval_2[y]) )
									count[x][y] = count[x][y] + 1;
							}
						}
						sumX[x] += count[x][y];
						sumY[y] += count[x][y];
					}
				}
				
				/*//print the cell table to screen
				for ( int x = 0 ; x < m ;  x++) { //each interval of attribute 1 
					for ( int y = 0 ; y < m ;  y++) {	////each interval of attribute 2
						if (x==0 && y==0) {
							System.out.print("x\\y,");
							for (int iy = 0 ; iy < interval_2.length ; iy++)
								System.out.print(interval_2[iy]+",");
							System.out.println();
						}
						if (y==0)
							System.out.print(interval_1[x]+",");
						System.out.print(count[x][y] + ",");
					}
					System.out.println();
				} */

				double mutualInfo = 0.0;
				double entropy = 0.0;
				for ( int x = 0 ; x < m ;  x++) { 
					for ( int y = 0 ; y < m ;  y++) {
						if (count[x][y] != 0 && sumX[x] != 0 && sumX[y] !=0) {
							double pxy = (count[x][y]*1.0/s); //System.out.println("pxy = " + pxy);
							double px = sumX[x]*1.0/s; //System.out.println("px = " + px);
							double py = sumY[y]*1.0/s; //System.out.println("py = " + py);
							mutualInfo += (pxy) * (Math.log((pxy) / ( (px)*(py) ))/Math.log(2)) ;
							entropy += (pxy) * (Math.log(pxy)/Math.log(2));
						}
					}
				}
				//System.out.println("mutualInfo = " + mutualInfo);
				//System.out.println("entropy = " + (-entropy) );
				if ( entropy != 0.0 ) {
					attIR[i][j+i+1] = mutualInfo / (-entropy);
					//System.out.println("mutualInfo = " + mutualInfo);
					//System.out.println("entropy = " + entropy);
					//System.out.println("R = " + attIR[i][j+i+1]);
				}
			}
			for (int j=0;j<i;j++) {
				attIR[i][j] = attIR[j][i];
			}
		}
	}
	
	public void writeResult(String filename) {
		
		System.out.print("Writing Result...");
		try {
			FileWriter writer = new FileWriter(this.outFString+File.separator+filename);
			BufferedWriter out = new BufferedWriter(writer);
			for ( int i = 0 ; i < attIR.length ; i ++) {
				for ( int j = 0 ; j < attIR[i].length ; j++) {
					if ( j == 0) {
						DecimalFormat df = new DecimalFormat("#.####");
						//out.write(Double.toString(df.format(attIR[i][j])));
						out.write(df.format(attIR[i][j]));
					} else {
						DecimalFormat df = new DecimalFormat("#.####");
						//out.write("," + Double.toString(df.format(attIR[i][j])));
						out.write("," + df.format(attIR[i][j]));
					}
				}
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			
		}
		System.out.println("Done!");
		System.out.println("File saved in " + this.outFString + File.separator + filename);
		
	}
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());
	}
	
	public static double max(double[] array) {
      // Validates input
      if (array== null) {
          throw new IllegalArgumentException("The Array must not be null");
      } else if (array.length == 0) {
          throw new IllegalArgumentException("Array cannot be empty.");
      }
  
      // Finds and returns max
      double max = array[0];
      for (int j = 1; j < array.length; j++) {
          if (Double.isNaN(array[j])) {
              return Double.NaN;
          }
          if (array[j] > max) {
              max = array[j];
          }
      }
  
      return max;
	}
	public static double min(double[] array) {
      // Validates input
      if (array== null) {
          throw new IllegalArgumentException("The Array must not be null");
      } else if (array.length == 0) {
          throw new IllegalArgumentException("Array cannot be empty.");
      }
  
      // Finds and returns min
      double min = array[0];
      for (int j = 1; j < array.length; j++) {
          if (Double.isNaN(array[j])) {
              return Double.NaN;
          }
          if (array[j] < min) {
              min = array[j];
          }
      }
  
      return min;
	}
	public static double range(double[] array) {
      // Validates input
      if (array== null) {
          throw new IllegalArgumentException("The Array must not be null");
      } else if (array.length == 0) {
          throw new IllegalArgumentException("Array cannot be empty.");
      }
  
      // Finds and returns min
      double min = array[0];
	  double max = array[0];
      for (int j = 1; j < array.length; j++) {
          if (Double.isNaN(array[j])) {
              return Double.NaN;
          }
          if (array[j] < min) {
              min = array[j];
          }
		  if (array[j] > max) {
              max = array[j];
          }
      }
	  
      return (max - min);
	}
}

