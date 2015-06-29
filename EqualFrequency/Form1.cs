using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;

namespace EqualFrequency
{
    public partial class Form1 : Form
    {
        private string nameOfFile;
        private FileInfo output1 = new FileInfo("Discrete Output.csv");
        private List<List<Double>> Attributes = new List<List<Double>>();
        private List<List<string>> Discrete = new List<List<string>>();

        public Form1(){InitializeComponent();}

        public List<List<Double>> GetData(FileInfo infile){
            StreamReader sr = new StreamReader(infile.ToString());
            string Line = null;
            string subline;
            List<List<double>> Table = new List<List<double>>();

            if (!infile.Exists)
                Console.WriteLine(@"Can't find Files! ");
            
            Line = sr.ReadLine();
            while ((Line = sr.ReadLine()) != null){
                Line = Line + ",";
                List<double> sample = new List<double>();
                while (Line.Contains(",")){
                    subline = Line.Substring(0, Line.IndexOf(","));
                    sample.Add(Double.Parse(subline));
                    Line = Line.Substring(Line.IndexOf(",") + 1, Line.Length - Line.IndexOf(",") - 1);    
                }//one line finish
                Table.Add(sample);    
            }
            //Transaction of Attributes
           /* for (int i = 1; i < Table[0].Count();i++){
                List<double> sampleT = new List<double>();
                for (int j = 0; j < Table.Count(); j++) {
                    sampleT.Add(Table[j][i]);
                }
                Attributes.Add(sampleT);
            }*/
            return Table;
        }

        public void Discretization(double alpha) {

            for (int i = 1; i < Attributes[0].Count(); i++){//for each attribute
                int bin;
                List<double> binValues = new List<double>();
                List<double> SortLevel = new List<double>();
                for (int j = 0; j < Attributes.Count(); j++)
                    SortLevel.Add(Attributes[j][i]);
                SortLevel.Sort();
                bin = (int)(Math.Sqrt(SortLevel.Count())/ alpha);
                
                for (int binCount = 1; binCount < bin+1; binCount++) {
                    binValues.Add(SortLevel[SortLevel.Count() * binCount / bin - 1]);
                }
                    
                for (int k = 0; k < Attributes.Count(); k++)//records num of attribute 
                {
                    for(int binC=0; binC<binValues.Count();binC++){
                        if (Attributes[k][i] <= binValues[binC])
                        {
                            Attributes[k][i] = binC+1;
                            break;
                        }
                    }    
                }    
            }    
        }
        
        
        
        private void textBox2_TextChanged(object sender, EventArgs e) { }
        private void button6_Click(object sender, EventArgs e) { Application.Exit(); }

        private void button1_Click(object sender, EventArgs e)
        {
            OpenFileDialog fileDialog = new OpenFileDialog();
            fileDialog.InitialDirectory = "";
            fileDialog.Filter = "csv files (*.csv)|*.csv";
            fileDialog.FilterIndex = 1;
            fileDialog.RestoreDirectory = true;
            if (fileDialog.ShowDialog() == DialogResult.OK)
            {
                String fileName = fileDialog.FileName;
                textBox1.Text = fileName;
                nameOfFile = fileDialog.FileName;
            }
            else
                MessageBox.Show("Please Select the Correct File!");

            FileInfo inFile = new FileInfo(nameOfFile);
            Attributes = GetData(inFile);
            MessageBox.Show("Data are Gotten!");
        }

        private void button3_Click(object sender, EventArgs e)
        {
            double a = Double.Parse(textBox2.Text);
            Discretization(a);
            
            StreamWriter sw = new StreamWriter(output1.ToString());
            sw.WriteLine("id");
            for (int t = 0; t < Attributes.Count(); t++){
                for (int count = 0; count < Attributes[t].Count(); count++)
                    sw.Write(Attributes[t][count] + ",");
                sw.WriteLine();
                sw.Flush();
            }
            sw.Close();

            MessageBox.Show("Discretization is finished!");
            System.Diagnostics.Process.Start(output1.FullName);
        }

       
    }
}
