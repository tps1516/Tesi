package varUtility;

import java.util.ArrayList;
import java.util.HashMap;

import data.feature.Feature;
import snapshot.SnapshotSchema;
import forecast.ForecastingModel;
import forecast.ForecastingModelIndex;

public class FromRToJava {

	public HashMap<String, ForecastingModel> converter(
			HashMap<String, ArrayList<Object>> hm, SnapshotSchema schema,
			Double[][] timeseries) {
		ForecastingModelIndex index = new ForecastingModelIndex();

		for (String combinations : hm.keySet()) {

			ArrayList<Object> dataFromR = hm.get(combinations);
			ArrayList<ArrayList<Feature>> listOfCorrelatedFeature = (ArrayList<ArrayList<Feature>>) dataFromR
					.get(index.feature);
			ArrayList<ArrayList<ArrayList<Double>>> listOfCorrelatedCoefficient = (ArrayList<ArrayList<ArrayList<Double>>>) dataFromR
					.get(index.coefficients);
			
//			int p= (int) 

			for (Feature f : schema.getTargetList()) {

			}

		}

		return null;
	}

}
