/*package run;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileLockInterruptionException;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeSet;

import mbrModel.KNNModel;
import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import snapshot.StaticSnapshotWeight;
import tree.SplitException;
import tree.Tree;
import data.EuclideanDistance;
import data.feature.AutocorrelationI;
import data.feature.Feature;
import data.feature.GetisOrdGeneralG;
import data.feature.MoranIndex;
import data.feature.ResubstitutionIndex;
import data.feature.ResubstitutionIndexOnGetisOrd;

public class IctTest {
	public static void main(String[] args) throws IOException,ErrorFormatException , FileNotFoundException,ClassNotFoundException{
		// TODO Auto-generated method stub
		String train="";
		String test="";
		String config="";
		int numSplits=20;	
		String heuristic="GO";
		Float bPerc=0.1f;
		Float centroidPercentage=0.2f;
		String sampling="quadtree";
		String testType="target";
		
		 // usa i prototipe dei coluster come modelli da associare ai centroidi 
		//Boolean clusterPrototypeModel=false; // campiona i sensori nel cluster e usa questi come modelli per l'interpolatore
		Boolean serializedModel=false; // recupera il modello serializzato nell'archivio
		Boolean serializedCendoidModel=false ;// recupera l'insieme dei centroidi dall'archivio (accoppiato con serializedModel=true)  
		
		Boolean serializedWeight=false; // recupera la matrice dei pesi serializzata nell'archivio
		
		Float serializedCentroidPercentage=0.1f;
	try{	
		 train="dataset/"+args[0];//"soace_ga_training_5XY.arff";
		 test= "dataset/"+args[1];//"space_ga_testing_6XY.arff";
		 config="dataset/"+args[2]+".ini";//"gasd.ini";
		 numSplits=new Integer(args[3]);	
		 heuristic=args[4];
		 bPerc=new Float(args[5]);	
		 centroidPercentage=new Float(args[6]);
		 sampling=new String(args[7]);//random - quadtree
		 testType= new String(args[8]); //spatial, target , mixed
		
		 serializedModel=new Boolean(args[9]);
		 serializedCendoidModel=new Boolean(args[10]);
		 serializedWeight=new Boolean(args[11]);
		 if(serializedCendoidModel)
			 serializedCentroidPercentage=new Float(args[12]);
	}
	catch(IndexOutOfBoundsException e){
		
		String report="ICT@KDDE.UNIBA.IT\n";
		report += "author = Annalisa Appice\n\n";
		report +="run jar by using the input parameters:\n";
		report +="trainingfile testingfile configfile numSplits heuristic bandwidthPercentage samplingPercentage samplingType splitVariableType\n";
		report += "training/testing data (arff) are stored in a local directory called \'dataset\'\n";
		report += "numSplit=20 (default)\n";
		report += "heuristic=GO=(Getis and Ord variand reduction), MoranAndVar=( global Moran I and data variance reduction) Var=(data variance reduction)\n";
		report += "bandwidthPercentage: real value in ]0,1]\n";
		report += "samplingPercentage: real value in ]0,1]\n";
		report += "samplingType= quadtree , random\n";
		report += "splitVariable= target, spatial, mixed\n";
		report += "clusterPrototypeModel= true (the prototype of a cluster is assigned to a centroid model), false (the sampled point is assigned to a centroid model)\n";
		report += "storedModel= true(the serialized model is stored), false(a new model is learned)\n";
		report += "storedCentroidModel= true(the serialized centrolid model is stored, it is considered only if storedModel=true), false(a new centroid model is learned)\n";
		report += "storedWeightMatrix=true(the serialized weight matrix is stored), false(a new model is learned)\n";
		report += "serializedSamplingPercentage: real value in ]0,1]  (optional if storedCentroidModel=true) , the cod of the serialzied file\n";
		report += "Reports are created in the local directory called \'output\'\n";
				
		System.out.println(report);
		return;
	}
		
		SnapshotSchema schema=null;
		
		AutocorrelationI autocorrelation=null;
			
		PrintStream outputReport = new PrintStream(new FileOutputStream("output/report/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".report"));
		outputReport.println("**************************");
		outputReport.println("train="+train);
		outputReport.println("test="+test);
		outputReport.println("split heuristic="+heuristic);
		outputReport.println("split variables="+testType);
		outputReport.println("sampling percentage="+centroidPercentage);
		outputReport.println("bandwidth percentage="+bPerc);
		outputReport.println("sampling type="+sampling);
	
	
		{
		
		
			if(heuristic.equals("GO")){
		
			//prototype=new SpatialPrototype();
			//autocorrelation=new GetisOrdGeneralG();
			//	autocorrelation=new MoranIndex();
			//autocorrelation= new SpatialResubstitutionIndex();
			autocorrelation=new ResubstitutionIndexOnGetisOrd();
			//autocorrelation=new SpatialResubstitutionIndex();
		
	
			}
			else
				if(heuristic.equals("MoranAndVar"))			
						autocorrelation=new MoranIndex();
				
			
			else{
			
				autocorrelation=new ResubstitutionIndex();
			}
		
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
	
		SnapshotData snap=null;
		try{
			snap=new SnapshotData(inputStream,schema);
		}
		catch (EOFException e) {
		}
		inputStream.close();
		inputFileReader.close();
		Tree tree;
		GregorianCalendar timeEnd = new GregorianCalendar();
	
		
		long computationTime=timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis();
		SnapshotWeigth W=null;
		if(!serializedModel)
		{
			
			
			System.out.println("Discovering ....");
			if(heuristic.equals("GO") || heuristic.equals("MoranAndVar")){
				
				String nomeFile="output/weight/"+args[1]+"_"+bPerc+".wmodel";
				if(!serializedWeight){
					b=SnapshotWeigth.maxDist(snap, new EuclideanDistance(snap.getSpatialFeaturesSize()));
					W=new StaticSnapshotWeight(bPerc*b,snap.size());
					W.updateSnapshotWeigth(snap, new EuclideanDistance(snap.getSpatialFeaturesSize()));
					W.salva(nomeFile);
				}
				else{
					W=StaticSnapshotWeight.carica(nomeFile);
					
				}
				computationTime+=W.getTimeInMillis();
				GregorianCalendar timeBeginInter = new GregorianCalendar();
				if(autocorrelation instanceof ResubstitutionIndexOnGetisOrd)
					snap.updateGetisAndOrd(W,schema);
				tree=new Tree(snap, schema, W, autocorrelation,  numSplits,centroidPercentage,sampling,testType);
				computationTime+=(new GregorianCalendar().getTimeInMillis()-timeBeginInter.getTimeInMillis());
			
				//tree=new Tree(snap, schema, W, new MoranIndex(),-1);
			}
			else{
				GregorianCalendar timeBeginInter = new GregorianCalendar();
				tree=new Tree(snap, schema,numSplits,centroidPercentage,sampling,testType);
				computationTime+=(new GregorianCalendar().getTimeInMillis()-timeBeginInter.getTimeInMillis());
			}
			//System.out.println(knn);
			timeEnd = new GregorianCalendar();		
			//currentTimeEnd = timeEnd.getTime();
			
			
			
			//outputReport.println("Computation time (milliseconds)="+(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis()));
			outputReport.println("Computation time (milliseconds)="+computationTime);
			System.out.println("Computation time (milliseconds)="+computationTime);
			tree.setComputationTime(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis());
			

			tree.salva("output/model/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".model");
			
		}
			else 
				try{
					System.out.println("Storing the serialized past model ...");
					String fileName="";
					if(serializedCendoidModel) //true = learn centroid from file
						fileName="output/model/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".model";
						else
						fileName=	"output/model/"+args[1]+"_"+sampling+"_"+serializedCentroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".model";
					tree=Tree.carica(fileName);
					if(!serializedCendoidModel){ // va rimpiazzato il modello dei centroidi
						{
						System.out.println("Sampling centroids with percentage "+centroidPercentage+ " ...");
						GregorianCalendar beginTime=new GregorianCalendar();
						tree.sampling(snap, schema, 0, snap.size()-1, centroidPercentage, sampling);
						outputReport.println("Computation time (milliseconds)="+computationTime);
						long time=(new GregorianCalendar().getTimeInMillis()-beginTime.getTimeInMillis())+tree.getComputationTime();
						System.out.println("Computation time (milliseconds)="+time);
						tree.setComputationTime(time);
						tree.salva("output/model/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".model");
						}
						
					}
					
				}
			catch (ClassNotFoundException e) {
			// TODO: handle exception
				e.printStackTrace();
				return;
			}
		catch (SplitException e) {
			// TODO: handle exception
			e.printStackTrace();
			return;
		}
		

		//System.out.println(tree);
		
		outputReport.println("Symbolic Clustering");
		System.out.println(tree.symbolicClusterDescription(""));
		outputReport.println(tree.symbolicClusterDescription(""));
		
		
	
		//KNNModel knn=new KNNModel();
		
		//tree.populateKNNModel(knn,snap,0,snap.size()-1,schema,W);
		//tree.populateKNNModel(knn);
		
		
		tree.saveSpatialClustering("output/cluster/train/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".csv",snap,schema);
		
		// testing phase
		schema=new SnapshotSchema(config);				
		FileReader inputTestFileReader;
		BufferedReader inputTestStream;
		inputTestFileReader = new FileReader(test+".arff");
		inputTestStream   = new BufferedReader(inputTestFileReader);

		do{
			inline=inputTestStream.readLine();
		}
		while(!inline.equals("@data"));
		
		
		SnapshotData snapTest=null;
		try{
			snapTest=new SnapshotData(inputTestStream,schema);
		}catch (EOFException e) {
			// TODO: handle exception
		}
		timeBegin =  timeEnd;
		
		//String error=knn.testKnn(snapTest, schema,"output/csv/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".csv");
		
		timeEnd = new GregorianCalendar();
	
		
		outputReport.println("Interpolation time (milliseconds)="+(timeEnd.getTimeInMillis()-timeBegin.getTimeInMillis()));
	
		//String schemastr=";";
		//for(Feature f:schema.getTargetList())
		//	schemastr+=(f.getName()+";");
		//System.out.println(schemastr);
		//System.out.println(error);
		
		//outputReport.println("Error statistics");
		//outputReport.println(schemastr);		
		//outputReport.println(error);
		
		
		tree.saveSpatialClustering("output/cluster/test/"+args[1]+"_"+sampling+"_"+centroidPercentage+"_"+testType+"_"+bPerc+"_"+heuristic+".csv",snapTest,schema);
		
		
		// Dispersione
		outputReport.println("Spatial dispersion");
		
		double trainingIntraDispersion=tree.computeSpatialIntraClusterDispersion(snap);
		double testingIntraDispersion=tree.computeSpatialIntraClusterDispersion(snapTest);
	
		outputReport.println("training intra-cluster dispersion;"+trainingIntraDispersion);
		outputReport.println("testing intra-cluster dispersion;"+testingIntraDispersion);
		System.out.println("training intra-cluster dispersion;"+ trainingIntraDispersion);
		System.out.println("testing intra-cluster dispersion;"+testingIntraDispersion);
		
		
		double trainingInterDispersion=tree.computeSpatialInterClusterDispersion(snap);
		double testingInterDispersion=tree.computeSpatialInterClusterDispersion(snapTest);
		outputReport.println("training inter-cluster dispersion;"+trainingInterDispersion);
		outputReport.println("testing inter-cluster dispersion;"+testingInterDispersion);
		System.out.println("training inter-cluster dispersion;"+trainingInterDispersion);
		System.out.println("testing inter-cluster dispersion;"+testingInterDispersion);
		
		
		inputTestStream.close();
		inputTestFileReader.close();
		
		}
		outputReport.close();

		
		
	}



}
*/