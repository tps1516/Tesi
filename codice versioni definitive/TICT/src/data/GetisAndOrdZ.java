package data;

import data.datavalue.NumericValue;
import data.datavalue.Value;
import data.feature.Feature;
import data.feature.NumericFeature;
import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class GetisAndOrdZ {
	
	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,	int index, double min, double max) {
		
		NumericFeature f=(NumericFeature)feature;
		Object M=f.getPrototype();
		
		//double M=f.getScaledMean();
		
		int featureIndex=f.getIndexMining();
		int N=0;

		
		int id1=data.getSensorPoint(index).getId();
		double sumWi=0.0;
		double sumWijXj=0.0;
		double sumW2i=0.0;
		double s=0;
		double num,den;
	
		
		for(int j=0;j<=data.size()-1;j++)
		{
			
			int id2=data.getSensorPoint(j).getId();
			
			Value Xj=data.getSensorPoint(j).getMeasure(featureIndex);
			
		
			if(!Xj.isNull()  )
			{
						
					double wij=W.getWeight(id1, id2);
				
						N++;
						s+=(Math.pow(((NumericValue)Xj).compareTo(M),2));
						//double scaledXj=((NumericValue)Xj).scale(min, max);
						
					//s+=Math.pow(scaledXj-M, 2);
					
						if(wij!=0){
					
								sumWijXj+=wij*(Double)Xj.getValue();
								//sumWijXj+=wij*scaledXj;
								sumWi+=wij;
								sumW2i+=Math.pow(wij, 2);
							}
							
							
				}
			}
					
				
			
			num=sumWijXj-f.getMean()*sumWi;
			//num=sumWijXj-f.getScaledMean()*sumWi;
			s=s/(N);
			den=Math.sqrt((s/(N-1))*(N*sumW2i-Math.pow(sumWi,2)));
			Double Gi=num/den;
			if(Gi.equals((Double.NaN)) || Gi.equals(Double.NEGATIVE_INFINITY) || Gi.equals(Double.POSITIVE_INFINITY))
					Gi=1000000.0;
//			
			return Gi;
	}
			
			
	

}
