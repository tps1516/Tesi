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
import snapshot.ErrorFormatException;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import varUtility.VAROutput;
import windowStructure.TemporalWindow;
import data.Network;
import data.SensorPoint;
import data.datavalue.NumericValue;
import data.datavalue.Value;
import data.feature.Feature;
import forecast.Forecasting;
import forecast.ForecastingModel;
import forecast.OutputReport;

public class run {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PrintStream outputComputationTime;

		String streamName = "";// ".arff";
		String config = ""; // ".ini"
		String filesPath = "";

		/*
		 * stream dei sensori, tiene traccia per ogni sensore dei valori assunti
		 * da essi per ogni feature
		 */
		Network network = new Network();

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
			filesPath = String.valueOf(args[10]) + "/";
			streamName = filesPath+"dataset/" + args[0];// ".arff";
			config =filesPath+ "dataset/" + args[1];// ".ini";
			TWSize = new Integer(args[2]);
			rPath = String.valueOf(args[3]);
			lagMax = String.valueOf(args[4]);
			season = String.valueOf(args[5]);
			exogen = String.valueOf(args[6]);
			ic = String.valueOf(args[7]);
			type = String.valueOf(args[8]);
			nahead = new Integer(args[9]);
		} catch (IndexOutOfBoundsException e) {
			String report = "TICT@KDDE.UNIBA.IT\n";
			report += "author = Annalisa Appice\n\n";
			report += "run jar by using the input parameters:\n";
			report += "streamfile configfile\n";
			report += "TWsize rPath lagMax season exogen ic type nahead \n";
			report += "streamfileTRAIN.arff is stored in a local directory named \'dataset\'\n";
			report += "the first snapshot contain the network structure\n";
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
		outputComputationTime = new PrintStream(new FileOutputStream(
				filesPath+"output/stream/report/ComputationTime/" + dataName + TWSize
						+ "_" + ic + "_" + type + "_CTime.csv"));
		outputComputationTime.print(";Learn VAR Model;Forecast\n");

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

		/*
		 * Crea la stringa per la denominazione del file di output .report
		 * (/output/stream/report)
		 */
		String name = "TWSize_" + TWSize + "_Type_"
				+ rParameters.get(index.type) + "_ic_"
				+ rParameters.get(index.ic);

		outputForecastReport = new OutputReport(nahead, rParameters, dataName, filesPath);

		SnapshotSchema schema = null;

		try {
			/*
			 * Inizio del software, qui passerà solamente una volta, la prima
			 */
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
					GregorianCalendar timeBegin = new GregorianCalendar();
					GregorianCalendar timeEnd = new GregorianCalendar();
					long timeLearnVARModel = 0;
					long timeForecast = 0;
					while (true) {
						try {
							// il primo snapshot contiene solo il network

							if (snapData == null) {
								/*
								 * Entrerà solo una volta, la prima, leggerà il
								 * network e creerà lo snapshot di network.
								 */
								snapData = new SnapshotData(inputStream, schema);
								System.out.println("ID Snapshot: " + snapData.getIdSnapshot());
								if (snapData.size() == 0)
									continue;

								snapData.sort();
								network.createWork(snapData, schema, TWSize);

								snapForecast = snapData.initialize(schema);

								VAROutput.inizializedOutputFiles(rParameters,
										schema, dataName, filesPath);

							} else {
								snapData = new SnapshotData(inputStream, schema);
								System.out.println("ID Snapshot: " + snapData.getIdSnapshot());
								if (snapData.size() == 0)
									continue;
								if (snapData.getIdSnapshot()==101){
									System.out.println("END");
									VAROutput.closeFiles();
									outputComputationTime.close();
									return;
								}

								snapData.sort();
								snapNetwork = snapData.mergeSnapshotData(
										snapForecast, schema);
								network.updateNetwork(snapNetwork, schema);

								if (snapData.getIdSnapshot() > TWSize) {
									timeBegin = new GregorianCalendar();
									network.learnVARModel(schema, rParameters);
									timeEnd = new GregorianCalendar();
									timeLearnVARModel = timeEnd
											.getTimeInMillis()
											- timeBegin.getTimeInMillis();

									
									System.out.println("modelli VAR :");
									for (int i: network){
										System.out.println("ID SENSORE: " + i);
										System.out.println(network.getVARModel(i));
									}
									
									timeBegin = new GregorianCalendar();
									Forecasting forecast = new Forecasting(
											nahead, schema, snapNetwork,
											network);
									timeEnd = new GregorianCalendar();
									timeForecast = timeEnd.getTimeInMillis()
											- timeBegin.getTimeInMillis();
									LinkedList<SnapshotData> res = forecast
											.forecasting();
									outputForecastReport.addForecast(res);
								}
								outputComputationTime.print(snapData
										.getIdSnapshot()
										+ ";"
										+ timeLearnVARModel
										+ ";"
										+ timeForecast + "\n");

								VAROutput.printAndReset(schema, String
										.valueOf(snapData.getIdSnapshot()), network);
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
			VAROutput.closeFiles();
			outputComputationTime.close();
			outputForecastReport.closeFiles();
		}

	}
}
