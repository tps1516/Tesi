package data.feature;

import java.util.HashMap;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;
import data.datavalue.Value;

public class SpatialEntropy //implements AutocorrelationI
{
	private static final double lambda=0.00000001;
	private static final double beta=100000.0;
	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W, int beginIndex, int endIndex) {
		Double E=0.0;
		CategoricalFeature f=(CategoricalFeature)feature;
		HashMap<String,Double> DInt=new HashMap<String,Double>();
		HashMap<String,Double> DExt=new HashMap<String,Double>();
		int featureIndex=f.getIndexMining();

		for(String classLabel:f){
			DInt.put(classLabel, new Double (0.0));;
			DExt.put(classLabel, new Double (0.0));;
			
		}
		int C=0;
		for(int i=beginIndex;i<=endIndex;i++){
			int id1=data.getSensorPoint(i).getId();
			Value Xi=data.getSensorPoint(i).getMeasure(featureIndex);
			if(!Xi.isNull()){
				C++;
				String classI=(String)Xi.getValue();
				for(int j=beginIndex;j<=endIndex;j++){
					int id2=data.getSensorPoint(j).getId();
					Value Xj=data.getSensorPoint(j).getMeasure(featureIndex);
					if(j!=i && !Xj.isNull()){
						String classJ=(String)Xj.getValue();
						Double dij=W.getDistance(id1, id2);
						
						if(classJ.equals(classI))
							DInt.put(classI,DInt.get(classI)+dij);
						else
							DExt.put(classI,DExt.get(classI)+dij);
					}
					
				}
			}
		}
		
		
		for(String classLabel:f){
			if(f.getFrequency(classLabel)>1)
				DInt.put(classLabel, DInt.get(classLabel)/(f.getFrequency(classLabel)*(f.getFrequency(classLabel)-1)));
			else
				DInt.put(classLabel,lambda);
			
			if(C==f.getFrequency(classLabel))
				DExt.put(classLabel, beta);
			else
				DExt.put(classLabel, DExt.get(classLabel)/(f.getFrequency(classLabel)*(C-(f.getFrequency(classLabel)))));
					
			
		}
		
		for(String classLabel:f){
			double p=((double)f.getFrequency(classLabel)/C);
			E+=(-DInt.get(classLabel)/DExt.get(classLabel))*p*Math.log10(p)/Math.log10(2);
		}
		
		return E;
	}

	public boolean isMax(double v) {
		// TODO Auto-generated method stub
		return v==0.0;
	}

}
