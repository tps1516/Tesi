package data.feature;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;
import data.datavalue.NumericValue;


public class GearyIndex //implements AutocorrelationI
{
	

	
	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W, int beginIndex, int endIndex){
		return compute(data, (NumericFeature)feature, W, beginIndex, endIndex);
	
	}
	
	private Double compute(SnapshotData data, NumericFeature f, SnapshotWeigth W, int beginIndex, int endIndex) {
		Double I=0.0;
		double sumWij=0;
		double sumWijVar=0;
		double sumVar=0;
	
		Object M=f.getPrototype();
		int featureIndex=f.getIndexMining();
		int N=0;
		for(int i=beginIndex;i<=endIndex;i++)
		{
			int id1=data.getSensorPoint(i).getId();
			NumericValue Xi=(NumericValue)data.getSensorPoint(i).getMeasure(featureIndex);
			if(!Xi.isNull()){
				for(int j=beginIndex;j<=endIndex;j++)
				{
					int id2=data.getSensorPoint(j).getId();
					//if(W.getNeighbourhood(id1).contains(id2))
					{
					
						double wij=W.getWeight(id1, id2);
						NumericValue Xj=(NumericValue)data.getSensorPoint(j).getMeasure(featureIndex);
						//System.out.println("("+i+","+j+")"+Xi+":"+Xj);
						
						if(!Xj.isNull() && wij!=0 )
						{
							sumWij+=wij;
							sumWijVar+=wij*Math.pow(Xi.compareTo(Xj.getValue()),2);						
						}
					}
					
				}
			}
			sumVar+=Math.pow(Xi.compareTo(M),2);
			
			N++;
		}
		
		I= (((double)(N-1))/(2*sumWij)) * (sumWijVar/sumVar);
		if(I.equals(Double.NaN))
			I= 2.0;
		
		return I;
	}

	
	public boolean isMax(double v) {
		// TODO Auto-generated method stub
		return v==2.0;
	}
	
	

}
