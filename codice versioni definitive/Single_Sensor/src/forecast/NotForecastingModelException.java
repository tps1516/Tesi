package forecast;

public class NotForecastingModelException extends Exception {

	NotForecastingModelException() {
		super();
	}

	NotForecastingModelException(String error) {
		super(error);
	}
}
