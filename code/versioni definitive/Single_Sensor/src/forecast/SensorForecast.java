package forecast;

import data.feature.Feature;
import snapshot.SnapshotSchema;

public class SensorForecast {
	private SnapshotSchema schema;

	SensorForecast(SnapshotSchema schema) {
		this.schema = schema;
	}

	double[] sensorForecasting(double[][] timeSeries, ForecastingModel VARModel) {
		double[] result = new double[schema.getTargetList().size()];
		Double value = 0.0;
		for (Feature f : schema.getTargetList()) {
			FeatureForecastingModel fModel = VARModel
					.getFeatureForecastingModel(f);
			value = fModel.forecasting(timeSeries);
			result[f.getFeatureIndex()] = value;
		}
		return result;
	}
}
