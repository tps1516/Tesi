package rForecast;

/*
 * Manage the exception of R execution.
 */

@SuppressWarnings("serial")
public class RVarException extends RuntimeException{

	RVarException(){
		super();
	}
	
	RVarException(String s){
		super(s);
	}

}
