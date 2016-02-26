package varUtility;

import java.util.ArrayList;
import java.util.HashMap;

import data.feature.*;
import snapshot.SnapshotSchema;
import forecast.FeatureForecastingModel;
import forecast.FeatureVARForecastingModel;
import forecast.ForecastingModel;

public class OptimalVARModel {

	public ArrayList<FeatureForecastingModel> computeOptimalVARModel(
			HashMap<String, ForecastingModel> hm, SnapshotSchema schema) {
		ArrayList<FeatureForecastingModel> optModel = new ArrayList<FeatureForecastingModel>();
		double min;
		FeatureVARForecastingModel FeatureDefModel = null;

		for (Feature f : schema.getTargetList()) {
			min = Double.MAX_VALUE;
			FeatureDefModel = null;
			for (String comb : hm.keySet()) {
				ForecastingModel model = hm.get(comb);
				FeatureVARForecastingModel featureModel = (FeatureVARForecastingModel) model
						.getFeatureForecastingModel(f);
				if (featureModel.getRMSE() < min) {
					min = featureModel.getRMSE();
					FeatureDefModel = featureModel;
				}

				ArrayList<Double> rmse = VAROutput.counterRMSE.get(comb);
				Double precRMSE = rmse.get(f.getFeatureIndex());
				rmse.set(f.getFeatureIndex(), precRMSE + featureModel.getRMSE());
				VAROutput.counterRMSE.put(comb.toString(), rmse);
			}
			optModel.add(FeatureDefModel.getFeature().getFeatureIndex(),
					FeatureDefModel);
		}
		return optModel;
	}

}
