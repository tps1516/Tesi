package varUtility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import data.Network;
import data.feature.Feature;
import rForecast.ParametersRForecastIndex;
import rForecast.RVar;
import rForecast.VARParameter;
import snapshot.SnapshotSchema;

public class VAROutput {

	private static HashMap<String, PrintStream> RMSEFilesOutput;
	private static HashMap<String, PrintStream> VARParametersFeatureFilesOutput;
	public static HashMap<String, HashMap<String, Double>> counterRMSE;
	private static PrintStream RMSEOptimalFileOutput;

	public static void inizializedOutputFiles(ArrayList<Object> rParameters,
			SnapshotSchema schema, String dataset, String outputPath) {

		ParametersRForecastIndex index = new ParametersRForecastIndex();
		PrintStream countRMSE;
		PrintStream VARParameters;
		RMSEFilesOutput = new HashMap<String, PrintStream>();
		VARParametersFeatureFilesOutput = new HashMap<String, PrintStream>();
		counterRMSE = new HashMap<String, HashMap<String, Double>>();
		ArrayList<Double> counterRMSEFeature;

		FileOutputStream outputVARParameters = null;
		FileOutputStream outputOptimalRMSE = null;
		try {
			outputOptimalRMSE = new FileOutputStream(outputPath
					+ "output/R/RMSE/output_Optimal_RMSE_" + dataset + "_.csv");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		RMSEOptimalFileOutput = new PrintStream(outputOptimalRMSE);
		RMSEOptimalFileOutput.print(";");

		for (Feature f : schema.getTargetList()) {

			FileOutputStream outputRMSE = null;
			try {
				outputRMSE = new FileOutputStream(outputPath
						+ "output/R/RMSE/outputRMSE_" + dataset + "_"
						+ rParameters.get(index.TWSize) + "_" + f.getName()
						+ ".csv");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			countRMSE = new PrintStream(outputRMSE);
			countRMSE.print(";");

			RMSEOptimalFileOutput.print(f.getName() + ";");

			try {
				outputVARParameters = new FileOutputStream(outputPath
						+ "output/R/Parameters/outputVARParameters_" + dataset
						+ "_" + rParameters.get(index.TWSize) + "_"
						+ f.getName() + ".csv");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			VARParameters = new PrintStream(outputVARParameters);
			VARParameters.print(";");
			counterRMSEFeature = new ArrayList<Double>(RVar.getVARParameters()
					.size());

			HashMap<String, Double> hashFeatureByPar = new HashMap<String, Double>();
			for (VARParameter vp : RVar.getVARParameters()) {
				VARParameters.print(vp.toString() + ";");
				countRMSE.print(vp.toString() + ";");
				counterRMSEFeature.add(0.0);
				hashFeatureByPar.put(vp.toString(), 0.0);
			}
			countRMSE.print("best");
			VARParametersFeatureFilesOutput.put(f.getName(), VARParameters);
			RMSEFilesOutput.put(f.getName(), countRMSE);
			counterRMSE.put(f.getName(), hashFeatureByPar);
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
			Network network) {
		HashMap<String, HashMap<String, Integer>> parResult;
		HashMap<String, Double> optimalRMSEByFeature;

		ArrayList<Object> resultComputation = network.countPar(schema);
		/*
		 * E' il Map contenente il numero di volte che una configurazione di
		 * parametri è stata scelta per ogni feature
		 */
		parResult = (HashMap<String, HashMap<String, Integer>>) resultComputation
				.get(0);
		/*
		 * E' il Map che contiene la somma degli RMSE ottimali per feature
		 */
		optimalRMSEByFeature = (HashMap<String, Double>) resultComputation
				.get(1);
		/*
		 * Variabile contenente il numero di SensorPoint con il modello VAR
		 * avvalorato
		 */
		int counterSensorWithVARModel = (int) resultComputation.get(2);
		if (counterSensorWithVARModel==0) counterSensorWithVARModel=1;
		
		RMSEOptimalFileOutput.print("\n" + idSnapshot + ";");

		for (Feature f : schema.getTargetList()) {
			/*
			 * Gestisce la stampa del file che conta il numero di volte che una
			 * configurazione è stata scelta per una determinata feature
			 */
			HashMap<String, Integer> parByF = parResult.get(f.getName());
			PrintStream ps = VARParametersFeatureFilesOutput.get(f.getName());
			ps.print("\n" + idSnapshot + ";");
			for (VARParameter vp : RVar.getVARParameters()) {
				ps.print(parByF.get(vp.toString()) + ";");
			}

			/*
			 * Gestisce la stampa del file che determina la media dell'RMSE
			 * migliore per una determinata feature
			 */

			Double avgOptimalRMSE = optimalRMSEByFeature.get(f.getName())
					/ counterSensorWithVARModel;
			String bestSAvg = String.valueOf(Double.valueOf(avgOptimalRMSE));
			bestSAvg = bestSAvg.replace('.', ',');
			RMSEOptimalFileOutput.print(bestSAvg + ";");

			PrintStream stream = RMSEFilesOutput.get(f.getName());
			HashMap<String, Double> rmseFeature = counterRMSE.get(f.getName());
			stream.print("\n");
			stream.print("ID: " + idSnapshot + ";");

			for (VARParameter vp : RVar.getVARParameters()) {
				Double d = rmseFeature.get(vp.toString());
				if (d != null) {
					double avg = d / counterSensorWithVARModel;
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
		HashMap<String, Double> aux;
		for (String s : counterRMSE.keySet()) {
			aux = new HashMap<String, Double>();
			for (VARParameter vp : RVar.getVARParameters()) {
				aux.put(vp.toString(), 0.0);
			}
			counterRMSE.put(s, aux);
		}

	}

}
