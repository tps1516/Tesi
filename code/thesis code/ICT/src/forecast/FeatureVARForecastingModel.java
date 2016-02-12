package forecast;

import java.util.ArrayList;
import java.util.Iterator;

import data.feature.Feature;

public class FeatureVARForecastingModel extends FeatureForecastingModel implements Iterable<RecordVAR>{

	private ArrayList<RecordVAR> equationModel;
	
	protected FeatureVARForecastingModel(Feature f,ArrayList<Feature> correlatedFeature,ArrayList<ArrayList<Double>> coefficients) {
		super(f);
		initializesEquationModel(correlatedFeature, coefficients);
	}
	 
	public String toString(){
		String str="";
		for (RecordVAR r: this){
			str+= r.toString() + "\n";
		}
		
		return str;
	}

	
	private void initializesEquationModel(ArrayList<Feature> correlatedFeature,ArrayList<ArrayList<Double>> coefficients){
		this.equationModel= new ArrayList<RecordVAR>();
		for (int i=0; i<correlatedFeature.size(); i++){
			RecordVAR record= new RecordVAR(correlatedFeature.get(i),coefficients.get(i));
			this.equationModel.add(record);
		}
	}

	@Override
	public Iterator<RecordVAR> iterator() {
		return this.equationModel.iterator();
	}
}
