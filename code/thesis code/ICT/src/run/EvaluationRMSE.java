package run;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;

import data.feature.Feature;
import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;

public class EvaluationRMSE {

	public static void main(String[] args) throws IOException {
		String streamName = "";// ".arff"
		String config = ""; // ".ini"
		String nAheadName = ""; // ".arff"
		String ic = ""; // ".arff"
		String type = ""; // ".arff"
		Integer nahead = 0;
		Integer TWSize = 0;
		ArrayList<BufferedReader> nAheadFiles = new ArrayList<BufferedReader>();
		ArrayList<PrintStream> output = new ArrayList<PrintStream>();

		try {
			streamName = "dataset/" + args[0];
			config = "dataset/" + args[1];
			nAheadName = "output/stream/forecast/" + args[2];
			TWSize = new Integer(args[3]);
			ic = String.valueOf(args[4]);
			type = String.valueOf(args[5]);
			nahead = new Integer(args[6]);
		} catch (IndexOutOfBoundsException e) {
			String report = "authors = Giancaspro Corrado & Mastropasqua Donato \n \n";
			report += "run jar by using the input parameters:\n";
			report += "streamfile configfile nAheadfileName\n";
			report += "TWsize ic type nahead \n";
			report += "streamfileTRAIN.arff is stored in a local directory named \'dataset\'\n";
			report += "the first snapshot contain the network structure\n";
			report += "nAheadfileName.arff is stored in a local directory named \'output\'stream\'forecast\'";
			report += "ic=AIC or ic=HQ or ic=SC or ic=FPE or ic=ALL\n";
			report += "criteria used for VAR model's construction\n";
			report += "type=const or type=trend or type=both or type=none or type=ALL\n";
			report += "type of deterministic regressor for VAR model's construction\n";
			report += "nahead>0, number of forecast";
			report += "Reports are created in the local directory named \'output\'\n";
			System.out.println(report);
			return;
		}
		String configStr = "";
		String dataName = args[0];
		SnapshotSchema schema;
		try {
			schema = new SnapshotSchema(config + ".ini");
			configStr = "";
			for (Feature g : schema.getTargetList())
				configStr += g.getName() + ";";
		} catch (ErrorFormatException e) {
			System.out.println(e);
			e.printStackTrace();
			return;
		}

		for (int i = 1; i <= nahead; i++) {
			try {
				PrintStream outputFile = new PrintStream(new FileOutputStream(
						"output/stream/report/ForecastRMSE/" + dataName
								+ "_nahead_" + i + "_" + TWSize + "_" + ic
								+ "_" + type + "_RMSE.csv"));
				outputFile.print(";" + configStr + "\n");
				output.add(outputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		BufferedReader inputStream;
		SnapshotData snapData = null;
		SnapshotData snapForecasted;

		for (int i = 1; i <= nahead; i++) {
			try {
				BufferedReader inputFilenAhead = new BufferedReader(
						new FileReader("output/stream/forecast/" + dataName
								+ "_nahead_" + i + "_" + TWSize + "_" + ic
								+ "_" + type + ".arff"));
				inputFilenAhead.readLine();
				nAheadFiles.add(inputFilenAhead);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		inputStream = new BufferedReader(new FileReader(streamName
				+ "TRAIN.arff"));
		String inlineReal = inputStream.readLine();
		if (!inlineReal.equals("@")) {
			inputStream.close();
			return;
		}
		int id = 1;
		int diff = 0;
		LinkedList<Boolean> find = new LinkedList<Boolean>();
		for (Feature f : schema.getTargetList()) {
			find.add(f.getFeatureIndex(), false);
		}
		try {
			while (true) {
				ArrayList<SnapshotData> forecasted = new ArrayList<SnapshotData>();

				snapData = new SnapshotData(inputStream, schema);

				if (snapData.getIdSnapshot() <= TWSize + 1)
					continue;
				diff = snapData.getIdSnapshot() - TWSize - 1;
				for (int i = 1; i <= diff; i++) {
					if (i > nahead)
						break;
					snapForecasted = new SnapshotData(nAheadFiles.get(i - 1),
							schema, true);
					System.out.println(snapForecasted);
					forecasted.add(snapForecasted);
				}

				int i = 0;
				for (SnapshotData dataFor : forecasted) {
					LinkedList<Double> RMSE = new LinkedList<Double>();
					RMSE = snapData.computeRMSE(dataFor, schema);
					print(output.get(i), RMSE, snapData.getIdSnapshot());
					i = i + 1;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void print(PrintStream file, LinkedList<Double> RMSE,
			int idSnapshot) {
		file.print(idSnapshot + ";");
		for (Double d : RMSE) {
			String p = d.toString();
			p = p.replace('.', ',');
			file.print(p + ";");
		}
		file.print("\n");
	}

}
