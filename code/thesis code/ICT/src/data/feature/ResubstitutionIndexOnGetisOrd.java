package data.feature;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;
import data.datavalue.NumericValue;



public class ResubstitutionIndexOnGetisOrd implements AutocorrelationI {
	private double sumX=0;
	private double sumX2=0;
	private int N=0;
	
	public ResubstitutionIndexOnGetisOrd(){
		
	}
	public ResubstitutionIndexOnGetisOrd(double sumX, double sumX2,int N) {
		// TODO Auto-generated constructor stub
		this.sumX=sumX;
		this.sumX2=sumX2;
		this.N=N;
	}
	
	
	public ResubstitutionIndexOnGetisOrd(SnapshotData data, Feature feature, SnapshotWeigth W,
			int beginIndex, int endIndex) {
		Double R=0.0;
		NumericFeature f=(NumericFeature)feature;
		sumX=0.0;
		sumX2=0.0;
		int featureIndex=f.getIndexMining();
		N=0;
		for(int i=beginIndex;i<=endIndex;i++)
		{
			
			int id1=data.getSensorPoint(i).getId();
			NumericValue Xi=(NumericValue)(data.getSensorPoint(i).getMeasure(featureIndex));
			if(!Xi.isNull()){
				N++;
				double giValue=Xi.getGetis();
				//sumX+=(Double)(Xi.getValue());
				sumX+=giValue;
				//sumX2+=Math.pow((Double)(Xi.getValue()),2);
				sumX2+=Math.pow(giValue,2);
			}
		}
		//R=sumX2/N-Math.pow(sumX/N,2);
	
	}
	public void addX(SnapshotData trainingSet, int begin, int end, SnapshotWeigth W,Feature feature) //X è in posizione end+1
	//public void addX(Value X,Feature feature)
	{
		NumericFeature f=(NumericFeature)feature;
		NumericValue X=(NumericValue)trainingSet.getSensorPoint(end+1).getMeasure(feature.getIndexMining());
		if(!X.isNull()){
			double giValue=X.getGetis();
			sumX+=giValue;
			sumX2+=Math.pow(giValue, 2);
			N++;
		}
	}
	public void subX(SnapshotData trainingSet, int begin, int end,SnapshotWeigth W,Feature feature) // X è in posizione begin -1
	//public void subX(Value X, Feature feature)
	{
		NumericFeature f=(NumericFeature)feature;
		NumericValue X=(NumericValue)trainingSet.getSensorPoint(begin-1).getMeasure(feature.getIndexMining());
		if(!X.isNull()){
			double giValue=X.getGetis();
			sumX-=giValue;
			sumX2-=Math.pow(giValue, 2);
			N--;
		}
	}
	
	public double getSumX(){
		return sumX;
	}

	public double getSumX2(){
		return sumX2;
	}
	
	public int getN(){
		return N;
	}

		@Override
		public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,	int beginIndex, int endIndex) {
			Double R=0.0;
			NumericFeature f=(NumericFeature)feature;
			Double sumX=0.0;
			double sumX2=0.0;
			int featureIndex=f.getIndexMining();
			int N=0;
			for(int i=beginIndex;i<=endIndex;i++)
			{
				
				int id1=data.getSensorPoint(i).getId();
				NumericValue Xi=(NumericValue)data.getSensorPoint(i).getMeasure(featureIndex);
				if(!Xi.isNull()){
					N++;
					sumX+=(Double)(Xi.getGetis());
					
					
					sumX2+=Math.pow((Double)(Xi.getGetis()),2);
				}
			}
			R=sumX2/N-Math.pow(sumX/N,2);
			
			return R;
		}

	
		
		
		@Override
		public boolean isMax(double v) {
			return v==0.0;
		}

		public Double get(){
			double R=sumX2/N-Math.pow(sumX/N,2);
			return R;
		}


}
