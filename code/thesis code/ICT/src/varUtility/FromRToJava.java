package varUtility;

import java.util.ArrayList;
import java.util.HashMap;

import data.feature.Feature;
import snapshot.SnapshotSchema;
import forecast.FeatureForecastingModel;
import forecast.FeatureVARForecastingModel;
import forecast.ForecastingModel;
import forecast.ForecastingModelIndex;

public class FromRToJava {

	public HashMap<String, ForecastingModel> converter(
			HashMap<String, ArrayList<Object>> hm, SnapshotSchema schema,
			Double[][] timeseries) {
		ForecastingModelIndex index = new ForecastingModelIndex();
		HashMap<String, ForecastingModel> hmResult = new HashMap<String, ForecastingModel>();

		for (String combinations : hm.keySet()) {
			ArrayList<FeatureForecastingModel> model = new ArrayList<FeatureForecastingModel>(
					schema.getTargetList().size());
			ArrayList<Object> dataFromR = hm.get(combinations);
			ArrayList<ArrayList<Feature>> listOfCorrelatedFeature = (ArrayList<ArrayList<Feature>>) dataFromR
					.get(index.feature);
			ArrayList<ArrayList<ArrayList<Double>>> listOfCorrelatedCoefficients = (ArrayList<ArrayList<ArrayList<Double>>>) dataFromR
					.get(index.coefficients);

			int p = (int) dataFromR.get(index.p);
			ArrayList<Double> coefTrend = (ArrayList<Double>) dataFromR
					.get(index.trend);
			ArrayList<Double> coefConst = (ArrayList<Double>) dataFromR
					.get(index.cost);

			for (Feature f : schema.getTargetList()) {
				int i = f.getFeatureIndex();
				FeatureForecastingModel VARModel = new FeatureVARForecastingModel(
						f, listOfCorrelatedFeature.get(i),
						listOfCorrelatedCoefficients.get(i), p, timeseries,
						coefConst.get(i), coefTrend.get(i));
				model.add(i, VARModel);
			}

			ForecastingModel fm = new ForecastingModel(model);
			hmResult.put(combinations, fm);

		}

		return hmResult;
	}

}
