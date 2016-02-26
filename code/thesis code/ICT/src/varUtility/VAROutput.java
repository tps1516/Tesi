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
	public static HashMap<String, ArrayList<Double>> counterRMSE;

	public static void inizializedOutputFiles(ArrayList<Object> rParameters,
			SnapshotSchema schema, String dataset) {

		ParametersRForecastIndex index = new ParametersRForecastIndex();
		PrintStream countRMSE;
		PrintStream VARParameters;
		RMSEFilesOutput = new HashMap<String, PrintStream>();
		VARParametersFeatureFilesOutput = new HashMap<String, PrintStream>();
		counterRMSE = new HashMap<String, ArrayList<Double>>();
		ArrayList<Double> counterRMSEFeature;

		for (VARParameter vp : RVar.getVARParameters()) {

			FileOutputStream outputRMSE = null;
			try {
				outputRMSE = new FileOutputStream("output/R/RMSE/outputRMSE_"
						+ dataset + "_" + rParameters.get(index.TWSize) + "_"
						+ vp.toString() + ".csv");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			countRMSE = new PrintStream(outputRMSE);
			counterRMSEFeature = new ArrayList<Double>(schema.getTargetList()
					.size());
			countRMSE.print(";");
			for (Feature f : schema.getTargetList()) {
				countRMSE.print(f.getName() + ";");
				counterRMSEFeature.add(f.getFeatureIndex(), 0.0);
			}
			counterRMSE.put(vp.toString(), counterRMSEFeature);
			RMSEFilesOutput.put(vp.toString(), countRMSE);
		}

		FileOutputStream outputVARParameters = null;
		for (Feature f : schema.getTargetList()) {

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
			for (VARParameter vp : RVar.getVARParameters()) {
				VARParameters.print(vp.toString() + ";");
			}
			VARParametersFeatureFilesOutput.put(f.getName(), VARParameters);
		}
	}

	public static void closeFiles() {
		for (String s : RMSEFilesOutput.keySet()) {
			RMSEFilesOutput.get(s).close();
		}
		for (String s : VARParametersFeatureFilesOutput.keySet()) {
			VARParametersFeatureFilesOutput.get(s).close();
		}
	}

	public static void printAndReset(SnapshotSchema schema, String idSnapshot,
			Tree tree) {
		HashMap<String, HashMap<String, Integer>> parResult;
		parResult = tree.countPar();

		int leavesintree = tree.countLeaves();
		for (VARParameter vp : RVar.getVARParameters()) {
			PrintStream stream = RMSEFilesOutput.get(vp.toString());
			ArrayList<Double> rmse = counterRMSE.get(vp.toString());
			stream.print("\n");
			stream.print("ID: " + idSnapshot + ";");
			for (Double d : rmse) {
				double avg = d / leavesintree;
				String savg = String.valueOf(Double.valueOf(avg));
				savg = savg.replace('.', ',');
				stream.print(savg + ";");
			}

		}
		resetRMSE(schema);

		for (Feature f : schema.getTargetList()) {
			HashMap<String, Integer> parByF = parResult.get(f.getName());
			PrintStream ps = VARParametersFeatureFilesOutput.get(f.getName());
			ps.print("\n" + idSnapshot + ";");
			for (VARParameter vp : RVar.getVARParameters()) {
				ps.print(parByF.get(vp.toString()) + ";");
			}
		}

	}

	private static void resetRMSE(SnapshotSchema schema) {
		ArrayList<Double> aux;
		for (String s : counterRMSE.keySet()) {
			aux = new ArrayList<Double>(schema.getTargetList().size());
			for (Feature f : schema.getTargetList()) {
				aux.add(f.getFeatureIndex(), 0.0);
			}
			counterRMSE.put(s, aux);
		}

	}

}
