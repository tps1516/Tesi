package data.feature;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class Entropy //implements AutocorrelationI
{

	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,
			int beginIndex, int endIndex) {
		CategoricalFeature f=(CategoricalFeature)feature;
		Double E=0.0;
		int C=f.getCountTuples();
		for(String classLabel:f){
			double p=((double)f.getFrequency(classLabel)/C);
			E-=p*Math.log10(p)/Math.log10(2);
		}
		
		return E;
	
	}

	
	public boolean isMax(double v) {
		// TODO Auto-generated method stub
		return v==0;
	}

}
