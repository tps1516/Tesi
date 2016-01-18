package data.feature;

import data.datavalue.NumericValue;
import data.datavalue.Value;

import snapshot.SnapshotData;

import snapshot.SnapshotWeigth;

public class MoranIndex implements AutocorrelationI {

	public  static double MAX=1;
	
	private int  N=0;
	private double sumWij=0;
	private double sumWXYij=0;
	private double sumX=0;
	private double sumX2=0;
	private double sumWijXi=0;
	
	
	public int getN(){
		return N;
		
	}
	public double getsumWij(){
		return sumWij;
	}
	
	public double getSumWXYij(){
		return sumWXYij;
	}
	
	public double getSumX(){
		return sumX;
	}
	public double getSumX2(){
		return sumX2;
	}
	public double sumWijXi(){
		return sumWijXi;
	}
	public MoranIndex(int N, double sumWij,double sumWXYij, double sumX,double sumX2,double sumWijXi){
		this.N=N;
		this.sumWij=sumWij;
		this.sumWXYij=sumWXYij;
		this.sumX=sumX;
		this.sumX2=sumX2;
		this.sumWijXi=sumWijXi;
	}
	
	public MoranIndex(){
		
	}
	
	public MoranIndex(SnapshotData data, Feature feature, SnapshotWeigth W, int beginIndex, int endIndex) {
	sumWij=0;
	sumWXYij=0;
	sumX=0;
	sumX2=0;
	sumWijXi=0;
	NumericFeature f=(NumericFeature)feature;
	

	//Object M=f.getPrototype();
	int featureIndex=f.getIndexMining();
	N=0;
	
	for(int i=beginIndex;i<=endIndex;i++){
		int id1=data.getSensorPoint(i).getId();
		NumericValue Xi=(NumericValue)data.getSensorPoint(i).getMeasure(featureIndex);
		double scaledValueI=((NumericValue)Xi).scale(f.getMin(), f.getMax());
		if(!Xi.isNull()){
			N++;
			sumX+=scaledValueI;
			sumX2+=Math.pow(scaledValueI, 2);
			for(int j=beginIndex;j<=endIndex;j++)
			{
				int id2=data.getSensorPoint(j).getId();
				double wij=W.getWeight(id1, id2);
				
				NumericValue Xj=(NumericValue)data.getSensorPoint(j).getMeasure(featureIndex);
				//System.out.println("("+i+","+j+")"+Xi+":"+Xj);
				double scaledValueJ=((NumericValue)Xj).scale(f.getMin(), f.getMax());
				
				if(!Xj.isNull() && wij!=0 ){
					sumWij+=wij;
					sumWXYij+=(wij*scaledValueI*scaledValueJ);
					sumWijXi+=wij*scaledValueI;
				}
			}
		}
	}
	

}
	
	public void addX(SnapshotData trainingSet, int begin, int end, SnapshotWeigth W,Feature feature) //X è in posizione end+1
	{
		NumericFeature f=(NumericFeature)feature;
		Value X=trainingSet.getSensorPoint(end+1).getMeasure(feature.getIndexMining());
		if(!X.isNull()){
			double scaledValue=((NumericValue)X).scale(f.getMin(), f.getMax());
			int id=trainingSet.getSensorPoint(end+1).getId();		
			N++;
			sumX+=scaledValue;
			sumX2+=Math.pow(scaledValue,2);
			for(int i=begin;i<=end;i++){
				int idI=trainingSet.getSensorPoint(i).getId();
				Value targetValueI=trainingSet.getSensorPoint(i).getMeasure(f.getIndexMining());
				double scaledValueI=((NumericValue)targetValueI).scale(f.getMin(), f.getMax());
				double winew=W.getWeight(idI,id);
				double wnewi=W.getWeight(id, idI);
				if(winew!=0){ // la matrice W è simmetrica
					sumWXYij+=2*scaledValue*scaledValueI*winew;
					sumWij+=2*winew;
					sumWijXi+=winew*scaledValue;
					sumWijXi+=scaledValueI*wnewi;
				}
			}
			double winew=W.getWeight(id,id);
			sumWXYij+=scaledValue*scaledValue*winew;
			sumWij+=winew;
			sumWijXi+=winew*scaledValue;
			
			
			
			
		}
	}
	
	
	public void subX(SnapshotData trainingSet, int begin, int end,SnapshotWeigth W,Feature feature) // X è in posizione begin -1
	{
		NumericFeature f=(NumericFeature)feature;
		Value X=trainingSet.getSensorPoint(begin-1).getMeasure(feature.getIndexMining());
		if(!X.isNull()){
			double scaledValue=((NumericValue)X).scale(f.getMin(), f.getMax());
			int id=trainingSet.getSensorPoint(begin-1).getId();		
			N--;
			sumX-=scaledValue;
			sumX2-=Math.pow(scaledValue,2);
			for(int i=begin;i<=end;i++){
				int idI=trainingSet.getSensorPoint(i).getId();
				Value targetValueI=trainingSet.getSensorPoint(i).getMeasure(f.getIndexMining());
				double scaledValueI=((NumericValue)targetValueI).scale(f.getMin(), f.getMax());
				double winew=W.getWeight(idI,id);
				double wnewi=W.getWeight(id,idI);
				if(winew!=0){// la matrice W è simmetrica
					sumWXYij-=2*scaledValue*scaledValueI*winew;
					sumWij-=2*winew;
					sumWijXi-=wnewi*scaledValue;
					sumWijXi-=scaledValueI*winew;
				}
			}
			double winew=W.getWeight(id,id);
			sumWXYij-=scaledValue*scaledValue*winew;
			sumWij-=winew;
			sumWijXi-=winew*scaledValue;
		}
		
	}
	
	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W, int beginIndex, int endIndex){
	
		return compute(data, (NumericFeature)feature, W, beginIndex, endIndex);
	
	}
	
	private Double compute(SnapshotData data, NumericFeature f, SnapshotWeigth W, int beginIndex, int endIndex) {
		
		Double I=0.0;
		double sumWij=0;
		double sumWijVar=0;
		double sumVar=0;
	
		//Object M=f.getPrototype();
		int featureIndex=f.getIndexMining();
		int N=0;
		
		double M=0.0;
		//double sumScaleI2=0;
		for(int i=beginIndex;i<=endIndex;i++)
		{
			NumericValue Xi=(NumericValue)data.getSensorPoint(i).getMeasure(featureIndex);
			double scaledValueI=((NumericValue)Xi).scale(f.getMin(), f.getMax());
			M+=scaledValueI;
		//	sumScaleI2+=Math.pow(scaledValueI, 2);
			
		}
		M=M/(endIndex-beginIndex+1);	
		for(int i=beginIndex;i<=endIndex;i++)
		{
			int id1=data.getSensorPoint(i).getId();
			NumericValue Xi=(NumericValue)data.getSensorPoint(i).getMeasure(featureIndex);
			double scaledValueI=((NumericValue)Xi).scale(f.getMin(), f.getMax());
			
			if(!Xi.isNull()){
				for(int j=beginIndex;j<=endIndex;j++)
				{
					int id2=data.getSensorPoint(j).getId();
					//if(W.getNeighbourhood(id1).contains(id2))
					{
						double wij=W.getWeight(id1, id2);
					
						NumericValue Xj=(NumericValue)data.getSensorPoint(j).getMeasure(featureIndex);
						//System.out.println("("+i+","+j+")"+Xi+":"+Xj);
						double scaledValueJ=((NumericValue)Xj).scale(f.getMin(), f.getMax());
						
						if(!Xj.isNull() && wij!=0 ){
							sumWij+=wij;
							//sumWijVar+=wij*Xi.compareTo(M)*Xj.compareTo(M);
							sumWijVar+=wij*(scaledValueI-M)*(scaledValueJ-M);
						
						}
					}
				}
				//sumVar+=Math.pow(Xi.compareTo(M),2);
				sumVar+=Math.pow(scaledValueI-M,2);
				N++;
			}
			
		
		}
		
		I= (((double)(N))/sumWij)*(sumWijVar/sumVar);
		if(I.equals(Double.NaN))
			I= 1.0;
		I=((I+1)/2); // scaled in [0,1]
		//double R= (sumScaleI2-(endIndex-beginIndex+1)*Math.pow(M, 2))/(endIndex-beginIndex+1);
		
		return I;
	}
	
	
	public Double get(){
		double meanX=sumX/N;
		double den=sumWij*(sumX2-N*Math.pow(meanX, 2));
		double num=N*(sumWXYij-2*meanX*sumWijXi+Math.pow(meanX, 2)*sumWij);
		
		Double I=num/den;
		if(I.equals(Double.NaN))
			I= 1.0;
		I=((I+1)/2); // scaled in [0,1]
		//double R= (sumScaleI2-(endIndex-beginIndex+1)*Math.pow(M, 2))/(endIndex-beginIndex+1);
		
		return I;
	}
	public boolean isMax(double v) {
		// TODO Auto-generated method stub
		return v==1.0;
	}



}
