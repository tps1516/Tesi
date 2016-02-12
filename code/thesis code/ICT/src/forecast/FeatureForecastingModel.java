package forecast;



import data.feature.Feature;

public abstract class FeatureForecastingModel{
	protected Feature feature;
	
	protected FeatureForecastingModel(Feature f){
		this.feature=f;
	}
	
	public abstract String toString();
	
	public Feature getFeature(){
		return this.feature;
	}

}
