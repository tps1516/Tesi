package run;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;








import rForecast.InvalidForecastParametersException;
import rForecast.ParametersChecker;
import rForecast.ParametersRForecastIndex;
import rForecast.RVar;
//import mbrModel.KNNModel;
import snapshot.DynamicSnapshotWeight;
import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import tree.TemporalWindow;
import tree.Tree;
import data.EuclideanDistance;
import data.feature.AutocorrelationI;
import data.feature.Feature;
import data.feature.ResubstitutionIndex;
import data.feature.ResubstitutionIndexOnGetisOrd;
import data.feature.GetisOrdIndex;

// completare deve prendere trainign and testing sets

public class TictTest {
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		PrintStream outputReport;
		String streamName="";//"soace_ga_training_5XY.arff";
		String config="";//"gasd.ini";
						
		float bperc=.1f;
		
		Integer splitNumber=20; //random split identifier
		float centroidPercentage=.2f;
		String sampling="quadtree";
		String testType="target";
		String isSpatial="GO";
		Integer TWSize = 0;
		String rPath ;
		String lagMax;
		String season;
		String exogen;
		String ic;
		String type;
		
		try{
			 streamName="dataset/"+args[0];//"soace_ga_training_5XY.arff";
			 config="dataset/"+args[1];//"gasd.ini";						
			
			 splitNumber=new Integer(args[2]); //random split identifier
			 bperc= new Float(args[3]);
			 centroidPercentage=new Float(args[4]);
			 TWSize=new Integer(args[5]);
			 rPath=String.valueOf(args[6]);
			 lagMax= String.valueOf(args[7]);
			 season= String.valueOf(args[8]);
			 exogen= String.valueOf(args[9]);
			 ic=String.valueOf(args[10]);
			 type=String.valueOf(args[11]);
		/*	 sampling=new String(args[5]);
			 testType=new String(args[6]);
			 isSpatial=new Boolean(args[7]);
			*/
		}
		catch(IndexOutOfBoundsException e){
			String report="TICT@KDDE.UNIBA.IT\n";
			report += "author = Annalisa Appice\n\n";
			report +="run jar by using the input parameters:\n";
			report +="streamfile configfile numSplits bandwidthPercentage samplingPercentage\n";
			report += "streamfileTRAIN.arff and streamfileTEST are stored in a local directory named \'dataset\'\n";
			report += "the first snapshot of both training and testing streams contain the network structure\n";
			report += "nummSplit=20 (default)\n";
			report += "bandwidthPercentage: real value in ]0,1]\n";
			report += "samplingPercentage: real value in ]0,1]\n";
			report += "Reports are created in the local directory named \'output\'\n";
					
			System.out.println(report);
			return;
			
		}
		
		 ArrayList<Object> rParameters  = new ArrayList<Object>();
		 ParametersRForecastIndex index = new ParametersRForecastIndex();
		 rParameters.add(index.rPath, rPath);
		 rParameters.add(index.lagMax, lagMax);
		 rParameters.add(index.season, season);
		 rParameters.add(index.exogen, exogen);
		 rParameters.add(index.ic, ic);
		 rParameters.add(index.type, type);
		 rParameters.add(index.TWSize, TWSize);
		 
		 try{
			 ParametersChecker checker = new ParametersChecker(TWSize-2);
			 checker.check(rParameters);
		 }catch (InvalidForecastParametersException e){
			 System.out.println(e.getMessage());
			 return;
		 }
		 
		String configStr="";
		AutocorrelationI autoCorrelation;
		
		if(isSpatial.equals("GO"))
			autoCorrelation=new ResubstitutionIndexOnGetisOrd();
		else
			autoCorrelation=new ResubstitutionIndex();

		
		String name=isSpatial+"_"+sampling+"_"+testType+"_"+centroidPercentage;
		outputReport = new PrintStream(new FileOutputStream("output/stream/report/"+args[0]+name+"TICT.report"));
		
		outputReport.println("TRAIN STREAM="+args[0]+"TRAIN");
		outputReport.println("TEST STREAM="+args[0]+"TEST");
		
		
		SnapshotSchema schemaTrain=null,schemaTest=null;
		
		try{
		
			Tree tree=null;
			double b=Double.MAX_VALUE;
			try {
				schemaTrain=new SnapshotSchema(config+ ".ini");

				configStr="";
				for(Feature g:schemaTrain.getTargetList())
					configStr+=g.getName()+";";
				schemaTest=new SnapshotSchema(config+ ".ini");
			
				
					
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			return;
		} catch (ErrorFormatException e) {
			System.out.println(e);
			e.printStackTrace();
			return;
		} 
		

		
		BufferedReader inputStreamTrain;
		BufferedReader inputStreamTest;
		SnapshotWeigth W=null;

		try {
			 
			 inputStreamTrain   = new BufferedReader(new FileReader(streamName+"TRAIN.arff"));
			 inputStreamTest  = new BufferedReader(new FileReader(streamName+"TEST.arff"));
			 
			 String out="";
				
			 try{
				String inlineTrain=inputStreamTrain.readLine();
				String inlineTest=inputStreamTest.readLine();
				
				if(!inlineTrain.equals("@") || !inlineTest.equals("@"))
						return;
				
				while(true){
					try{
					//il primo snapshot contiene solo il network
					
					SnapshotData snapTrain=new SnapshotData(inputStreamTrain,schemaTrain);
					
					SnapshotData snapTest=new SnapshotData(inputStreamTest,schemaTest,true);
					if(snapTrain.size()==0 || snapTest.size()==0)
						continue;
					
					if(W==null)
					{
						GregorianCalendar timeBegin= new GregorianCalendar();
						b=SnapshotWeigth.maxDist(snapTrain, new EuclideanDistance(snapTrain.getSpatialFeaturesSize()));
						W=new DynamicSnapshotWeight(b*bperc);
						W.updateSnapshotWeigth(snapTrain, new EuclideanDistance(snapTrain.getSpatialFeaturesSize()));
				
						GregorianCalendar timeEnd= new GregorianCalendar();
						outputReport.println("W computation time (milliseconds):"+(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis()));
						

						String nomeFile="output/stream/weight/"+args[0]+".wmodel";
						W.salva(nomeFile);
						
						snapTrain=new SnapshotData(inputStreamTrain,schemaTrain);
						snapTest=new SnapshotData(inputStreamTest,schemaTest,true);
						
						//tree=Tree.carica(("output/stream/model/"+args[0]+name+"269"+"TICT.model"));
					}
					
				/*	if(snapTrain.getIdSnapshot()<=269){
						System.out.println("Skipped "+snapTrain.getIdSnapshot());
						continue;
					}*/
					
					snapTrain.updateNull(W, schemaTrain);
					
					
					GregorianCalendar timeBegin= new GregorianCalendar();
					java.util.Date currentTimeBegin = timeBegin.getTime();
					
					
					
					if(!isSpatial.equals("GO") && schemaTrain.getTargetList().size()>1)
						snapTrain.scaledSchema(schemaTrain);
				
						
					
					if(autoCorrelation instanceof ResubstitutionIndexOnGetisOrd)
						snapTrain.updateGetisAndOrd(W,schemaTrain);
					else
						snapTrain.scaledSchema(schemaTrain);
					System.out.println("**** ID: "+snapTrain.getIdSnapshot());
					outputReport.println("**** ID: "+snapTrain.getIdSnapshot());
					;
					if(tree==null)// first snapshot in the stream
					{
						
						
						timeBegin= new GregorianCalendar();
					
						
						tree=new Tree(snapTrain, schemaTrain, W, autoCorrelation, splitNumber,centroidPercentage,sampling,testType, TWSize, rParameters);
						System.out.println(tree);
						
						//KNNModel knn=new KNNModel();
						//tree.populateKNNModel(knn);
						
						GregorianCalendar timeEnd= new GregorianCalendar();
						
						long time=timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis();
				
						tree.setComputationTime(time);
					
						
						outputReport.println(tree.symbolicClusterDescription(""));
						outputReport.println("Computation time(milliseconds)="+time);
						
						timeBegin= new GregorianCalendar();
						//String mse=knn.testKnn(snapTest, schemaTest, "output/stream/csvtict/"+args[0]+name+"_"+snapTest.getIdSnapshot()+".csv");
						timeEnd= new GregorianCalendar();
						outputReport.println("Interpolation time(milliseconds)="+(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis()));
						
						int pastLeaves=0,afterPruningLeaves=0,newLeaves=0;
						newLeaves=tree.countLeaves();
				
						
						//outputReport.println("Error statistics");
						//outputReport.println(configStr+"\n"+mse);
						
						outputReport.println("Leaves statisics");
						outputReport.println("number of leaves of the tree inherited from the past="+pastLeaves);
						outputReport.println("number of leaves of the tree after pruning="+afterPruningLeaves);
						outputReport.println("number of leaves in the new tree="+newLeaves);
						
						
						tree.salva("output/stream/model/"+args[0]+name+snapTrain.getIdSnapshot()+"TICT.model");
			
						
					}
					else{
						
						int pastLeaves=0,afterPruningLeaves=0,newLeaves=0;
						pastLeaves=tree.countLeaves();
						timeBegin= new GregorianCalendar();
						currentTimeBegin = timeBegin.getTime();
						tree.prune(snapTrain,schemaTrain,W,autoCorrelation);
						afterPruningLeaves=tree.countLeaves();
						tree.drift(snapTrain,schemaTrain,W,autoCorrelation,  splitNumber,centroidPercentage,sampling,testType, rParameters);
						System.out.println(tree);
						//KNNModel knn=new KNNModel();
						//tree.populateKNNModel(knn);
						
						
						
						GregorianCalendar timeEnd= new GregorianCalendar();
					
						tree.setComputationTime(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis());
						
						outputReport.println(tree.symbolicClusterDescription(""));
						outputReport.println("Computation time(milliseconds)="+(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis()));
						
						timeBegin= new GregorianCalendar();
						//String mse=knn.testKnn(snapTest, schemaTest, "output//stream//csvtict//"+args[0]+name+"_"+snapTest.getIdSnapshot()+".csv");
						timeEnd= new GregorianCalendar();
						outputReport.println("Interpolation time(milliseconds)="+(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis()));
						//outputReport.println("Error statistics");
						//outputReport.println(configStr+"\n"+mse);
						
						
						
						newLeaves=tree.countLeaves();
						outputReport.println("Leaves statisics");
						outputReport.println("number of leaves of the tree inherited from the past="+pastLeaves);
						outputReport.println("number of leaves of the tree after pruning="+afterPruningLeaves);
						outputReport.println("number of leaves in the new tree="+newLeaves);
						
						tree.salva("output/stream/model/"+args[0]+name+snapTrain.getIdSnapshot()+"TICT.model");
						
						
						// Dispersione
					/*	outputReport.println("Spatial dispersion");
						
						double trainingIntraDispersion=tree.computeSpatialIntraClusterDispersion(snapTrain);
						double testingIntraDispersion=tree.computeSpatialIntraClusterDispersion(snapTest);
					
						outputReport.println("training intra-cluster dispersion;"+trainingIntraDispersion);
						outputReport.println("testing intra-cluster dispersion;"+testingIntraDispersion);
						System.out.println("training intra-cluster dispersion;"+ trainingIntraDispersion);
						System.out.println("testing intra-cluster dispersion;"+testingIntraDispersion);
						
						
						double trainingInterDispersion=tree.computeSpatialInterClusterDispersion(snapTrain);
						double testingInterDispersion=tree.computeSpatialInterClusterDispersion(snapTest);
						outputReport.println("training inter-cluster dispersion;"+trainingInterDispersion);
						outputReport.println("testing inter-cluster dispersion;"+testingInterDispersion);
						System.out.println("training inter-cluster dispersion;"+trainingInterDispersion);
						System.out.println("testing inter-cluster dispersion;"+testingInterDispersion);
						
						*/
					
						
						
						
					}
	
				
				}
				catch(EOFException e){
					System.out.println("asda");
					break;
				}
			}
		}
		 catch(EOFException e){
			 System.out.println(e);
		 }
		 catch(IOException e){
			 System.out.println(e);
			 
		 }
		 finally{
	        	try {
					inputStreamTrain.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
	        	
		 }
	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}       
		}
		catch(Exception e){
			
			e.printStackTrace();
		}
		finally{
			outputReport.close();
			
		}
		
		
        
}

}
