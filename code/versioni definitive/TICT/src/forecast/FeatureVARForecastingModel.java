package forecast;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.Iterator;
import varUtility.RMSEUtility;
import data.feature.Feature;

public class FeatureVARForecastingModel extends FeatureForecastingModel
		implements Iterable<RecordVAR>, Serializable {

	private ArrayList<RecordVAR> equationModel;
	private int p;
	private Double coefficientsTrend;
	private Double coefficientsConst;
	private Double RMSE;
	private String VARParameters;

	public String getVARParameters() {
		return this.VARParameters;
	}

	public FeatureVARForecastingModel(Feature f,
			ArrayList<Feature> correlatedFeature,
			ArrayList<ArrayList<Double>> coefficients, int p,
			Double[][] timeseries, Double coefConst, Double coefTrend,
			String varPar) {
		super(f);
		initializesEquationModel(correlatedFeature, coefficients);
		this.p = p;
		this.coefficientsConst = coefConst;
		this.coefficientsTrend = coefTrend;
		this.VARParameters = varPar;
		setRMSE(timeseries);
	}

	public String toString() {
		String str = "Feature: " + super.feature.getName() + "\n"
				+ "equation: " + "\n";
		str += "Comb: " + this.VARParameters + "\n";
		str += "p= " + this.p + "\n";
		for (RecordVAR r : this) {
			str += r.toString() + "\n";
		}
		str += "Trend coefficients: " + this.coefficientsTrend + "\n";
		str += "Const coefficients: " + this.coefficientsConst + "\n";
		str += "RMSE: " + this.RMSE + "\n";

		return str;
	}

	private void initializesEquationModel(ArrayList<Feature> correlatedFeature,
			ArrayList<ArrayList<Double>> coefficients) {
		this.equationModel = new ArrayList<RecordVAR>();
		for (int i = 0; i < correlatedFeature.size(); i++) {
			RecordVAR record = new RecordVAR(correlatedFeature.get(i),
					coefficients.get(i));
			this.equationModel.add(record);
		}
	}

	@Override
	public Iterator<RecordVAR> iterator() {
		return this.equationModel.iterator();
	}

	public int getP() {
		return this.p;
	}

	public Double getCoefficientsTrend() {
		return this.coefficientsTrend;
	}

	public Double getCoefficientsConst() {
		return this.coefficientsConst;
	}

	private void setRMSE(Double[][] timeseries) {
		RMSEUtility rmsUtilty = new RMSEUtility();
		this.RMSE = rmsUtilty.computeRMSE(timeseries, this);
	}

	public Double getRMSE() {
		return this.RMSE;
	}

	@Override
	public Double forecasting(double[][] timeSeries)
			throws NotForecastingException {
		Double sum = 0.0;
		Feature f;
		int i;
		int indexColumn;
		Double value;
		ArrayList<Double> coefficients;
		for (RecordVAR record : this.equationModel) {
			i = timeSeries.length - 1;
			f = record.getFeature();
			indexColumn = f.getFeatureIndex();
			coefficients = record.getCoefficients();
			for (Double coeff : coefficients) {
				value = timeSeries[i][indexColumn];
				if (value.equals(Double.MAX_VALUE))
					throw new NotForecastingException("Sensore non predicibile");
				sum += value * coeff;
				i = i - 1;
			}
		}
		sum += this.coefficientsConst + this.coefficientsTrend;
		return sum;
	}

}
