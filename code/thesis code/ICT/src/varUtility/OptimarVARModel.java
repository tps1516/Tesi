package varUtility;

import java.util.ArrayList;
import java.util.HashMap;

import data.feature.*;
import snapshot.SnapshotSchema;
import forecast.FeatureForecastingModel;
import forecast.FeatureVARForecastingModel;
import forecast.ForecastingModel;

public class OptimarVARModel {

	public ArrayList<FeatureForecastingModel> computOptimalVARModel(
			HashMap<String, ForecastingModel> hm, SnapshotSchema schema) {
		ArrayList<FeatureForecastingModel> optModel = new ArrayList<FeatureForecastingModel>();

		for (Feature f : schema.getTargetList()) {

			for (String comb : hm.keySet()) {
				ForecastingModel model = hm.get(comb);

			}
		}
		return optModel;
	}

}
