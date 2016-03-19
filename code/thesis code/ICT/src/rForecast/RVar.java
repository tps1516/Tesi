package rForecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import snapshot.SnapshotSchema;
import rForecast.ParametersRForecastIndex;
import rcaller.RCaller;
import rcaller.RCode;
import data.feature.Feature;
import forecast.ForecastingModelIndex;

public class RVar extends RForecast {

	private String result;
	private String rPath;
	private String lagMax;
	private static String type;
	private String season;
	private Object exogen;
	private static String ic;
	private int TWsize;
	private RCaller caller;
	private RCode code;
	private static LinkedList<VARParameter> VARParameters;
	private ArrayList<Object> res;
	private static LinkedList<String> acceptableIc;
	private static LinkedList<String> acceptableType;
	private HashMap<String, ArrayList<Object>> hashResult;
	private int id;
	
	public static LinkedList<VARParameter> getVARParameters() {
		return VARParameters;
	}

	/*
	 * tale metodo ha la responsabilità di interfacciarsi con R per eseguire le
	 * funzioni VAR su tutte le combinazioni richieste e di restituire i
	 * risultati ottenuti dalla sua chiamata
	 */
	public HashMap<String, ArrayList<Object>> RForecasting(int id,double[][] dataset,
			SnapshotSchema schema, ArrayList<Object> rParameters) {
this.id=id;
		caller = new RCaller();
		code = new RCode();
		code.clear();

		// carico i parametri passati in input
		loadParameters(rParameters);

		// setto il path dello script R.exe
		caller.setRscriptExecutable(rPath);

		// costruisco tutte le combinazioni possibili in base ai parametri da
		// cambiare
		// loadCombinations(rParameters);

		// carico le librerie che servono per eseguire la funzione VAR
		loadRLibrary();

		// carico il dataset
		loadData(dataset, schema);

		result = "result <- list (";

		/*
		 * ciclo su tutte le possibili combinazioni per ottenere i risultati
		 * variando i parametri di input della funzione VAR
		 */
		for (int i = 0; i < VARParameters.size(); i++) {

			VARParameter comb = VARParameters.get(i);
			this.type = comb.getType();
			this.ic = comb.getIC();

			// aggiungo la funzione varSelect
			executeVARSelect();

			// aggiungo la funzione VAR
			executeVAR(comb.toString());

			/*
			 * richiamo i comandi R per ottenere i dati organizzati in modo tale
			 * sapere come poterli recuperare
			 */
			outputRSchemaSetup(schema, comb.toString());

			// chiudo la stringa di ritorno
			if (i == VARParameters.size() - 1)
				result += ")";
			else
				result += ",";
		}

		// aggiungo lo script R appena scritto
		code.addRCode(result);

		// eseguo lo script
		executeR();

		// costruisco l'oggetto java da restuituire recuperando i dati ottenuti
		// da R
		mapRTOJava(schema);

		return hashResult;
	}

	/*
	 * metodo privato ha la responsabilità di caricare i parametri passati in
	 * input per l'esecuzione della funzione VAR
	 */
	private void loadParameters(ArrayList<Object> rParameters) {
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		this.rPath = (String) rParameters.get(index.rPath);
		this.lagMax = (String) rParameters.get(index.lagMax);
		// this.type = (String) rParameters.get(index.type);
		this.season = (String) rParameters.get(index.season);
		if (rParameters.get(index.exogen) instanceof double[][])
			this.exogen = (double[][]) rParameters.get(index.exogen);
		else
			this.exogen = (String) rParameters.get(index.exogen);
		// this.ic = (String) rParameters.get(index.ic);
		this.TWsize = (int) rParameters.get(index.TWSize);
	}

	/*
	 * metodo privato ha la responsabilità di creare le diverse combinazioni al
	 * variare dei parametri "type" e "ic"
	 */
	public static void loadCombinations(ArrayList<Object> rParameters) {
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		VARParameters = new LinkedList<VARParameter>();

		/*
		 * cambiarli in static come costanti
		 */
		// ArrayList<String> acceptableType = new ArrayList<String>();
		// ArrayList<String> acceptableIc = new ArrayList<String>();

		// inizializzo l'array contenente tutti i valori di type ammessi
		// initializedAcceptableType(acceptableType);

		// inizializzo l'array contenete tutti i valori di ic ammessi
		// initializedAcceptableIc(acceptableIc);

		// recupero i valori di type e ic passati in input
		type = (String) rParameters.get(index.type);
		ic = (String) rParameters.get(index.ic);

		/*
		 * verifico quali combinazioni di type e ic sono state richieste se ic o
		 * type assumono il valore ALL allora vorrà dire che bisognerà
		 * effettuare la funzione VAR con tutti i valori ammessi da tali
		 * parametri
		 */
		if (type.equalsIgnoreCase("ALL") && ic.equalsIgnoreCase("ALL")) {

			/*
			 * in questo caso sia ic che type assumono valore ALL quindi bisogna
			 * creare 16 combinazioni variando entrambi i parametri
			 */

			for (String sType : acceptableType) {

				for (String sIc : acceptableIc) {
					VARParameter comb = new VARParameter(sType, sIc);
					VARParameters.add(comb);
				}

			}

		} else if (type.equalsIgnoreCase("ALL")) {

			/*
			 * in questo caso solo type ha valore ALL quindi si creano soltanto
			 * 4 combinazioni facendo variare solo il valore di type con ic
			 * fissato
			 */
			for (String sType : acceptableType) {
				VARParameter comb = new VARParameter(sType, ic);
				VARParameters.add(comb);
			}

		} else if (ic.equalsIgnoreCase("ALL")) {

			/*
			 * in questo caso solo ic ha valore ALL quindi si creano soltanto 4
			 * combinazioni facendo variare solo il valore di ic con type
			 * fissato
			 */
			for (String sIc : acceptableIc) {
				VARParameter comb = new VARParameter(type, sIc);
				VARParameters.add(comb);
			}

		} else {

			/*
			 * in questo caso ne type e ne ic hanno valore ALL quindi si crea
			 * una sola combinazione con entrambi i parametri già fissatti
			 * dall'utente
			 */
			VARParameter comb = new VARParameter(type, ic);
			VARParameters.add(comb);
		}
	}

	/*
	 * metodo privato aggiunge allo script R da eseguire il codice necessario
	 * per caricare tutte le librerie R necessarie per eseguire la funzione VAR
	 */
	private void loadRLibrary() {

		code.addRCode("pack1 <- require(MASS)");
		code.addRCode("pack2 <- require(sandwich)");
		code.addRCode("pack3 <- require(base)");
		code.addRCode("pack4 <- require(zoo)");
		code.addRCode("pack5 <- require(strucchange)");
		code.addRCode("pack6 <- require(lmtest)");
		code.addRCode("pack7 <- require(urca)");
		code.addRCode("pack8 <- require(vars)");
		code.addRCode("pack8 <- require(Runiversal)");
	}

	/*
	 * metodo privato ha la responsabilità di aggiungere allo script R il codice
	 * per caricare il dataset su cui effettuare la funzione VAR
	 */
	private void loadData(double[][] dataset, SnapshotSchema s) {
		if (id==1){
			code.addDoubleMatrix("dataset", dataset);
			String dimnames = "dimnames(dataset) <- list (c (";
			for (int i = 1; i < this.TWsize; i++)
				dimnames += "\"t" + i + "\",";
			dimnames += "\"t" + TWsize + "\"),c(";
			for (int i = 0; i < s.getTargetList().size() - 1; i++)
				dimnames += "\"" + s.getTargetList().get(i).getName() + "\",";
			dimnames += "\""
					+ s.getTargetList().get(s.getTargetList().size() - 1).getName()
					+ "\"))";
			code.addRCode(dimnames);
		}else {
			code.addDoubleMatrix("dataset", dataset);
			String dimnames = "dimnames(dataset) <- list (c (";
			for (int i = 1; i < this.TWsize; i++)
				dimnames += "\"t" + i + "\",";
			dimnames += "\"t" + TWsize + "\"),c(";
			for (int i = 0; i < s.getTargetList().size() - 1; i++)
				dimnames += "\"" + s.getTargetList().get(i).getName() + "\",";
			dimnames += "\""
					+ s.getTargetList().get(s.getTargetList().size() - 1).getName()
					+ "\"))";
			code.addRCode(dimnames);
		}

		
	}

	/*
	 * metodo privato ha la responsabilità di aggiungere allo script R il codice
	 * per eseguire la funzione varSelect
	 */
	private void executeVARSelect() {
		if (exogen instanceof double[][])
			code.addDoubleMatrix("exo", (double[][]) this.exogen);
		String VARSelect = "varNuova<-VARselect(dataset, lag.max = "
				+ this.lagMax + ", type = \"" + this.type + "\", season = "
				+ this.season + ",";
		if (exogen instanceof String)
			VARSelect += " exogen = NULL)";
		else
			VARSelect += " exo)";
		code.addRCode(VARSelect);
	}

	/*
	 * metodo privato ha la responsabilità di aggiungere allo script R il codice
	 * per eseguire la funzione VAR
	 */
	private void executeVAR(String comb) {
		String p = "";

		/*
		 * verifico che ic verrà utilizzato per ottenere il p adeguato
		 */
		switch (ic) {
		case ("AIC"): {
			p = "1";
			break;
		}
		case ("HQ"): {
			p = "2";
			break;
		}
		case ("SC"): {
			p = "3";
			break;
		}
		case ("FPE"): {
			p = "4";
			break;
		}
		}

		// recupero il p adeguato
		String orderp = comb + "_orderp <- as.numeric(varNuova$selection[" + p
				+ "])";

		code.addRCode(orderp);
	}

	/*
	 * metodo privato ha la responsabilità di aggiungere il codice R per
	 * organizzare i dati ottenuti dall'esecuzione della funzione VAR da
	 * restituire a java
	 */
	private void outputRSchemaSetup(SnapshotSchema schema, String comb) {

		for (Feature f : schema.getTargetList()) {
			String nomefeature = f.getName();
			code.addRCode(nomefeature + "_" + comb + "_nomifeature <- 0");
			code.addRCode(nomefeature + "_" + comb + "_coeff <- 0");
		}

		String p = comb + "_orderp";

		code.addRCode("if(!is.na(" + p + ")){");

		String VAR = "var <- VAR(dataset, p = " + comb + "_orderp, type = \""
				+ this.type + "\", ic = \"" + this.ic + "\")";

		code.addRCode(VAR);

		/*
		 * per ciascuna feature dello schema ottengo un array di stringhe che
		 * sono le feature correlate alla feature in esame l'insieme di tutti i
		 * coefficienti per predire tale feature per disambiguare tutti gli
		 * array di stringhe ciascun array verrà chiamato nel seguente modo:
		 * nomeDellaFeatureAcuiFaRiferimento_combinazioneDeiParametriConCuiEStatoOttenuto_nomifeature
		 * per disambiguare tutti gli array di coefficienti ciascun array verrà
		 * chiamato nel seguente modo:
		 * nomeDellaFeatureAcuiFaRiferimento_combinazioneDeiParametriConCuiEStatoOttenuto_coeff
		 */
		for (Feature f : schema.getTargetList()) {
			// PRELEVO I NOMI DELLE FEATURE CORRELATE
			String nomefeature = f.getName();
			code.addRCode("dataframe <- data.frame(var$varresult$"
					+ nomefeature + "$coefficients)");
			code.addRCode("nomifeature <- dimnames(dataframe)");
			code.addRCode(nomefeature + "_" + comb
					+ "_nomifeature <- nomifeature[[1]]");

			// PRELEVO I VALORI DEI COEFFICIENTI
			code.addRCode(nomefeature + "_" + comb
					+ "_coeff <- as.numeric(var$varresult$" + nomefeature
					+ "$coefficients)");
		}

		code.addRCode("} else " + p + " = 0");

		/*
		 * salvo tutti gli array in una sola lista che sarà restituita a JAVA
		 * assegnando ciascun array ad una variabile nella lista
		 */
		for (int i = 0; i < schema.getTargetList().size() - 1; i++) {
			String nomefeature = schema.getTargetList().get(i).getName();
			result += nomefeature + "_" + comb + "_f = " + nomefeature + "_"
					+ comb + "_nomifeature, ";
			result += nomefeature + "_" + comb + "_c = " + nomefeature + "_"
					+ comb + "_coeff, ";
		}

		String nomefeature = schema.getTargetList()
				.get(schema.getTargetList().size() - 1).getName();
		result += nomefeature + "_" + comb + "_f = " + nomefeature + "_" + comb
				+ "_nomifeature, ";
		result += nomefeature + "_" + comb + "_c = " + nomefeature + "_" + comb
				+ "_coeff, p_" + comb + " = " + comb + "_orderp";
	}

	/*
	 * eseguo lo script R
	 */
	private void executeR() {
		caller.setRCode(code);
		caller.runAndReturnResult("result");
	}

	/*
	 * metodo privato serve per avvalorare l'oggetto da restituire recupera i
	 * risultati ottenuti da R e li inserisce raggruppati per combinazioni e per
	 * feature nell'hashmap
	 */
	private void mapRTOJava(SnapshotSchema schema) {
		hashResult = new HashMap<String, ArrayList<Object>>();

		/*
		 * ciclo su tutte le combinazioni presenti
		 */
		for (VARParameter comb : VARParameters) {
			res = new ArrayList<Object>();
			// recupero il valore di p per la specifica combinazione
			int p = 0;
			p = caller.getParser().getAsIntArray("p_" + comb)[0];
			if (p != 0) {
				/*
				 * istanzio l'arraylist di arraylist delle feature correlate
				 */
				ArrayList<ArrayList<Feature>> correlatedFeatures = new ArrayList<ArrayList<Feature>>(
						schema.getTargetList().size());

				/*
				 * istanzio l'arrayList di arrayList di arrayList di double dei
				 * coefficienti
				 */
				ArrayList<ArrayList<ArrayList<Double>>> correlatedCoefficients = new ArrayList<ArrayList<ArrayList<Double>>>();

				/*
				 * avvaloro l'hashmap ausiliario che associa a ciascun nome la
				 * feature a cui fa riferimento
				 */
				HashMap<String, Feature> HM = populateFeatureMap(schema);

				ArrayList<Double> listCoefTrend = new ArrayList<Double>();
				ArrayList<Double> listCoefConst = new ArrayList<Double>();

				/*
				 * dall'array delle feature restitioto da R occorre eliminare
				 * gli eventuali attributi "const" e "trend" presenti presneti
				 * in base al valore di type utilizzato essi si trovano nelle
				 * ultime posizioni dell'array delle feature e bisogna spostare
				 * dall'array di coefficienti gli eventuali coefficieni di trend
				 * e di const sempre presenti nelle ultime posizioni con questo
				 * ciclo su tutt ele feature effettuo queste operazioni di
				 * rimozione e spostamento
				 */
				for (Feature f : schema.getTargetList()) {
					double c_trend = 0.0;
					double c_const = 0.0;

					/*
					 * recupero l'array delle feature correlate per tale
					 * combinazione e tale feature
					 */
					String[] feature = caller.getParser().getAsStringArray(
							f.getName() + "_" + comb + "_f");

					/*
					 * recupero l'array dei coefficienti correlati per tale
					 * combinazione e tale feature
					 */
					String[] coeff = caller.getParser().getAsStringArray(
							f.getName() + "_" + comb + "_c");

					/*
					 * in base alla specifica combinazione effettuo le
					 * operazioni sopracitate
					 */
					switch (comb.getType()) {
					case ("both"): {
						if (!(coeff[coeff.length - 1].equals("NA")))
							c_trend = Double
									.parseDouble(coeff[coeff.length - 1]);
						if (!(coeff[coeff.length - 2].equals("NA")))
							c_const = Double
									.parseDouble(coeff[coeff.length - 2]);
						String[] aux = new String[feature.length - 2];
						for (int i = 0; i < feature.length - 2; i++)
							aux[i] = feature[i];
						feature = aux;
						String[] c_aux = new String[coeff.length - 2];
						for (int i = 0; i < coeff.length - 2; i++)
							c_aux[i] = coeff[i];
						coeff = c_aux;
						break;
					}
					case ("trend"): {
						if (!(coeff[coeff.length - 1].equals("NA")))
							c_trend = Double
									.parseDouble(coeff[coeff.length - 1]);
						c_const = 0.0;
						String[] aux = new String[feature.length - 1];
						for (int i = 0; i < feature.length - 1; i++)
							aux[i] = feature[i];
						feature = aux;
						String[] c_aux = new String[coeff.length - 1];
						for (int i = 0; i < coeff.length - 1; i++)
							c_aux[i] = coeff[i];
						coeff = c_aux;
						break;
					}
					case ("const"): {
						if (!(coeff[coeff.length - 1].equals("NA")))
							c_const = Double
									.parseDouble(coeff[coeff.length - 1]);
						c_trend = 0.0;
						String[] aux = new String[feature.length - 1];
						for (int i = 0; i < feature.length - 1; i++)
							aux[i] = feature[i];
						feature = aux;
						String[] c_aux = new String[coeff.length - 1];
						for (int i = 0; i < coeff.length - 1; i++)
							c_aux[i] = coeff[i];
						coeff = c_aux;
						break;
					}
					case ("none"): {
						c_trend = 0.0;
						c_const = 0.0;
					}
					}

					/*
					 * aggiungo negli arrayList dei coefficienti di trend e di
					 * const i coefficienti appena recuperati memorizzandoli
					 * nella corretta posizione e cioè in base alla feature a
					 * cui fanno riferimento
					 */
					listCoefTrend.add(f.getFeatureIndex(), c_trend);
					listCoefConst.add(f.getFeatureIndex(), c_const);

					/*
					 * memorizzo le feateure correlate alla specifica featurein
					 * esame solo una volta(erano memorizzate p volte)
					 */
					String[] adjfeature = featureEpure(feature, p);

					ArrayList<Feature> correlatedFeature = new ArrayList<Feature>();
					ArrayList<ArrayList<Double>> coefficientsByFeature = new ArrayList<ArrayList<Double>>();

					/*
					 * creo l'arrayList finale di feature correlate per la
					 * determinata feature e creo per ciascuna feature correlata
					 * un arrayList contentente tutti i coefficienti associati a
					 * quella feature correlata
					 */
					for (int i = 0; i < adjfeature.length; i++) {

						/*
						 * tramite l'hashMap ausiliario recupero l'intera
						 * feature in base al suo nome
						 */
						correlatedFeature.add((Feature) HM.get(adjfeature[i])
								.clone());
						ArrayList<Double> coefficients = new ArrayList<Double>(
								p);

						/*
						 * setto l'arrayList di coefficienti associati alla
						 * specifica feature correlata
						 */
						coefficients = addCoefficients(adjfeature.length, i,
								coeff);

						/*
						 * aggiungo l'arrayList di coefficienti all'arrayList
						 * generale dei coefficienti associati per la
						 * determinata feature
						 */
						coefficientsByFeature.add(coefficients);
					}

					/*
					 * al termine del ciclo relativo ad una specifica feature
					 * memorizzo il suo relativo arrayList di feature correlate
					 * e il suo relativo arrayList di array list di coefficienti
					 * correlati memorizzandoli nei rispettivi arraylist e nelle
					 * posizioni corrette in base all'indice della feature
					 */
					correlatedCoefficients.add(f.getFeatureIndex(),
							coefficientsByFeature);
					correlatedFeatures.add(f.getFeatureIndex(),
							correlatedFeature);

				}

				/*
				 * al termine del ciclo sulla determinata combinazione memorizzo
				 * tutti i risultati ottenuti e appena organizzati nelle
				 * posizioni corrette dell'arrayList di object e infine aggiungo
				 * tale arrayList nell'hashMap da restituire utilizzando come
				 * chiave la combinazione a cui esso fa riferimento
				 */
				ForecastingModelIndex index = new ForecastingModelIndex();
				res.add(index.feature, correlatedFeatures);
				res.add(index.coefficients, correlatedCoefficients);
				res.add(index.p, p);
				res.add(index.trend, listCoefTrend);
				res.add(index.cost, listCoefConst);
				hashResult.put(comb.toString(), res);
			} else
				hashResult.put(comb.toString(), null);
		}
	}

	/*
	 * metodo privato ha la responsabilità di associare a ciascuna stringa
	 * rappresentate il nome di una feature, la feature a cui fa riferimento
	 * utilizzando un hashMap
	 */
	private HashMap<String, Feature> populateFeatureMap(SnapshotSchema s) {
		HashMap<String, Feature> HM = new HashMap<String, Feature>();
		for (Feature f : s.getTargetList())
			HM.put(f.getName(), f);
		return HM;
	}

	/*
	 * metodo privato ha la responsabilità di restituire un array di string
	 * contente le feature correlate una sola volta invece di p volte e
	 * eliminando la sottostringa ".l1"
	 */
	private String[] featureEpure(String[] feature, int p) {
		String[] ret = new String[feature.length / p];
		int i;
		for (i = 0; i < ret.length; i++)
			ret[i] = feature[i].replace(".l1", "");
		return ret;
	}

	/*
	 * metodo privato ha la responsabilità, data la posizione della feature, di
	 * recuperare tutti i coefficienti associati a tale feature dall'array
	 * generale di coefficienti restituito da R
	 */
	private ArrayList<Double> addCoefficients(int step, int from, String[] coeff) {
		ArrayList<Double> ret = new ArrayList<Double>();
		for (int j = from; j < coeff.length; j = j + step) {
			if (coeff[j].equals("NaN") || coeff[j].equals("NA")
					|| coeff[j].equals("Null"))
				ret.add(0.0);
			else
				ret.add(Double.valueOf(coeff[j]));
		}
		return ret;
	}

	public static void initializedAcceptableType() {
		acceptableType = new LinkedList<String>();
		acceptableType.add("const");
		acceptableType.add("trend");
		acceptableType.add("both");
		acceptableType.add("none");
	}

	public static void initializedAcceptableIc() {
		acceptableIc = new LinkedList<String>();
		acceptableIc.add("AIC");
		acceptableIc.add("HQ");
		acceptableIc.add("SC");
		acceptableIc.add("FPE");
	}
}
