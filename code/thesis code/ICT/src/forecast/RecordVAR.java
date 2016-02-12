package forecast;

import java.util.ArrayList;

import data.feature.Feature;

class RecordVAR {
	private Feature feature;
	private ArrayList<Double> coefficients;
	
	 
	RecordVAR(Feature f,ArrayList<Double> coef){
		this.feature=f;
		this.coefficients=coef;
	}
	
	
	public String toString() {
		String str="";
		
		str+= feature.getName()+ ":";
		for (Double coef:coefficients){
			str+=" " + coef;
		}
		
		return str;
	}
	
	
	Feature getFeature(){
		return this.feature;
	}
	
	ArrayList<Double> getCoefficients(){
		return this.coefficients;
	}
}
