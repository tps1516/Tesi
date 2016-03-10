package forecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import snapshot.SnapshotSchema;
import snapshot.SnapshotData;

import data.Network;
import data.SensorPoint;
import data.datavalue.NumericValue;
import data.datavalue.Value;
import data.feature.Feature;

public class Forecasting {
	private int nahead;
	private SnapshotSchema schema;

	private SnapshotData data;
	private Network network;
	private HashMap<Integer, ArrayList<SensorPoint>> naheadToSnapData;

	public Forecasting(int nahead, SnapshotSchema schema, 
			SnapshotData data, Network network) {
		this.nahead = nahead;
		this.schema = schema;
	
		this.data = data;
		this.network = network;
		this.naheadToSnapData = new HashMap<Integer, ArrayList<SensorPoint>>();
	}

	public LinkedList<SnapshotData> forecasting() {
		for (int i = 0; i < nahead; i++) {
			naheadToSnapData.put(i, null);
		}
		
		data.sort();
		double[] forecast;
		SensorForecast sensorForecast = new SensorForecast(schema);
		for (SensorPoint sp : data) {
			ForecastingModel VARModel = VARModels.get(sp.getId());
			double[][] matrix = network.getTemporalWindow(sp.getId())
					.exportInMatrixForm();
			for (int i = 0; i < this.nahead; i++) {
				forecast = sensorForecast.sensorForecasting(matrix, VARModel);
				if (forecast == null) {
					for (int indexNAhead = i; indexNAhead < this.nahead; indexNAhead++) {
						SensorPoint spF = createSP(sp, null);
						addInHM(i, spF);
						i=i+1;
					}
					break;
				}
				SensorPoint spF = createSP(sp, forecast);
				addInHM(i, spF);
				matrix = Forecasting.shiftData(matrix, forecast);
			}
		}
		return returnResult();
	}

	private static double[][] shiftData(double[][] matrix, double[] forecast) {
		double[][] result = new double[matrix.length][matrix[0].length];
		int i;
		for (i = 1; i < matrix.length; i++)
			for (int j = 0; j < matrix[0].length; j++) {
				result[i - 1][j] = matrix[i][j];
			}
		for (int j = 0; j < forecast.length; j++) {
			result[i-1][j] = forecast[j];
		}
		return result;
	}

	private LinkedList<SnapshotData> returnResult() {
		LinkedList<SnapshotData> result = new LinkedList<SnapshotData>();
		for (int i = 0; i < nahead; i++) {
			SnapshotData SnapData = new SnapshotData(naheadToSnapData.get(i));
			result.add(i, SnapData);
		}
		return result;
	}

	private SensorPoint createSP(SensorPoint sp, double[] forecast) {
		SensorPoint sensorPoint = new SensorPoint(sp.getId());

		for (Feature f : this.schema.getSpatialList()) {
			double value = ((double) sp.getMeasure(f.getIndexMining())
					.getValue());
			NumericValue nv = new NumericValue(value, f.getIndexMining());
			sensorPoint.addMeasure((Value) nv);
		}
		if (forecast != null) {
			for (Feature f : this.schema.getTargetList()) {
				NumericValue nv = new NumericValue(
						forecast[f.getFeatureIndex()], f.getIndexMining());
				sensorPoint.addMeasure((Value) nv);
			}
		} else {
			for (Feature f : this.schema.getTargetList()) {
				NumericValue nv = new NumericValue(Double.MAX_VALUE,
						f.getIndexMining());
				sensorPoint.addMeasure((Value) nv);
			}
		}
		return sensorPoint;
	}

	private void addInHM(int i, SensorPoint spF) {
		if (naheadToSnapData.get(i) != null) {
			naheadToSnapData.get(i).add(spF);
		} else {
			ArrayList<SensorPoint> sensorsPoint = new ArrayList<SensorPoint>();
			sensorsPoint.add(spF);
			naheadToSnapData.put(i, sensorsPoint);
		}

	}

}
