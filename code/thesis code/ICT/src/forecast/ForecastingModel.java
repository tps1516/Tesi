package forecast;

import snapshot.SnapshotSchema;

import java.util.ArrayList;
import java.util.Iterator;

import data.feature.Feature;

public class ForecastingModel implements Iterable <FeatureForecastingModel>{
	private int p;
	private ArrayList<FeatureForecastingModel> models;
	
 
	public ForecastingModel(double [][] dataset,SnapshotSchema schema, ArrayList<Object> rParameters){
		
	}
	 
	public String toString(){
		String str="";
		for (FeatureForecastingModel VARModel: models){
			str+=VARModel.toString() + "\n";
		}
		
		return str;
	}
	private void initializesModels(ArrayList<Object> resultExecutR,SnapshotSchema schema){
		models= new ArrayList<FeatureForecastingModel>(schema.getTargetList().size());
		ForecastingModelIndex index=new ForecastingModelIndex();
		ArrayList<ArrayList<Feature>> listOfCorrelatedFeature = (ArrayList<ArrayList<Feature>>) resultExecutR.get(index.feature);
		ArrayList<ArrayList<ArrayList<Double>>> listOfCorrelatedCoefficients= (ArrayList<ArrayList<ArrayList<Double>>>)resultExecutR.get(index.coefficients);
		
		
		for (Feature f:schema.getTargetList()){
			int i=f.getIndexMining()-schema.getSpatialList().size();
			FeatureForecastingModel VARModel= new FeatureVARForecastingModel(f,
						listOfCorrelatedFeature.get(i),listOfCorrelatedCoefficients.get(i));
			models.add(i,VARModel);
		}
	}


	@Override
	public Iterator<FeatureForecastingModel> iterator() {
		
		return this.models.iterator();
	}




}
