package varUtility;

import java.util.ArrayList;
import java.util.HashMap;
import data.feature.Feature;
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
			HashMap<String, Double> hashRMSEFeatureByParameters = VAROutput.counterRMSE
					.get(f.getName());
			for (String comb : hm.keySet()) {
				ForecastingModel model = hm.get(comb);

				if (model == null) {
					hashRMSEFeatureByParameters.put(comb, null);
				} else {

					FeatureVARForecastingModel featureModel = (FeatureVARForecastingModel) model
							.getFeatureForecastingModel(f);
					if (featureModel.getRMSE() < min) {
						min = featureModel.getRMSE();
						FeatureDefModel = featureModel;
					}

					Double precRMSE = hashRMSEFeatureByParameters.get(comb);

					if (precRMSE != null) {
						hashRMSEFeatureByParameters.put(comb, precRMSE
								+ featureModel.getRMSE());
					}
				}
			}
			VAROutput.counterRMSE.put(f.getName(), hashRMSEFeatureByParameters);
			optModel.add(FeatureDefModel.getFeature().getFeatureIndex(),
					FeatureDefModel);
		}
		return optModel;
	}

}
