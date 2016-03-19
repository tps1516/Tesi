package rForecast;

/*
 * Manage the exception about the parameters for R.
 */

@SuppressWarnings("serial")
public class InvalidForecastParametersException extends RuntimeException {

	InvalidForecastParametersException() {
		super();
	}

	public InvalidForecastParametersException(String s) {
		super(s);
	}

}
