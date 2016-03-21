package data.datavalue;

public class NumericValue extends Value {

	public NumericValue(Double value, int attributeIndex) {
		// TODO Auto-generated constructor stub
		super(value, attributeIndex);

	}

	public double compareTo(Object o) {
		// TODO Auto-generated method stub
		return (Double) value - (Double) o;
	}

	public String toString() {
		// return value+ "("+attributeIndex+")";
		// return ""+value+"[GO"+getis+"]";
		if (value != null)
			return value.toString();
		else
			return "null";
	}

}
