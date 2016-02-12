package rForecast;

import java.util.ArrayList;

import snapshot.SnapshotSchema;
import rForecast.ParametersRForecastIndex;
import rcaller.RCaller;
import rcaller.RCode;
import data.feature.Feature;

public class RVar extends RForecast {

	private String rPath;
	private String lagMax;
	private String type;
	private String season;
	private Object exogen;
	private String ic;
	private int TWsize = 2;
	private RCaller caller;
	private RCode code;

	@Override
	public ArrayList<Object> RForecasting(double[][] dataset,
			SnapshotSchema schema, ArrayList<Object> rParameters, int TWSize) {
		caller = new RCaller();
		code = new RCode();
		loadParameters(rParameters, TWSize);
		loadRLibrary();
		loadData(dataset, schema);
		executeVARSelect();
		executeVAR();
		caller.setRscriptExecutable(rPath);
		organizedata(schema);
		runandreturnresult();
		return null;
	}

	private void loadParameters(ArrayList<Object> rParameters, int TWSize) {
		ParametersRForecastIndex index = new ParametersRForecastIndex();
		this.rPath = (String) rParameters.get(index.rPath);
		this.lagMax = (String) rParameters.get(index.lagMax);
		this.type = (String) rParameters.get(index.type);
		this.season = (String) rParameters.get(index.season);
		if (rParameters.get(index.exogen) instanceof double[][])
			this.exogen = (double[][]) rParameters.get(index.exogen);
		else
			this.exogen = (String) rParameters.get(index.exogen);
		this.ic = (String) rParameters.get(index.ic);
		this.TWsize = TWSize;
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
		System.out.println(dimnames);
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
		System.out.println(VARSelect);
	}

	public void executeVAR() {
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

	private void organizedata(SnapshotSchema schema) {
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
		result += nomefeature + "_c = " + nomefeature + "_coeff)";
		code.addRCode(result);
	}

	private void runandreturnresult() {
		caller.setRCode(code);
		caller.runAndReturnResult("result");
		String[] k = caller.getParser().getAsStringArray("attr1_c");
		for (int i = 0; i < k.length; i++)
			System.out.println(k[i]);
	}
}
