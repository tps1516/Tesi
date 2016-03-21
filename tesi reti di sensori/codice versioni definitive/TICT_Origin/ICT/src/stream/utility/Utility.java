package stream.utility;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.Random;

import data.SensorPoint;

import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;

public class Utility {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException,ErrorFormatException{
		String fileName=args[0];
		
		String config="dataset/"+fileName+".ini";//"gasd.ini";
		
		int n=new Integer(args[1]);
		int m=new Integer(args[2]); //n/m train m-n/m test
		if(n>=m) throw new NumberFormatException("La percentule di split deve essere un valore compreso tra 0 e 100");
		String splitNumber=n+"_"+m;
		String trainStream="dataset/"+fileName+splitNumber+"TRAIN.arff";
		String testStream="dataset/"+fileName+splitNumber+"TEST.arff";
		
		FileOutputStream fileTrain = new FileOutputStream(trainStream);
      	PrintStream OutputTrain = new PrintStream(fileTrain);
      	
    	FileOutputStream fileTest = new FileOutputStream(testStream);
      	PrintStream OutputTest = new PrintStream(fileTest);
    
      	FileReader inputFileReader = null;
		BufferedReader inputStream;
		
	
		inputFileReader = new FileReader("dataset/"+fileName+".arff");
		inputStream   = new BufferedReader(inputFileReader);
		String out="";
		String outStream="";
		SnapshotSchema schema=null;
		schema=new SnapshotSchema(config);
			
	

		String inline=inputStream.readLine();
		if(!inline.equals("@"))
		return;
		while(true){
		
			 SnapshotData snap=new SnapshotData(inputStream,schema);
			 OutputTrain.println("@");
			 OutputTest.println("@");
			 Random rd=new Random();
			 GregorianCalendar time= new GregorianCalendar();
			 rd.setSeed(time.getTimeInMillis());
			 int trainingSize=0;
			 int testingSize=0;
			 for(SensorPoint s:snap){
				 int randomChoice=rd.nextInt(m);
				 if(randomChoice<=(n-1))
				 {
					 // add to train1
					 OutputTrain.println(s.toString().replace("null", "?"));
					 trainingSize++;
				 }
				 else{
					 OutputTest.println(s.toString().replace("null", "?"));
					 testingSize++;
				 }
			 }// end for
			 System.out.println(snap.size()+":"+trainingSize+":"+testingSize);	 
		}		
		
	}

}
