package data.feature;

import data.datavalue.NumericValue;
import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;
import data.datavalue.Value;

public class GetisOrdIndex //implements AutocorrelationI 
{
	public  static double MAX=0.0;
	

	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,
			int beginIndex, int endIndex) {
		
		if(endIndex-beginIndex==0)
			return 0.0;
		Double G=0.0;
		
		Double GVariance=0.0;
		double sumGi2=0.0;
		double sumGi=0;
		int countGi=0;

	
		NumericFeature f=(NumericFeature)feature;
		Object M=f.getPrototype();
		int featureIndex=f.getIndexMining();
		int N=0;
		double M2=0.0;
		
		for(int i=beginIndex;i<=endIndex;i++)
		{
			
			int id1=data.getSensorPoint(i).getId();
			
			Value Xi=data.getSensorPoint(i).getMeasure(featureIndex);
			double sumWi=0.0;
			double sumWijXj=0.0;
			double sumW2i=0.0;
			double s=0;
			double num,den;
			
			if(!Xi.isNull()){
				N=0;
				boolean flag=false;
				//System.out.println("Xi"+Xi);
				for(int j=beginIndex;j<=endIndex;j++)
				{
					
					int id2=data.getSensorPoint(j).getId();
					//if(W.getNeighbourhood(id1).contains(id2))
					{						double wij=W.getWeight(id1, id2);
						Value Xj=data.getSensorPoint(j).getMeasure(featureIndex);
						if(!Xj.isNull()  ){
							N++;
							s+=(Math.pow(((NumericValue)Xj).compareTo(M),2));
							if(wij!=0){
					//			System.out.println("Xj"+Xj);
								
								flag=true;
								sumWijXj+=wij*(Double)Xj.getValue();
								sumWi+=wij;
								sumW2i+=Math.pow(wij, 2);
							}
							
							
						}
					}
					
				}
				if(!flag){
					System.out.println("Empty neighbourhood for "+Xi);
					//to debug 					compute( data,  feature,  W,							 beginIndex,  endIndex) ;
					
				}
				num=sumWijXj-f.getMean()*sumWi;
				s=s/(N);
				den=Math.sqrt((s/(N-1))*(N*sumW2i-Math.pow(sumWi,2)));
				Double Gi=num/den;
				if(Gi.equals((Double.NaN)) || Gi.equals(Double.NEGATIVE_INFINITY) || Gi.equals(Double.POSITIVE_INFINITY))
					Gi=1000000.0;
//				System.out.println(id1+":"+Xi.getValue()+"Gi:"+Gi);
				sumGi+=Gi;
				sumGi2+=Math.pow(Gi,2);
				countGi++;
			}
			
			
			
		}
		GVariance=Math.sqrt(sumGi2-Math.pow(sumGi,2)/countGi)/countGi;
		
		//System.out.println("Local Getis Variance:"+GVariance);
		
		return GVariance;
	}

	
	
	
	

	public boolean isMax(double v) {
		// TODO Auto-generated method stub
		return v==0.0;
	}
}