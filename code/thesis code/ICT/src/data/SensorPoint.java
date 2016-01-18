package data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import data.datavalue.Value;

public class SensorPoint implements Iterable<Value>, Cloneable,Serializable{
	private int id;
	private List<Value> measures=new ArrayList<Value>();
	
	public SensorPoint(int id) {
			this.id=id;
		
	}


	public int getId(){
		return id;
	}
	
		
	public void addMeasure(Value v){
		measures.add(v.getAttributeIndex(), v);
	}

	public void setMeasure(Value v){
		measures.set(v.getAttributeIndex(), v);
	}



	public Value getMeasure(int index) {
		return measures.get(index);
	}
	
	public String toString(){
		//return id+":"+measures;
		String s=""+id;
		for(Value v:measures)
			if(v==null)
				s+=",?";
			else
			s+=","+v;
		return s;
	}

	@Override
	public Iterator<Value> iterator() {
		// TODO Auto-generated method stub
		return measures.iterator();
	}
	

	

}