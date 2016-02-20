package varUtility;

import java.util.ArrayList;

import forecast.FeatureVARForecastingModel;
import forecast.RecordVAR;

public class RMSEUtility {

	public Double computeRMSE(int p, Double[][] timeseries,
			FeatureVARForecastingModel VARModel) {
		double RMSE = 0.0;
		int beginIndexReducedMatrix = 0;
		int timeseriesRows = timeseries.length;
		int endIndexComputeRMSE = timeseriesRows - 1;
		int i = 0;
		double[][] reducedMatrix;
		double predicted = 0.0;
		double real = 0.0;
		for (i = p; i < endIndexComputeRMSE; i++) {
			reducedMatrix = reduceMatrix(timeseries, p, VARModel,
					beginIndexReducedMatrix, i - 1);
			predicted = predict(reducedMatrix, VARModel, p);
			real = timeseries[i][VARModel.getFeature().getFeatureIndex()];
			RMSE += Math.pow((real - predicted), 2);
			beginIndexReducedMatrix++;
		}
		RMSE /= (timeseriesRows - p);
		return RMSE;
	}

	private double[][] reduceMatrix(Double[][] timeseries, int p,
			FeatureVARForecastingModel VARModel, int beginIndexReducedMatrix,
			int endIndexReducedMatrix) {
		double[][] matrix = new double[p][timeseries[0].length];
		int indexMatrix = 0;
		for (int i = beginIndexReducedMatrix; i <= endIndexReducedMatrix; i++) {
			for (int j = 0; j < timeseries[0].length; j++) {
				matrix[indexMatrix][j] = timeseries[i][j];
			}
			indexMatrix++;
		}
		return matrix;
	}

	private double predict(double[][] reducedMatrix,
			FeatureVARForecastingModel VARModel, int p) {
		double predict = 0.0;
		for (RecordVAR record : VARModel) {
			ArrayList<Double> coeff = record.getCoefficients();
			ArrayList<Double> valReali = new ArrayList<Double>();
			for (int i = p-1; i >= 0; i--) {
				valReali.add(reducedMatrix[i][record.getFeature()
						.getFeatureIndex()]);
			}
			for (int i = 0; i < p; i++)
				predict += coeff.get(i) * valReali.get(i);
		}
		predict += VARModel.getCoefficientsConst()
				+ VARModel.getCoefficientsTrend();
		return predict;
	}
}
