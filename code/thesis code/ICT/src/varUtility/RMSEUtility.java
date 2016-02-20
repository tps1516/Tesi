package varUtility;

import forecast.FeatureVARForecastingModel;

public class RMSEUtility {

	public Double computeRMSE(int p, Double[][] timeseries,
			FeatureVARForecastingModel VARModel) {
		double RMSE = 0.0;
		int beginIndexReducedMatrix = 0;
		int timeseriesRows = timeseries[0].length;
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
			int i) {
		double[][] matrix = new double[p][timeseries.length];
		return null;
	}

	private double predict(double[][] reducedMatrix,
			FeatureVARForecastingModel VARModel, int p) {
		return 0.0;
	}
}
