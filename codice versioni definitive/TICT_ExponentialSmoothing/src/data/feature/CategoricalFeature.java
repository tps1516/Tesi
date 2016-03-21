package data.feature;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;



public class CategoricalFeature extends Feature implements Iterable<String>, Cloneable{

	private Map<String,Integer> ts= new HashMap<String,Integer>();
	private Map<String,Double> streamTs= new HashMap<String,Double>();
	
	
	
	public CategoricalFeature(String name,int indexStream){
		super(name,indexStream);
	}
	
	public void addCategory(String v){
		if(v!=null){
			this.countTuples++;
			if(ts.containsKey(v))
				ts.put(v, ts.get(v)+1);
			else
				ts.put(v,1);
		}
		
	}
	@Override
	public Iterator<String> iterator() {
		// TODO Auto-generated method stub
		return ts.keySet().iterator();
	}
	
	public int getFrequency(String c){
		return (ts.get(c));
	}
	
	public int getNumberOfClasses(){
		return ts.size();
	}
	

	
	String getMaxFrequentCategory(){
		int max=-1;
		String category="";
		for(String s:ts.keySet()){
			int f=ts.get(s);
			if(f>max){
				category=s;
				max=f;
			}
		}
		return category;
	}
	
	public String toString(){
		return super.toString()+ts+"\n";
	}

	
	public void clear(){
		super.clear();
		ts.clear();
	
	}
	
	public Object clone(){
		CategoricalFeature f=(CategoricalFeature) super.clone();
		f.ts=new HashMap<String, Integer>();
		for(String s:ts.keySet())
			f.ts.put(s, new Integer(ts.get(s)));
		
		f.streamTs=new HashMap<String, Double>();
		for(String s:streamTs.keySet())
			f.streamTs.put(s, new Double(streamTs.get(s)));
		
		return f;
		

		
	}

	public Object getPrototype() {
		// TODO Auto-generated method stub
		return getMaxFrequentCategory();
	}

	@Override
	public void computeAutocorrelation(AutocorrelationI a, SnapshotData data,
			SnapshotWeigth W, int beginIndex, int endIndex) {
		autocorrelationMeasure=//new SpatialEntropy()
				a.compute(data, this, W, beginIndex, endIndex);
		
	}

	






	
	

}
