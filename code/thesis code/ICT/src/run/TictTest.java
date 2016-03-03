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
import java.util.HashMap;
import java.util.LinkedList;

import rForecast.InvalidForecastParametersException;
import rForecast.ParametersChecker;
import rForecast.ParametersRForecastIndex;
import rForecast.RVar;
import rForecast.VARParameter;
//import mbrModel.KNNModel;
import snapshot.DynamicSnapshotWeight;
import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import tree.Tree;
import varUtility.VAROutput;
import windowStructure.TemporalWindow;
import data.EuclideanDistance;
import data.Network;
import data.SensorPoint;
import data.datavalue.NumericValue;
import data.datavalue.Value;
import data.feature.AutocorrelationI;
import data.feature.Feature;
import data.feature.ResubstitutionIndex;
import data.feature.ResubstitutionIndexOnGetisOrd;
import data.feature.GetisOrdIndex;
import forecast.Forecasting;
import forecast.ForecastingModel;

// completare deve prendere trainign and testing sets

public class TictTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PrintStream outputReport;

		String streamName = "";// "soace_ga_training_5XY.arff";
		String config = "";// "gasd.ini";

		GregorianCalendar timeGlobalBegin;
		GregorianCalendar timeGlobalEnd;

		float bperc = .1f;
		Network network = new Network();
		Integer splitNumber = 20; // random split identifier
		float centroidPercentage = .2f;
		String sampling = "quadtree";
		String testType = "target";
		String isSpatial = "GO";
		Integer TWSize = 0;
		String rPath;
		String lagMax;
		String season;
		String exogen;
		String ic;
		String type;
		Integer nahead;

		try {
			timeGlobalBegin = new GregorianCalendar();
			streamName = "dataset/" + args[0];// "soace_ga_training_5XY.arff";
			config = "dataset/" + args[1];// "gasd.ini";

			splitNumber = new Integer(args[2]); // random split identifier
			bperc = new Float(args[3]);
			centroidPercentage = new Float(args[4]);
			TWSize = new Integer(args[5]);
			rPath = String.valueOf(args[6]);
			lagMax = String.valueOf(args[7]);
			season = String.valueOf(args[8]);
			exogen = String.valueOf(args[9]);
			ic = String.valueOf(args[10]);
			type = String.valueOf(args[11]);
			nahead = new Integer(args[12]);
			/*
			 * sampling=new String(args[5]); testType=new String(args[6]);
			 * isSpatial=new Boolean(args[7]);
			 */
		} catch (IndexOutOfBoundsException e) {
			String report = "TICT@KDDE.UNIBA.IT\n";
			report += "author = Annalisa Appice\n\n";
			report += "run jar by using the input parameters:\n";
			report += "streamfile configfile numSplits bandwidthPercentage samplingPercentage\n";
			report += "streamfileTRAIN.arff and streamfileTEST are stored in a local directory named \'dataset\'\n";
			report += "the first snapshot of both training and testing streams contain the network structure\n";
			report += "nummSplit=20 (default)\n";
			report += "bandwidthPercentage: real value in ]0,1]\n";
			report += "samplingPercentage: real value in ]0,1]\n";
			report += "Reports are created in the local directory named \'output\'\n";

			System.out.println(report);
			return;

		}

		ArrayList<Object> rParameters = new ArrayList<Object>();
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		rParameters.add(index.rPath, rPath);
		rParameters.add(index.lagMax, lagMax);
		rParameters.add(index.season, season);
		rParameters.add(index.exogen, exogen);
		rParameters.add(index.ic, ic);
		rParameters.add(index.type, type);
		rParameters.add(index.TWSize, TWSize);

		try {
			ParametersChecker checker = new ParametersChecker(TWSize - 2);
			checker.check(rParameters);
		} catch (InvalidForecastParametersException e) {
			System.out.println(e.getMessage());
			return;
		}

		RVar.initializedAcceptableIc();
		RVar.initializedAcceptableType();
		RVar.loadCombinations(rParameters);

		String configStr = "";
		AutocorrelationI autoCorrelation;

		if (isSpatial.equals("GO"))
			autoCorrelation = new ResubstitutionIndexOnGetisOrd();
		else
			autoCorrelation = new ResubstitutionIndex();

		/*
		 * Crea la stringa per la denominazione del file di output .report
		 * (/output/stream/report)
		 */
		String name = "TWSize_" + TWSize + "_Type_"
				+ rParameters.get(index.type) + "_ic_"
				+ rParameters.get(index.ic);

		outputReport = new PrintStream(new FileOutputStream(
				"output/stream/report/" + args[0] + name + "_TICT.report"));

		outputReport.println("TRAIN STREAM=" + args[0] + "TRAIN");
		outputReport.println("TEST STREAM=" + args[0] + "TEST");

		SnapshotSchema schemaTrain = null, schemaTest = null;

		try {
			/*
			 * Inizio del software, qui passerà solamente una volta, la prima
			 */
			Tree tree = null;
			double b = Double.MAX_VALUE;
			try {
				/*
				 * Creerà il primo schema del dataset.
				 */
				schemaTrain = new SnapshotSchema(config + ".ini");

				configStr = "";
				for (Feature g : schemaTrain.getTargetList())
					configStr += g.getName() + ";";
				schemaTest = new SnapshotSchema(config + ".ini");

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
			SnapshotWeigth W = null;
			SnapshotData snapTrain = null;
			SnapshotData snapTest = null;
			/*
			 * E' lo snapshotData che si occuperà di tenere traccia dei sensori
			 * attivi e inattivi in ogni snapshot
			 */
			SnapshotData snapNetwork = null;
			/*
			 * E' lo snapshotData che si preoccuperà di tenere traccia del
			 * forecast su ogni sensore, per ogni snapshot. Finché non ci sarà
			 * una previsione, rimarrà avvalorato a Double.MAX_VALUE.
			 */
			SnapshotData snapForecast = null;

			try {
				inputStreamTrain = new BufferedReader(new FileReader(streamName
						+ "TRAIN.arff"));
				inputStreamTest = new BufferedReader(new FileReader(streamName
						+ "TEST.arff"));

				String out = "";

				try {
					String inlineTrain = inputStreamTrain.readLine();
					String inlineTest = inputStreamTest.readLine();

					if (!inlineTrain.equals("@") || !inlineTest.equals("@"))
						return;

					while (true) {
						try {
							// il primo snapshot contiene solo il network

							if (snapTrain == null) {
								/*
								 * Entrerà solo una volta, la prima, leggerà il
								 * network e creerà lo snapshot di network.
								 */
								snapTrain = new SnapshotData(inputStreamTrain,
										schemaTrain);

								snapTest = new SnapshotData(inputStreamTest,
										schemaTest, true);
								if (snapTrain.size() == 0
										|| snapTest.size() == 0)
									continue;

								snapTrain.sort();
								network.createWork(snapTrain, schemaTrain,
										TWSize);

								snapForecast = snapTrain
										.createAndAvvalorateSnapForecast(schemaTrain);
								snapForecast.sort();

							} else {
								snapTrain = new SnapshotData(inputStreamTrain,
										schemaTrain);

								snapTest = new SnapshotData(inputStreamTest,
										schemaTest, true);
								if (snapTrain.size() == 0
										|| snapTest.size() == 0)
									continue;
								snapTrain.sort();
								snapForecast.sort();
								snapNetwork = snapTrain.mergeSnapshotData(
										snapForecast, schemaTrain);
								network.updateNetwork(snapNetwork, schemaTrain);
							}
							if (W == null) {
								/*
								 * Avvalora la prima riga del file di output
								 * dove vengono calcolate le medie degli RMSE
								 */
								VAROutput.inizializedOutputFiles(rParameters,
										schemaTrain, args[0]);
								GregorianCalendar timeBegin = new GregorianCalendar();
								b = SnapshotWeigth.maxDist(
										snapTrain,
										new EuclideanDistance(snapTrain
												.getSpatialFeaturesSize()));
								W = new DynamicSnapshotWeight(b * bperc);
								W.updateSnapshotWeigth(
										snapTrain,
										new EuclideanDistance(snapTrain
												.getSpatialFeaturesSize()));

								GregorianCalendar timeEnd = new GregorianCalendar();
								outputReport
										.println("W computation time (milliseconds):"
												+ (timeEnd.getTimeInMillis() - timeBegin
														.getTimeInMillis()));

								String nomeFile = "output/stream/weight/"
										+ args[0] + ".wmodel";
								W.salva(nomeFile);

								snapTrain = new SnapshotData(inputStreamTrain,
										schemaTrain);
								snapTest = new SnapshotData(inputStreamTest,
										schemaTest, true);
								snapTrain.sort();
								snapNetwork = snapTrain.mergeSnapshotData(
										snapForecast, schemaTrain);
								network.updateNetwork(snapNetwork, schemaTrain);
							}

							snapTrain.updateNull(W, schemaTrain);

							GregorianCalendar timeBegin = new GregorianCalendar();
							java.util.Date currentTimeBegin = timeBegin
									.getTime();

							if (!isSpatial.equals("GO")
									&& schemaTrain.getTargetList().size() > 1)
								snapTrain.scaledSchema(schemaTrain);

							if (autoCorrelation instanceof ResubstitutionIndexOnGetisOrd)
								snapTrain.updateGetisAndOrd(W, schemaTrain);
							else
								snapTrain.scaledSchema(schemaTrain);
							System.out.println("**** ID: "
									+ snapTrain.getIdSnapshot());
							outputReport.println("**** ID: "
									+ snapTrain.getIdSnapshot());

							if (tree == null)// first snapshot in the stream
							{

								timeBegin = new GregorianCalendar();

								tree = new Tree(snapTrain, schemaTrain, W,
										autoCorrelation, splitNumber,
										centroidPercentage, sampling, testType,
										TWSize, rParameters);

								GregorianCalendar timeEnd = new GregorianCalendar();
								System.out.println(tree);
								long time = timeEnd.getTimeInMillis()
										- timeBegin.getTimeInMillis();

								tree.setComputationTime(time);

								outputReport.println(tree.toString());
								outputReport
										.println("Computation time(milliseconds)="
												+ time);

								int pastLeaves = 0, afterPruningLeaves = 0, newLeaves = 0;
								newLeaves = tree.countLeaves();

								outputReport.println("Leaves statisics");
								outputReport
										.println("number of leaves of the tree inherited from the past="
												+ pastLeaves);
								outputReport
										.println("number of leaves of the tree after pruning="
												+ afterPruningLeaves);
								outputReport
										.println("number of leaves in the new tree="
												+ newLeaves);

								tree.salva("output/stream/model/" + args[0]
										+ name + snapTrain.getIdSnapshot()
										+ "TICT.model");

							} else {

								int pastLeaves = 0, afterPruningLeaves = 0, newLeaves = 0;
								pastLeaves = tree.countLeaves();
								timeBegin = new GregorianCalendar();
								currentTimeBegin = timeBegin.getTime();
								tree.prune(snapTrain, schemaTrain, W,
										autoCorrelation);
								afterPruningLeaves = tree.countLeaves();
								tree.drift(snapTrain, schemaTrain, W,
										autoCorrelation, splitNumber,
										centroidPercentage, sampling, testType,
										rParameters);

								GregorianCalendar timeEnd = new GregorianCalendar();

								tree.setComputationTime(timeEnd
										.getTimeInMillis()
										- timeBegin.getTimeInMillis());
								System.out.println(tree);

								if (tree.existVARModel()) {

									/*
									 * HashMap<Integer, ForecastingModel> hm =
									 * tree
									 * .deriveForecastingModel(snapNetwork);
									 * 
									 * for (Integer i : hm.keySet()) {
									 * System.out.println("ID SENSORE: " + i);
									 * System.out.println(hm.get(i)); }
									 */
									Forecasting forecast = new Forecasting(
											nahead, schemaTrain, tree,
											snapNetwork, network);
									LinkedList<SnapshotData> res = forecast
											.forecasting();
									snapForecast=res.get(0);

								}
								outputReport.println(tree.toString());
								outputReport
										.println("Computation time(milliseconds)="
												+ (timeEnd.getTimeInMillis() - timeBegin
														.getTimeInMillis()));

								newLeaves = tree.countLeaves();
								outputReport
										.println("number of leaves of the tree inherited from the past="
												+ pastLeaves);
								outputReport
										.println("number of leaves of the tree after pruning="
												+ afterPruningLeaves);
								outputReport
										.println("number of leaves in the new tree="
												+ newLeaves);

								outputReport.println("\n");

								tree.salva("output/stream/model/" + args[0]
										+ name + snapTrain.getIdSnapshot()
										+ "TICT.model");

								VAROutput.printAndReset(schemaTrain, String
										.valueOf(snapTrain.getIdSnapshot()),
										tree);
							}

						} catch (EOFException e) {
							break;
						}
					}
				} catch (EOFException e) {
					System.out.println(e);
				} catch (IOException e) {
					System.out.println(e);

				} finally {
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
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			outputReport.close();
			VAROutput.closeFiles();
			for (Integer i : network) {
				System.out.println(i);
				System.out.print(network.getTemporalWindow(i));

			}
		}

	}
}
