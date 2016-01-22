/*package run;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.IOException;

import java.util.GregorianCalendar;

import data.EuclideanDistance;


import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;

import snapshot.StaticSnapshotWeight;



public class CreateWeight {
	public static void main(String[] args) throws IOException,ErrorFormatException , FileNotFoundException,ClassNotFoundException{
		// TODO Auto-generated method stub
	
		String train="";
		String test="";
		String config="";
	
		Float bPerc=0.1f;
		
	
		 train= "dataset/"+args[0];//"space_ga_testing_6XY.arff";
		 test= "dataset/"+args[1];//"space_ga_testing_6XY.arff";
		 config="dataset/"+args[2]+".ini";//"gasd.ini";
		
		 bPerc=new Float(args[3]);	
		 
	
	
	
		
		SnapshotSchema schema=null;
		

		{
		
		
			
		double b=Double.MAX_VALUE;
		schema=new SnapshotSchema(config);
			
		FileReader inputFileReader = null;
		BufferedReader inputStream;
		inputFileReader = new FileReader(train+".arff");
		
		inputStream   = new BufferedReader(inputFileReader);
		String inline;
		
		 do{
			inline=inputStream.readLine();
		}
		while(!inline.contains("@data"));
		
	
		GregorianCalendar timeBegin= new GregorianCalendar();
		java.util.Date currentTimeBegin = timeBegin.getTime();
		SnapshotData snap=null;
		try{
			snap=new SnapshotData(inputStream,schema);
		}
		catch (EOFException e) {
		}
		inputStream.close();
		inputFileReader.close();

		GregorianCalendar timeEnd = new GregorianCalendar();
		java.util.Date currentTimeEnd=  timeEnd.getTime();
		
		long computationTime=timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis();

		b=SnapshotWeigth.maxDist(snap, new EuclideanDistance(snap.getSpatialFeaturesSize()));
		SnapshotWeigth W=new StaticSnapshotWeight(bPerc*b,snap.size());
		W.updateSnapshotWeigth(snap, new EuclideanDistance(snap.getSpatialFeaturesSize()));
		String nomeFile="output/weight/"+args[1]+"_"+bPerc+".wmodel";
		W.salva(nomeFile);
		System.out.println("done in " + W.getTimeInMillis());
		
	}
	}



}*/
