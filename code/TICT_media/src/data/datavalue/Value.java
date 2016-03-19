package data.datavalue;

import java.io.Serializable;

public class Value implements Serializable, Cloneable {

	protected Object value;
	protected int attributeIndex;

	public Value(Object value, int attributeIndex) {
		this.value = value;
		this.attributeIndex = attributeIndex;
	}

	public Object getValue() {
		return value;
	}

	public int getAttributeIndex() {
		return attributeIndex;
	}

	public Boolean isNull() {
		return value == null;
	}

	public void setValue(Object v) {
		value = v;
	}

	public String toString() {
		// return value+ "("+attributeIndex+")";
		if (value == null)
			return "null";
		else
			return "" + value;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
