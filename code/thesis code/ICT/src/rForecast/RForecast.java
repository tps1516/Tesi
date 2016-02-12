package rForecast;

import java.util.ArrayList;

import snapshot.SnapshotSchema;

public abstract class RForecast {

	public abstract ArrayList<Object> RForecasting(double[][] dataset, SnapshotSchema schema, ArrayList<Object> rParameters, int TWSize);
}
