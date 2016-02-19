package rForecast;

import java.util.ArrayList;
import java.util.HashMap;

import snapshot.SnapshotSchema;
import rForecast.ParametersRForecastIndex;
import rcaller.RCaller;
import rcaller.RCode;
import data.feature.Feature;
import forecast.ForecastingModelIndex;

public class RVar extends RForecast {

	private String rPath;
	private String lagMax;
	private String type;
	private String season;
	private Object exogen;
	private String ic;
	private int TWsize;
	private RCaller caller;
	private RCode code;
	private ArrayList<Combination> combinations;
	private ArrayList<Object> res;

	@Override
	public ArrayList<Object> RForecasting(double[][] dataset,
			SnapshotSchema schema, ArrayList<Object> rParameters) {
		caller = new RCaller();
		code = new RCode();
		
		loadParameters(rParameters);
		loadCombinations(rParameters);
		caller.setRscriptExecutable(rPath);
		for (Combination c: combinations){
			System.out.println(c);
		}
		loadRLibrary();
		loadData(dataset, schema);
		for (Combination comb: combinations){
			this.type=comb.getType();
			this.ic=comb.getIC();
			executeVARSelect();
			executeVAR();
			organizeRdata(schema);
		}
		executeR();
		mapRTOJava(schema);
		return res;
	}

	private void loadParameters(ArrayList<Object> rParameters) {
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		this.rPath = (String) rParameters.get(index.rPath);
		this.lagMax = (String) rParameters.get(index.lagMax);
		//this.type = (String) rParameters.get(index.type);
		this.season = (String) rParameters.get(index.season);
		if (rParameters.get(index.exogen) instanceof double[][])
			this.exogen = (double[][]) rParameters.get(index.exogen);
		else
			this.exogen = (String) rParameters.get(index.exogen);
		//this.ic = (String) rParameters.get(index.ic);
		this.TWsize = (int) rParameters.get(index.TWSize);
	}

	
	private void loadCombinations(ArrayList<Object> rParameters){
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		this.combinations=new ArrayList<Combination>();
		ArrayList<String> acceptableType = new ArrayList<String>();
		ArrayList<String> acceptableIc = new ArrayList<String>();
		initializedAcceptableType(acceptableType);
		initializedAcceptableIc(acceptableIc);
		type = (String) rParameters.get(index.type);
		ic = (String) rParameters.get(index.ic);
		if (type.equalsIgnoreCase("ALL") && ic.equalsIgnoreCase("ALL")){
			
			for (String sType: acceptableType){
				
				for (String sIc: acceptableIc){
					Combination comb = new Combination(sType,sIc);
					combinations.add(comb);
				}
				
			}
			
		} else if (type.equalsIgnoreCase("ALL")) {
			
			for (String sType: acceptableType){
				Combination comb=new Combination(sType,ic);
				combinations.add(comb);
			}
			
		} else if (ic.equalsIgnoreCase("ALL")){
			
			for (String sIc: acceptableIc){
				Combination comb=new Combination(type,sIc);
				combinations.add(comb);
			}
			
		} else {
			Combination comb= new Combination(type,ic);
			combinations.add(comb);
		}
	}
	
	
	private void loadRLibrary() {
		code.clear();
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

	private void loadData(double[][] dataset, SnapshotSchema s) {

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

	private void executeVAR() {
		String p = "";
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
		String orderp = "orderp <- as.numeric(varNuova$selection[" + p + "])";
		code.addRCode(orderp);
		String VAR = "var <- VAR(dataset, p = orderp, type = \"" + this.type
				+ "\", ic = \"" + this.ic + "\")";
		code.addRCode(VAR);
	}

	private void organizeRdata(SnapshotSchema schema) {
		for (Feature f : schema.getTargetList()) {
			// PRELEVO I NOMI DELLE FEATURE CORRELATE
			String nomefeature = f.getName();
			code.addRCode("dataframe <- data.frame(var$varresult$"
					+ nomefeature + "$coefficients)");
			code.addRCode("nomifeature <- dimnames(dataframe)");
			code.addRCode(nomefeature + "_nomifeature <- nomifeature[[1]]");

			// PRELEVO I VALORI DEI COEFFICIENTI
			code.addRCode(nomefeature + "_coeff <- as.numeric(var$varresult$"
					+ nomefeature + "$coefficients)");
		}
		String result = "result <- list (";
		for (int i = 0; i < schema.getTargetList().size() - 1; i++) {
			// SALVO I VALORI IN UNA SOLA LISTA
			String nomefeature = schema.getTargetList().get(i).getName();
			result += nomefeature + "_f = " + nomefeature + "_nomifeature, ";
			result += nomefeature + "_c = " + nomefeature + "_coeff, ";
		}
		String nomefeature = schema.getTargetList()
				.get(schema.getTargetList().size() - 1).getName();
		result += nomefeature + "_f = " + nomefeature + "_nomifeature, ";
		result += nomefeature + "_c = " + nomefeature + "_coeff, p = var$p)";
		code.addRCode(result);
	}

	private void executeR() {
		caller.setRCode(code);
		caller.runAndReturnResult("result");
	}

	private void mapRTOJava(SnapshotSchema schema) {
		res = new ArrayList<Object>();
		ArrayList<ArrayList<Feature>> correlatedFeatures = new ArrayList<ArrayList<Feature>>(
				schema.getTargetList().size());
		ArrayList<ArrayList<ArrayList<Double>>> correlatedCoefficients = new ArrayList<ArrayList<ArrayList<Double>>>();
		HashMap<String, Feature> HM = populateFeatureMap(schema);
		for (Feature f : schema.getTargetList()) {
			String[] feature = caller.getParser().getAsStringArray(
					f.getName() + "_f");
			String[] coeff = caller.getParser().getAsStringArray(
					f.getName() + "_c");
			int p = caller.getParser().getAsIntArray("p")[0];
			String[] adjfeature = featureEpure(feature, p);
			ArrayList<Feature> correlatedFeature = new ArrayList<Feature>();
			ArrayList<ArrayList<Double>> coefficientsByFeature = new ArrayList<ArrayList<Double>>();
			for (int i = 0; i < adjfeature.length; i++) {
				correlatedFeature.add((Feature) HM.get(adjfeature[i]).clone());
				ArrayList<Double> coefficients = new ArrayList<Double>(p);
				coefficients = addCoefficients(adjfeature.length, i, coeff);
				coefficientsByFeature.add(coefficients);
			}
			correlatedCoefficients.add(f.getIndexMining()
					- schema.getSpatialList().size(), coefficientsByFeature);
			correlatedFeatures.add(f.getIndexMining()
					- schema.getSpatialList().size(), correlatedFeature);

		}

		ForecastingModelIndex index = new ForecastingModelIndex();
		res.add(index.feature, correlatedFeatures);
		res.add(index.coefficients, correlatedCoefficients);
	}

	private HashMap<String, Feature> populateFeatureMap(SnapshotSchema s) {
		HashMap<String, Feature> HM = new HashMap<String, Feature>();
		for (Feature f : s.getTargetList())
			HM.put(f.getName(), f);
		return HM;
	}

	private String[] featureEpure(String[] feature, int p) {
		String[] ret = new String[feature.length / p];
		int i;
		for (i = 0; i < ret.length; i++)
			ret[i] = feature[i].replace(".l1", "");
		return ret;
	}

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
	
	
	private void initializedAcceptableType(ArrayList<String> acceptableType) {
		acceptableType.add("const");
		acceptableType.add("trend");
		acceptableType.add("both");
		acceptableType.add("none");
	}

	private void initializedAcceptableIc(ArrayList<String> acceptableIc) {
		acceptableIc.add("AIC");
		acceptableIc.add("HQ");
		acceptableIc.add("SC");
		acceptableIc.add("FPE");
	}
}
