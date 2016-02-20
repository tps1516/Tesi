package forecast;

import rForecast.RForecast;
import rForecast.RVar;
import snapshot.SnapshotSchema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import data.feature.Feature;

public class ForecastingModel implements Iterable<FeatureForecastingModel>,
		Serializable {
	
	private ArrayList<FeatureForecastingModel> models;

	
	public ForecastingModel(double[][] dataset, SnapshotSchema schema,
			ArrayList<Object> rParameters) {
		ArrayList<Object> result = new ArrayList<Object>();
		RForecast r = new RVar();
		result = r.RForecasting(dataset, schema, rParameters);
		initializesModels(result, schema);
	}

	
	public ForecastingModel(ArrayList<FeatureForecastingModel> mod){
		setModels(mod);
	}
	
	public String toString() {
		String str = "MODELLO VAR:" + "\n";;
		for (FeatureForecastingModel VARModel : models) {
			str += VARModel.toString() + "\n";
		}

		return str;
	}

	private void initializesModels(ArrayList<Object> resultExecutR,
			SnapshotSchema schema) {
		models = new ArrayList<FeatureForecastingModel>(schema.getTargetList()
				.size());
		ForecastingModelIndex index = new ForecastingModelIndex();
		ArrayList<ArrayList<Feature>> listOfCorrelatedFeature = (ArrayList<ArrayList<Feature>>) resultExecutR
				.get(index.feature);
		ArrayList<ArrayList<ArrayList<Double>>> listOfCorrelatedCoefficients = (ArrayList<ArrayList<ArrayList<Double>>>) resultExecutR
				.get(index.coefficients);

		for (Feature f : schema.getTargetList()) {
			int i = f.getIndexMining() - schema.getSpatialList().size();
			FeatureForecastingModel VARModel = new FeatureVARForecastingModel(
					f, listOfCorrelatedFeature.get(i),
					listOfCorrelatedCoefficients.get(i));
			models.add(i, VARModel);
		}
	}

	@Override
	public Iterator<FeatureForecastingModel> iterator() {

		return this.models.iterator();
	}

	
	private void setModels(ArrayList<FeatureForecastingModel> mod){
		this.models=mod;
	}
	
	
	public FeatureForecastingModel getFeatureForecastingModel(Feature f){
		return models.get(f.getFeatureIndex());
	}
}
