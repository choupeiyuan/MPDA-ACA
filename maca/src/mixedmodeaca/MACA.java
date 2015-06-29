/*
 * MACA
 */

package mixedmodeaca;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Administrator
 */
class MACA {

    //static void makeAttTable(String inFString, int[] cColID, String selectedAttributePath, boolean isDouble) {
    static void makeAttTable(String inFString, int[] cColID, String selectedAttributePath) {
        HashSet<Integer> colSet = new HashSet<Integer>();
        for (int i = 0 ; i < cColID.length ; i++)
            colSet.add(cColID[i]);
        try { //load selected attribute values
                        //System.out.println("inFString = " + inFString);
                        //System.out.println("selectedAttributePath = " + selectedAttributePath);
                        System.out.print("Writing Selected Attribute Table ... ");
                        FileInputStream fis = new FileInputStream(inFString);
                        BufferedReader dataFile = new BufferedReader(new InputStreamReader(fis));
                        String line = null;
                        ArrayList<ArrayList<String>> sAtt = new ArrayList<ArrayList<String>>();
                        ArrayList<String> label = new ArrayList<String>();
                        int cnt = 0;
                        while( (line = dataFile.readLine()) != null) {
                            int col = 0;
                            int selectedCol = 0;
                            SimpleTokenizer st = new SimpleTokenizer(line,",");
                            while (st.hasMoreTokens()) {
                                //System.out.println("Debug: " + col);
                               if (cnt == 0) {
                                    if (colSet.contains(col)) {
                                        ArrayList<String> row = new ArrayList<String>();
                                        sAtt.add(row);
                                        //st.nextToken();
                                        label.add(st.nextToken());
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
                        dataFile.close();
                        FileWriter writer = new FileWriter(selectedAttributePath);
                        BufferedWriter out = new BufferedWriter(writer);
                        out.write("id");
                        for ( int i = 0 ; i < label.size() ; i++)
                            out.write("," + label.get(i)); 
                        out.newLine();
                        for ( int  rec = 0; rec < sAtt.get(0).size() ; rec++) {
                            for ( int attX = 0; attX < sAtt.size() ; attX++){
                               /* if (isDouble) {
                                    DecimalFormat df = new DecimalFormat("#.####");
                                    if (attX == 0) {
                                        out.write((rec+1) + "," + df.format(sAtt.get(attX).get(rec)));
                                    } else {
                                        out.write("," + df.format(sAtt.get(attX).get(rec)));
                                    }
                                } else {*/
                                   if (attX == 0) {
                                        out.write((rec+1) + "," + sAtt.get(attX).get(rec));
                                    } else {
                                        out.write("," + sAtt.get(attX).get(rec));
                                    }
                                //}
                            }
                            out.newLine();
                        }
                        out.close();
                        //for ( int attX = 0; attX < sAtt.size() ; attX++)
                          //  System.out.println(sAtt.get(attX));
                        System.out.println("Done!");
                } catch (Exception e) {
                        System.out.println("Writing Selected Attribute Table Err: " + e);
                }
    }

}
