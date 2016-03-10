package rForecast;

public class VARParameter {

	private String type;
	private String ic;
	
	VARParameter(String type, String ic) {
		this.type = type;
		this.ic = ic;
	}

	public String toString(){
		return type+"_"+ic;
	}
	
	String getType(){
		return type;
	}
	
	String getIC(){
		return ic;
	}
}
