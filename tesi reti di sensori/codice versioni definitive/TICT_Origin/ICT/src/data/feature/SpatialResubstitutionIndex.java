package data.feature;

import data.datavalue.Value;
import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class SpatialResubstitutionIndex // implements AutocorrelationI 
{

	private Double computePrototype(SnapshotData data, NumericFeature f, SnapshotWeigth W, int beginIndex,
			int endIndex) {
		
	
		double sum=0.0;
		int count=0;
		int featureIndex=f.getIndexMining();
		for(int i=beginIndex;i<=endIndex;i++)
		{
			
			int id1=data.getSensorPoint(i).getId();
			Value Xi=data.getSensorPoint(i).getMeasure(featureIndex);
			if(!Xi.isNull()){
				count++;
					
				double predictedXi=0.0;
				double weightI=0.0;
				//System.out.println("Xi"+Xi);
				for(int j=beginIndex;j<=endIndex;j++)
				{
					int id2=data.getSensorPoint(j).getId();
					double wij=W.getWeight(id1, id2);
					Value Xj=data.getSensorPoint(j).getMeasure(featureIndex);
					if(!Xj.isNull() && wij!=0  ){
						
						
						predictedXi+=wij*(Double)Xj.getValue();
						weightI+=wij;
					}
				}
				predictedXi/=weightI;
				sum+=(predictedXi);
				
			}
					
		}
				
		
		return sum/count;
	}
	
	
	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,
			int beginIndex, int endIndex) {
		Double R=0.0;
		NumericFeature f=(NumericFeature)feature;
		Double sumX=0.0;
		double mean=computePrototype(data, f, W, beginIndex, endIndex);

		int featureIndex=f.getIndexMining();
		
		int N=0;
		for(int i=beginIndex;i<=endIndex;i++)
		{
			
			int id1=data.getSensorPoint(i).getId();
			Value Xi=data.getSensorPoint(i).getMeasure(featureIndex);
			if(!Xi.isNull()){
				N++;
				sumX+=Math.pow((Double)(Xi.getValue())-mean,2);
				
			}
		}
		R=sumX/N;
		return R;
	}


	public boolean isMax(double v) {
		return v==0.0;
	}

}