package rForecast;

class Combination {

	private String type;
	private String ic;
	
	Combination(String type, String ic) {
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
