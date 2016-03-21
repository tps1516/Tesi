package forecast;

public class NotForecastingException extends Exception {

	NotForecastingException() {
		super();
	}

	NotForecastingException(String error) {
		super(error);
	}
}
