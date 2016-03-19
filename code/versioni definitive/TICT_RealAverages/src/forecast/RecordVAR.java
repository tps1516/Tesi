package forecast;

import java.util.ArrayList;
import java.io.Serializable;
import data.feature.Feature;

public class RecordVAR implements Serializable {
	private Feature feature;
	private ArrayList<Double> coefficients;

	RecordVAR(Feature f, ArrayList<Double> coef) {
		this.feature = f;
		this.coefficients = coef;
	}

	public String toString() {
		String str = "";

		str += feature.getName() + ":";
		for (Double coef : coefficients) {
			str += " " + coef;
		}

		return str;
	}

	public Feature getFeature() {
		return this.feature;
	}

	public ArrayList<Double> getCoefficients() {
		return this.coefficients;
	}
}
