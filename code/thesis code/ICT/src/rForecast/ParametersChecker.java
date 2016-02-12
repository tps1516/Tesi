package rForecast;

import java.util.ArrayList;

public class ParametersChecker {

	private ArrayList<String> acceptableType = new ArrayList<String>();
	private ArrayList<String> acceptableIc = new ArrayList<String>();
	private int maxLagMax;

	public ParametersChecker(int maxLagMax) {
		this.maxLagMax = maxLagMax;
		initializedAcceptableType();
		initializedAcceptableIc();
	}

	private void initializedAcceptableType() {
		this.acceptableType.add("const");
		this.acceptableType.add("trend");
		this.acceptableType.add("both");
		this.acceptableType.add("none");
	}

	private void initializedAcceptableIc() {
		this.acceptableIc.add("AIC");
		this.acceptableIc.add("HQ");
		this.acceptableIc.add("SC");
		this.acceptableIc.add("FPE");
	}

	public void check(ArrayList<Object> rParameters) {
		ParametersRForecastIndex index = new ParametersRForecastIndex();

		if (rParameters.get(index.rPath) == ""
				|| rParameters.get(index.rPath) == null) {
			throw new InvalidForecastParametersException(
					"Inserire il path di RScript!");
		}

		int lagMax;
		try {
			lagMax = Integer.parseInt((String) rParameters.get(index.lagMax));
		} catch (Exception e) {
			throw new InvalidForecastParametersException(
					"Inserire un lagMax valido!");
		}
		if (lagMax > this.maxLagMax) {
			throw new InvalidForecastParametersException(
					"Inserire un lagMax valido!");
		}

		int season;
		if (!rParameters.get(index.season).equals("NULL")) {
			try {
				season = Integer.parseInt((String) rParameters
						.get(index.season));
			} catch (Exception e) {
				throw new InvalidForecastParametersException(
						"Inserire un valore di season valido!");
			}
			if ((season < 0) || (season > 12)) {
				throw new InvalidForecastParametersException(
						"Inserire un valore di season valido!");
			}
		}

		if (!rParameters.get(index.exogen).equals("NULL")) {
			if (!(rParameters.get(index.exogen) instanceof Double[][]))
				throw new InvalidForecastParametersException(
						"Inserire un dataset di variabili esogene valido!");
		}

		if (!acceptableIc.contains(rParameters.get(index.ic)))
			throw new InvalidForecastParametersException(
					"Inserire un valore di 'ic' valido!");

		if (!acceptableType.contains(rParameters.get(index.type)))
			throw new InvalidForecastParametersException(
					"Inserire un valore valido per il parametro 'type'!");
	}
}
