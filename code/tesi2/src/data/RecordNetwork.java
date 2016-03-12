package data;

import forecast.ForecastingModel;
import windowStructure.FeatureWindow;

public class RecordNetwork {
	
	private FeatureWindow timeseries;
	private ForecastingModel VARModel;
	
	RecordNetwork(FeatureWindow ts){
		this.VARModel=null;
		this.timeseries=ts;
	}
	
	FeatureWindow getTimeseries(){
		return this.timeseries;
	}
	
	ForecastingModel getVARModel(){
		return this.VARModel;
	}

	
	void setTimeSeries(FeatureWindow ts){
		this.timeseries=ts;
	}
	
	void setVARModel(ForecastingModel VARMod){
		this.VARModel=VARMod;
	}
}
