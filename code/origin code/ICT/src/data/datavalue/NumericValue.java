package data.datavalue;

public class NumericValue extends Value{
	
	Double  getis=0.0;
	public NumericValue(Double value, int attributeIndex) {
		// TODO Auto-generated constructor stub
		super(value,attributeIndex);
		
	}

	public double getGetis(){
		return getis;
	}
	
	public void setGetis(double getis){
		this.getis=getis;
	}

	public double compareTo(Object o) {
		// TODO Auto-generated method stub
		return (Double)value-(Double)o;
	}
	
	public double scale(double min, double max){
		return ((Double) value-min)/(max-min);
	}
	
	public String toString(){
		//return value+ "("+attributeIndex+")";
		//return ""+value+"[GO"+getis+"]";
		if(value!=null)
		return value.toString();
		else return "null";
	}

	

}
