package run;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;

import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import data.SensorPoint;
import data.feature.Feature;

public class EvalutationSmoothingRMSE {

	public static void main(String[] args) throws IOException {
		String streamName = "";// ".arff"
		String config = ""; // ".ini"
		String smoothingName = ""; // ".arff"
		Double alpha = 0.0;

		try {
			streamName = "dataset/" + args[0];
			config = "dataset/" + args[1];
			smoothingName = "output/stream/smoothing/" + args[2];
			alpha = new Double(args[3]);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("eccezione");
		}
		String dataName = args[0];
		String configStr = "";
		SnapshotSchema schema;
		PrintStream outputFile = null;

		try {
			schema = new SnapshotSchema(config + ".ini");
			configStr = "";
			for (Feature g : schema.getTargetList())
				configStr += g.getName() + ";";
		} catch (ErrorFormatException | IOException e) {
			System.out.println(e);
			e.printStackTrace();
			return;
		}
		try {
			outputFile = new PrintStream(new FileOutputStream(
					"output/stream/smoothing/" + dataName + "_alpha" + alpha
							+ "_smoothing_RMSE.csv"));
			outputFile.print(";" + configStr + "\n");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedReader inputFile = new BufferedReader(new FileReader(
				"output/stream/smoothing/" + dataName + "_alfa" + alpha
						+ "smoothing.arff"));
		inputFile.readLine();

		BufferedReader inputStream;
		SnapshotData snapData = null;
		SnapshotData snapSmoot;

		inputStream = new BufferedReader(new FileReader(streamName
				+ "TRAIN.arff"));

		String inlineReal = inputStream.readLine();
		if (!inlineReal.equals("@")) {
			inputStream.close();
			return;
		}
		snapData = new SnapshotData(inputStream, schema);
		snapData = new SnapshotData(inputStream, schema);

		try {
			while (true) {
				snapData = new SnapshotData(inputStream, schema);
				snapSmoot = new SnapshotData(inputFile, schema, true);
				System.out.println(snapSmoot);
				LinkedList<Double> RMSE = new LinkedList<Double>();
				RMSE = computeRMSE(snapData,snapSmoot, schema);
				print(outputFile, RMSE, snapData.getIdSnapshot());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
	
	private static LinkedList<Double> computeRMSE(SnapshotData snapReal,
			SnapshotData snapForecast, SnapshotSchema schema) {
		LinkedList<Double> res = new LinkedList<Double>();

		for (Feature f : schema.getTargetList()) {
			res.add(0.0);
		}

		int counter = 0;
		ArrayList<SensorPoint> forecastedData = new ArrayList<SensorPoint>();

		for (SensorPoint spForecast : snapForecast) {
			forecastedData.add(spForecast);
		}

		Double prev = 0.0;
		int j = 0;
		for (SensorPoint spReal : snapReal) {
			boolean flag = false;

			if (spReal.getId() == forecastedData.get(j).getId()) {

				if (((Double) forecastedData
						.get(j)
						.getMeasure(
								schema.getTargetList().get(0).getFeatureIndex())
						.getValue()) != null) {
					counter++;
					for (Feature f : schema.getTargetList()) {

						Double dif = Math.pow(
								(Double) ((Double) spReal.getMeasure(
										f.getIndexMining()).getValue())
										- ((Double) forecastedData.get(j)
												.getMeasure(f.getIndexMining())
												.getValue()), 2);
						prev = res.get(f.getFeatureIndex());
						res.set(f.getFeatureIndex(), dif + prev);
					}
				}

				j = j + 1;

			} else {
				while (!flag) {
					j = j + 1;
					System.out.print(" SNAP : " + snapReal.getIdSnapshot()
							+ "\n i: " + spReal.getId() + " j: " + j);
					if (spReal.getId() == forecastedData.get(j).getId()) {

						if (((Double) forecastedData
								.get(j)
								.getMeasure(
										schema.getTargetList().get(0)
												.getFeatureIndex()).getValue()) != null) {
							counter++;
							for (Feature f : schema.getTargetList()) {

								Double dif = Math.pow((Double) ((Double) spReal
										.getMeasure(f.getIndexMining())
										.getValue())
										- ((Double) forecastedData.get(j)
												.getMeasure(f.getIndexMining())
												.getValue()), 2);
								prev = res.get(f.getFeatureIndex());
								res.set(f.getFeatureIndex(), dif + prev);
							}
						}
						flag = true;
						j = j + 1;
					}
				}
			}
		}
		int i = 0;
		for (Double d : res) {
			prev = res.get(i);
			prev /= counter;
			prev = Math.sqrt(prev);
			res.set(i, prev);
			i = i + 1;
		}
		return res;
	}

}
