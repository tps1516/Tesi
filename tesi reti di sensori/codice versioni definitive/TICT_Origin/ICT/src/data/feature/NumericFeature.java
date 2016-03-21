package data.feature;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class NumericFeature extends Feature implements Cloneable{
	private Double min=null;
	private Double max=null;
	private Double sum=null;	
	private Double scaledmean=null;
	//private Double streamMean=new Double(0.0); // modello sulla landmark window
	//private Double spatialMean=0.0;
	//private Double spatialStreamMean=0.0;


	
	
	public NumericFeature(String name,int indexStream){
		super(name,indexStream);
		
	}
	
	public void setMean(Double v){
		
		if(this.sum==null)
			this.sum=v;
		else
		{
			this.sum+=v;
		}
		this.countTuples++;
		
	}
	
	
	public void setMin(Double min){
		if(this.min==null)
			this.min=min;
		else
			if(min<this.min)
				this.min=min;
		
	}
	public void setMax(Double max){
		if(this.max==null)
			this.max=max;
		else
			if(max>this.max)
				this.max=max;
	}
	
	public double getMin(){
		return min;
	}
	public double getMax(){
		return max;
	}
	
	
	public void setScaledMean(double m){
		scaledmean=m;
	}
	public double getScaledMean(){
		return scaledmean;
	}
	public double getMean(){
		if(sum!=null)
		return (sum/countTuples);
		else return -1.0;
	}
	

	
	public void clear(){
		super.clear();
		min=null;
		max=null;
		sum=null;
	
	}
	public String toString(){
		return super.toString()+//"MIN:"+getMin()+"MAX:"+getMax()+"SUM:"+sum+"COUNT:"+countTuples+
				"MEAN:"+getMean();//+"SMEAN:"+getStreamPrototype()+"\n";
	}
	
	public Object clone(){
		
		NumericFeature v=(NumericFeature) super.clone();
		if(min!=null)
			v.min= new Double(min);
		if(max!=null)
			v.max= new Double(max);
		if(sum!=null)
			v.sum= new Double(sum);
	
		return v;
			
}



	@Override
	public void computeAutocorrelation(AutocorrelationI a, SnapshotData data,
			SnapshotWeigth W, int beginIndex, int endIndex) {

		//autocorrelationMeasure=a.compute(data, this, W, beginIndex, endIndex);
		
		if(a instanceof MoranIndex)
		{
			
			autocorrelationMeasure=new MoranIndex(data, this, W, beginIndex, endIndex).get(); // Annalisa per ottimizzazione di MoranVar
			//System.out.println("Auto feature "+a.compute(data, this, W, beginIndex, endIndex)+ " " +autocorrelationMeasure);
			
		}
		else if(a instanceof ResubstitutionIndexOnGetisOrd){
				autocorrelationMeasure=new ResubstitutionIndexOnGetisOrd(data, this, W, beginIndex, endIndex).get(); // Annalisa per ottimizzazione di ResubstitutionIndexOnGetisAndOrd
			//	System.out.println("Auto feature "+a.compute(data, this, W, beginIndex, endIndex)+ " " +autocorrelationMeasure);
			}
			else
				if(a instanceof ResubstitutionIndex){
					autocorrelationMeasure=new ResubstitutionIndex(data, this, W, beginIndex, endIndex).get(); // Annalisa per ottimizzazione di ResubstitutionIndexOnGetisAndOrd
					//System.out.println("Auto feature "+a.compute(data, this, W, beginIndex, endIndex)+ " " +autocorrelationMeasure);
				}
				else throw new RuntimeException("Unknown split evalaution heuristic");
				//autocorrelationMeasure=a.compute(data, this, W, beginIndex, endIndex);
		
	}

	

	

	

	@Override
	public Object getPrototype() {

			return getMean();
	
		
	}




	




}
