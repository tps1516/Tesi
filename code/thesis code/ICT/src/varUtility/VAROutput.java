package varUtility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import data.feature.Feature;
import rForecast.ParametersRForecastIndex;
import rForecast.RVar;
import rForecast.VARParameter;
import snapshot.SnapshotSchema;
import tree.Tree;

public class VAROutput {

	private static HashMap<String, PrintStream> RMSEFilesOutput;
	private static HashMap<String, PrintStream> VARParametersFeatureFilesOutput;
	public static HashMap<String, HashMap<String,Double>> counterRMSE;
	private static PrintStream RMSEOptimalFileOutput;

	public static void inizializedOutputFiles(ArrayList<Object> rParameters,
			SnapshotSchema schema, String dataset) {

		ParametersRForecastIndex index = new ParametersRForecastIndex();
		PrintStream countRMSE;
		PrintStream VARParameters;
		RMSEFilesOutput = new HashMap<String, PrintStream>();
		VARParametersFeatureFilesOutput = new HashMap<String, PrintStream>();
		counterRMSE = new HashMap<String, HashMap<String,Double>>();
		ArrayList<Double> counterRMSEFeature;

		FileOutputStream outputVARParameters = null;
		FileOutputStream outputOptimalRMSE=null;
		try {
			 outputOptimalRMSE = new FileOutputStream("output/R/RMSE/output_Optimal_RMSE_"+ dataset+"_.csv");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		RMSEOptimalFileOutput= new PrintStream(outputOptimalRMSE);
		RMSEOptimalFileOutput.print(";");
		
		for (Feature f : schema.getTargetList()) {
			
			
			FileOutputStream outputRMSE = null;
			try {
				outputRMSE = new FileOutputStream("output/R/RMSE/outputRMSE_"
						+ dataset + "_" + rParameters.get(index.TWSize) + "_"
						+ f.getName() + ".csv");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			countRMSE = new PrintStream(outputRMSE);
			countRMSE.print(";");
			
			RMSEOptimalFileOutput.print(f.getName()+";");
			
			try {
				outputVARParameters = new FileOutputStream(
						"output/R/Parameters/outputVARParameters_" + dataset
								+ "_" + rParameters.get(index.TWSize) + "_"
								+ f.getName() + ".csv");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			VARParameters = new PrintStream(outputVARParameters);
			VARParameters.print(";");
			counterRMSEFeature = new ArrayList<Double>(RVar.getVARParameters()
					.size());
			
			HashMap<String,Double> hashFeatureByPar = new HashMap<String,Double>();
			for (VARParameter vp : RVar.getVARParameters()) {
				VARParameters.print(vp.toString() + ";");
				countRMSE.print(vp.toString()+";");
				counterRMSEFeature.add(0.0);
				hashFeatureByPar.put(vp.toString(), 0.0);
			}
			countRMSE.print("best");
			VARParametersFeatureFilesOutput.put(f.getName(), VARParameters);
			RMSEFilesOutput.put(f.getName(), countRMSE);
			counterRMSE.put(f.getName(),hashFeatureByPar);
		}
	}

	public static void closeFiles() {
		for (String s : RMSEFilesOutput.keySet()) {
			RMSEFilesOutput.get(s).close();
		}
		for (String s : VARParametersFeatureFilesOutput.keySet()) {
			VARParametersFeatureFilesOutput.get(s).close();
		}
		RMSEOptimalFileOutput.close();
	}

	public static void printAndReset(SnapshotSchema schema, String idSnapshot,
			Tree tree) {
		HashMap<String, HashMap<String, Integer>> parResult;
		HashMap<String,Double> optimalRMSEByFeature;
		
		ArrayList<Object> resultComputation= tree.countPar();
		parResult = (HashMap<String, HashMap<String, Integer>>)resultComputation.get(0);
		optimalRMSEByFeature= (HashMap<String,Double>) resultComputation.get(1);
		
		
		int leavesintree = tree.countLeaves();
		
		/*
		for (VARParameter vp : RVar.getVARParameters()) {
			PrintStream stream = RMSEFilesOutput.get(vp.toString());
			ArrayList<Double> rmse = counterRMSE.get(vp.toString());
			stream.print("\n");
			stream.print("ID: " + idSnapshot + ";");
			if (rmse!=null) {
				for (Double d : rmse) {
					double avg = d / leavesintree;
					String savg = String.valueOf(Double.valueOf(avg));
					savg = savg.replace('.', ',');
					stream.print(savg + ";");
				}
			} else {
				for (Feature f: schema.getTargetList()) {
					stream.print("null" + ";");
				}
			}
		}
		
		
		
		resetRMSE();
		
		*/
		
		
		RMSEOptimalFileOutput.print("\n" + idSnapshot + ";");
		for (Feature f : schema.getTargetList()) {
			HashMap<String, Integer> parByF = parResult.get(f.getName());
			PrintStream ps = VARParametersFeatureFilesOutput.get(f.getName());
			ps.print("\n" + idSnapshot + ";");
			for (VARParameter vp : RVar.getVARParameters()) {
				ps.print(parByF.get(vp.toString()) + ";");
			}
			
			Double avgOptimalRMSE= optimalRMSEByFeature.get(f.getName())/leavesintree;
			String bestSAvg = String.valueOf(Double.valueOf(avgOptimalRMSE));
			bestSAvg = bestSAvg.replace('.', ',');
			RMSEOptimalFileOutput.print(bestSAvg+";");
			
			
			PrintStream stream = RMSEFilesOutput.get(f.getName());
			HashMap<String,Double> rmseFeature = counterRMSE.get(f.getName());
			stream.print("\n");
			stream.print("ID: " + idSnapshot + ";");
			
		
			for (VARParameter vp : RVar.getVARParameters()) {
				Double d=rmseFeature.get(vp.toString());	
				if (d!= null) {
					double avg = d / leavesintree;
					String sAvg = String.valueOf(Double.valueOf(avg));
					sAvg = sAvg.replace('.', ',');
					stream.print(sAvg + ";");
				} else {
					stream.print("null" + ";");
				}
				
			}
			stream.print(bestSAvg);
					
		}
	
		resetRMSE();
			
	}

	private static void resetRMSE() {
		HashMap<String,Double> aux;
		for (String s : counterRMSE.keySet()) {
			aux = new HashMap<String,Double>();
			for (VARParameter vp : RVar.getVARParameters()) {
				aux.put (vp.toString(),0.0);
			}
			counterRMSE.put(s, aux);
		}

	}

}
