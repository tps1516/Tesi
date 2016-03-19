package rForecast;

import java.util.ArrayList;
import java.util.HashMap;

import snapshot.SnapshotSchema;

public abstract class RForecast {

	public abstract HashMap<String, ArrayList<Object>> RForecasting(int id,
			double[][] dataset, SnapshotSchema schema,
			ArrayList<Object> rParameters);
}
