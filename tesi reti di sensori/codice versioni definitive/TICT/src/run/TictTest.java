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
import java.util.LinkedList;
import rForecast.InvalidForecastParametersException;
import rForecast.ParametersChecker;
import rForecast.ParametersRForecastIndex;
import rForecast.RVar;
import snapshot.DynamicSnapshotWeight;
import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import tree.Tree;
import varUtility.VAROutput;
import data.EuclideanDistance;
import data.Network;
import data.feature.AutocorrelationI;
import data.feature.Feature;
import data.feature.ResubstitutionIndex;
import data.feature.ResubstitutionIndexOnGetisOrd;
import forecast.Forecasting;
import forecast.OutputReport;


public class TictTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PrintStream outputReport;
		PrintStream outputComputationTime;
		PrintStream outputClustering;

		String filesPath = "";
		String streamName = "";// ".arff";
		String config = ""; // ".ini"

		/*
		 * raggio per il calcolo dell'autocorrelazione
		 */
		float bperc = .1f;

		/*
		 * stream dei sensori, tiene traccia per ogni sensore dei valori assunti
		 * da essi per ogni feature
		 */
		Network network = new Network();

		/*
		 * numero di split
		 */
		Integer splitNumber = 20; // random split identifier

		/*
		 * metrica per il calocolo dell'autocorrelazione
		 */
		String isSpatial = "GO";

		String testType = "target";

		/*
		 * dimensione massima della finestra temporale quando è piena è
		 * possibile iniziare il forecasting
		 */
		Integer TWSize = 0;

		/*
		 * path dello script R.exe
		 */
		String rPath;

		/*
		 * parametri utilizzati per la costruzione del modello VAR
		 */
		String lagMax;
		String season;
		String exogen;
		String ic;
		String type;

		/*
		 * numero di forecast da effettuare
		 */
		Integer nahead;

		OutputReport outputForecastReport;

		/*
		 * vado a leggere i parametri in input al software
		 */
		try {

			filesPath = String.valueOf(args[12]) + "/";
			streamName = filesPath+"dataset/" + args[0];// ".arff";
			config = filesPath+"dataset/" + args[1];// ".ini";
			splitNumber = new Integer(args[2]); // random split identifier
			bperc = new Float(args[3]);
			TWSize = new Integer(args[4]);
			rPath = String.valueOf(args[5]);
			lagMax = String.valueOf(args[6]);
			season = String.valueOf(args[7]);
			exogen = String.valueOf(args[8]);
			ic = String.valueOf(args[9]);
			type = String.valueOf(args[10]);
			nahead = new Integer(args[11]);
			
		} catch (IndexOutOfBoundsException e) {
			String report = "TICT@KDDE.UNIBA.IT\n";
			report += "author = Annalisa Appice\n\n";
			report += "run jar by using the input parameters:\n";
			report += "streamfile configfile numSplits bandwidthPercentage \n";
			report += "TWsize rPath lagMax season exogen ic type nahead \n";
			report += "streamfileTRAIN.arff is stored in a local directory named \'dataset\'\n";
			report += "the first snapshot contain the network structure\n";
			report += "nummSplit=20 (default)\nTWSize>0 ";
			report += "rPath=where RScript.exe is located on your computer\n";
			report += "lagMax>0 && lagMax < TWSize-2\n";
			report += "season>0 && season < 13 or NULL\n";
			report += "exogen=matrix of exogen variables or NULL\n";
			report += "ic=AIC or ic=HQ or ic=SC or ic=FPE or ic=ALL\n";
			report += "criteria used for VAR model's construction\n";
			report += "type=const or type=trend or type=both or type=none or type=ALL\n";
			report += "type of deterministic regressor for VAR model's construction\n";
			report += "nahead>0, number of focast";
			report += "Reports are created in the local directory named \'output\'\n";

			System.out.println(report);
			return;

		}
		String dataName = args[0];

		/*
		 * Inizializzo il file per la stampa del costo in tempo delle operazioni
		 */
		outputComputationTime = new PrintStream(new FileOutputStream(filesPath
				+ "output/stream/report/ComputationTime/" + dataName + TWSize
				+ "_" + ic + "_" + type + "_CTime.csv"));
		outputComputationTime.print(";Learn Tree;Learn VAR Model;Forecast\n");

		outputClustering = new PrintStream(new FileOutputStream(filesPath
				+ "output/stream/report/clustering/" + dataName
				+ "_NumberOfCluster.csv"));
		outputClustering.print(";Number of Cluster\n");

		/*
		 * inizializzo l'arrayList che memorizza i parametri per la costruzione
		 * del modello var
		 */
		ArrayList<Object> rParameters = new ArrayList<Object>();
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		rParameters.add(index.rPath, rPath);
		rParameters.add(index.lagMax, lagMax);
		rParameters.add(index.season, season);
		rParameters.add(index.exogen, exogen);
		rParameters.add(index.ic, ic);
		rParameters.add(index.type, type);
		rParameters.add(index.TWSize, TWSize);

		/*
		 * effettuo un controllo sui parametri e nel caso in cui non venga
		 * superato cattura e gestisco l'eccezione fermando l'esecuzione del
		 * software
		 */
		try {
			ParametersChecker checker = new ParametersChecker(TWSize - 2);
			checker.check(rParameters);
		} catch (InvalidForecastParametersException e) {
			System.out.println(e.getMessage());
			return;
		}

		/*
		 * inizializzo nella classe RVar l'oggetto che contiene tutti i
		 * possibili valori assumibili da ic
		 */
		RVar.initializedAcceptableIc();

		/*
		 * inizializzo nella classe RVar l'oggetto che contiene tutti i
		 * possibili valori assumibili da type
		 */
		RVar.initializedAcceptableType();

		/*
		 * costruisco tutte le possibili configurazioni per la costruzione del
		 * modello VAR, dipendentemente dai parametri assunti da ic e type in
		 * input
		 */
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

		outputReport = new PrintStream(new FileOutputStream(filesPath
				+ "output/stream/report/" + args[0] + name + "_TICT.report"));

		outputReport.println("TRAIN STREAM=" + args[0] + "TRAIN");

		outputForecastReport = new OutputReport(nahead, rParameters, dataName,
				filesPath);

		SnapshotSchema schema = null;

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
				schema = new SnapshotSchema(config + ".ini");

				configStr = "";
				for (Feature g : schema.getTargetList())
					configStr += g.getName() + ";";

			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
				return;
			} catch (ErrorFormatException e) {
				System.out.println(e);
				e.printStackTrace();
				return;
			}

			BufferedReader inputStream;
			SnapshotWeigth W = null;

			/*
			 * snapshotData corrente
			 */
			SnapshotData snapData = null;

			/*
			 * E' lo snapshotData che si occuperà di tenere traccia dei sensori
			 * attivi e inattivi in ogni snapshot
			 */
			SnapshotData snapNetwork = null;

			/*
			 * E' lo snapshotData che si preoccuperà di tenere traccia del
			 * forecast con nahead=1 effettuato su ogni sensore, per ogni
			 * snapshot. Finché non ci sarà una previsione, rimarrà avvalorato a
			 * Double.MAX_VALUE.
			 */
			SnapshotData snapForecast = null;

			try {
				inputStream = new BufferedReader(new FileReader(streamName
						+ "TRAIN.arff"));

				String out = "";

				try {
					String inline = inputStream.readLine();

					if (!inline.equals("@"))
						return;

					while (true) {
						try {
							// il primo snapshot contiene solo il network

							if (snapData == null) {
								/*
								 * Entrerà solo una volta, la prima, leggerà il
								 * network e creerà lo snapshot di network.
								 */
								snapData = new SnapshotData(inputStream, schema);

								if (snapData.size() == 0)
									continue;

								snapData.sort();
								network.createWork(snapData, schema, TWSize);

								snapForecast = snapData.initializeSnapshotForecast(schema);
							} else {
								snapData = new SnapshotData(inputStream, schema);

								if (snapData.size() == 0)
									continue;
								snapData.sort();
								snapNetwork = snapData.mergeSnapshotData(
										snapForecast, schema);
								network.updateNetwork(snapNetwork, schema);
							}
							if (W == null) {
								/*
								 * Avvalora la prima riga del file di output
								 * dove vengono calcolate le medie degli RMSE
								 */
								VAROutput.inizializedOutputFiles(rParameters,
										schema, dataName, filesPath);
								GregorianCalendar timeBegin = new GregorianCalendar();
								b = SnapshotWeigth.maxDist(
										snapData,
										new EuclideanDistance(snapData
												.getSpatialFeaturesSize()));
								W = new DynamicSnapshotWeight(b * bperc);
								W.updateSnapshotWeigth(
										snapData,
										new EuclideanDistance(snapData
												.getSpatialFeaturesSize()));

								GregorianCalendar timeEnd = new GregorianCalendar();
								outputReport
										.println("W computation time (milliseconds):"
												+ (timeEnd.getTimeInMillis() - timeBegin
														.getTimeInMillis()));

								String nomeFile = filesPath
										+ "output/stream/weight/" + args[0]
										+ ".wmodel";
								W.salva(nomeFile);

								snapData = new SnapshotData(inputStream, schema);

								snapData.sort();
								snapNetwork = snapData.mergeSnapshotData(
										snapForecast, schema);
								network.updateNetwork(snapNetwork, schema);
							}

							snapData.updateNull(W, schema);

							GregorianCalendar timeBegin = new GregorianCalendar();
							java.util.Date currentTimeBegin = timeBegin
									.getTime();

							if (!isSpatial.equals("GO")
									&& schema.getTargetList().size() > 1)
								snapData.scaledSchema(schema);

							if (autoCorrelation instanceof ResubstitutionIndexOnGetisOrd)
								snapData.updateGetisAndOrd(W, schema);
							else
								snapData.scaledSchema(schema);
							System.out.println("**** ID: "
									+ snapData.getIdSnapshot());
							outputReport.println("**** ID: "
									+ snapData.getIdSnapshot());

							if (snapData.getIdSnapshot() == 101) {
								System.out.println("END");
								outputReport.close();
								VAROutput.closeFiles();
								outputClustering.close();
								outputComputationTime.close();
								return;
							}

							if (tree == null)// first snapshot in the stream
							{

								timeBegin = new GregorianCalendar();

								tree = new Tree(snapData, schema, W,
										autoCorrelation, splitNumber, testType,
										TWSize);

								GregorianCalendar timeEnd = new GregorianCalendar();
								System.out.println(tree);
								long time = timeEnd.getTimeInMillis()
										- timeBegin.getTimeInMillis();

								tree.setComputationTime(time);

								outputReport.println(tree
										.symbolicClusterDescription(""));
								outputClustering.print(snapData.getIdSnapshot()
										+ ";" + tree.countLeaves() + "\n");
								// inizia tempo
								GregorianCalendar timeBeginVARModel = new GregorianCalendar();
								tree.learnVARModel(rParameters);
								// finisci tempo
								GregorianCalendar timeEndVARModel = new GregorianCalendar();
								long learnVARModelTime = timeEndVARModel
										.getTimeInMillis()
										- timeBeginVARModel.getTimeInMillis();
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

								tree.salva(filesPath + "output/stream/model/"
										+ args[0] + name
										+ snapData.getIdSnapshot()
										+ "TICT.model");
								outputComputationTime.print(snapData
										.getIdSnapshot()
										+ ";"
										+ tree.getComputationTime()
										+ ";"
										+ learnVARModelTime + ";0\n");

							} else {

								int pastLeaves = 0, afterPruningLeaves = 0, newLeaves = 0;
								pastLeaves = tree.countLeaves();
								timeBegin = new GregorianCalendar();
								currentTimeBegin = timeBegin.getTime();
								tree.prune(snapData, schema, W, autoCorrelation);
								afterPruningLeaves = tree.countLeaves();
								tree.drift(snapData, schema, W,
										autoCorrelation, splitNumber, testType);

								GregorianCalendar timeEnd = new GregorianCalendar();
								outputClustering.print(snapData.getIdSnapshot()
										+ ";" + tree.countLeaves() + "\n");
								tree.setComputationTime(timeEnd
										.getTimeInMillis()
										- timeBegin.getTimeInMillis());

								GregorianCalendar timeBeginVARModel = new GregorianCalendar();
								tree.learnVARModel(rParameters);
								GregorianCalendar timeEndVARModel = new GregorianCalendar();
								long learnVARModelTime = timeEndVARModel
										.getTimeInMillis()
										- timeBeginVARModel.getTimeInMillis();
								long forecastTime = 0;
								System.out.println(tree);

								GregorianCalendar timeBeginForecast = new GregorianCalendar();
								if (tree.existVARModel()) {

									/*
									 * HashMap<Integer, ForecastingModel> hm =
									 * tree
									 * .deriveForecastingModel(snapNetwork);
									 */

									Forecasting forecast = new Forecasting(
											nahead, schema, tree, snapNetwork,
											network);
									LinkedList<SnapshotData> res = forecast
											.forecasting();
									GregorianCalendar timeEndForecast = new GregorianCalendar();
									forecastTime = timeEndForecast
											.getTimeInMillis()
											- timeBeginForecast
													.getTimeInMillis();
									snapForecast = res.get(0);
									outputForecastReport.addForecast(res);

									/*
									 * for (Integer i : hm.keySet()) {
									 * System.out.println("ID SENSORE: " + i);
									 * System.out.println(hm.get(i)); }
									 */

								} else {

									GregorianCalendar timeEndForecast = new GregorianCalendar();
									forecastTime = timeEndForecast
											.getTimeInMillis()
											- timeBeginForecast
													.getTimeInMillis();
								}
								outputReport.println(tree
										.symbolicClusterDescription(""));
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

								tree.salva(filesPath + "output/stream/model/"
										+ args[0] + name
										+ snapData.getIdSnapshot()
										+ "TICT.model");

								/*
								 * scriviamo i file di output per le statische
								 */
								VAROutput.printAndReset(schema, String
										.valueOf(snapData.getIdSnapshot()),
										tree);
								outputComputationTime.print(snapData
										.getIdSnapshot()
										+ ";"
										+ tree.getComputationTime()
										+ ";"
										+ learnVARModelTime
										+ ";"
										+ forecastTime + "\n");
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
						inputStream.close();

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
			outputClustering.close();
			outputComputationTime.close();
			outputForecastReport.closeFiles();

		}

	}
}
