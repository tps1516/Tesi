package snapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

//import mbrModel.MBR;


import data.SensorPoint;
import data.datavalue.CategoricalValue;
import data.datavalue.NumericValue;
import data.datavalue.Value;
import data.feature.CategoricalFeature;
import data.feature.Feature;
import data.feature.NumericFeature;
import data.feature.SpatialFeature;

public class SnapshotData implements Iterable<SensorPoint> {
	private List<SensorPoint> data = new ArrayList<SensorPoint>();
	private int idSnapshot;
	private static int idSnapshotGenerator = 1;

	private static int numberOfSpatialFeatures = 2;
	private static int numberOfTargetFeatures = 1;
	private static int idSensorGenerator = 1;
	static int idCode = 0;

	public static void resetIdSnapshotGenerator() {
		idSnapshotGenerator = 1;
	}

	private boolean inputSnaphot(BufferedReader stream, SnapshotSchema schema)
			throws IOException {
		String inLine = stream.readLine(); // prima tupla
		if (inLine == null)
			return false;
		numberOfSpatialFeatures = schema.getSpatialList().size();
		numberOfTargetFeatures = schema.getTargetList().size();

		Set<Integer> idSet = new TreeSet<Integer>();
		
		// System.out.println(schema);

		while (inLine != null && !inLine.equals("@")) {

			String str[] = inLine.split(",");

			SensorPoint sp;
			if (schema.getKey() != null) {
				Integer idKey = new Integer(str[schema.getKey()
						.getIndexStream()]);
				if (idSet.contains(idKey)) {
					System.out.println("DUPLICATE:" + inLine);
					inLine = stream.readLine(); // skip line

					continue;
				}
				sp = new SensorPoint(idKey);
				idSet.add(idKey);
			} else
				sp = new SensorPoint(idSensorGenerator++);

			for (SpatialFeature sf : schema.getSpatialList()) {
				int indexStream = sf.getIndexStream();
				int indexMining = sf.getIndexMining();
				sp.addMeasure(new NumericValue(new Double(str[indexStream]),
						indexMining));
			
			}

			for (Feature f : schema.getTargetList()) {
				int indexStream = f.getIndexStream();
				int indexMining = f.getIndexMining();
				if (f instanceof NumericFeature) {

					if (!str[indexStream].equals("?")) {
						sp.addMeasure(new NumericValue(new Double(
								str[indexStream]), indexMining));

					} else
						sp.addMeasure(new NumericValue(null, indexMining));

				} else if (f instanceof CategoricalFeature) {

					if (!str[indexStream].equals("?")) {
						sp.addMeasure(new CategoricalValue(str[indexStream],
								indexMining));
					} else
						sp.addMeasure(new CategoricalValue(null, indexMining));
				}
			}

			data.add(sp);
			inLine = stream.readLine();

		}
		// System.out.println(schema);
		if (inLine == null) {
			// System.out.println(SnapshotData.idSnapshotGenerator);
			// throw new EOFException();
		}
		return true;

	}

	public SnapshotData(BufferedReader stream, SnapshotSchema schema)
			throws IOException {

		idSnapshot = idSnapshotGenerator++;
		boolean flag = inputSnaphot(stream, schema);
		if (!flag)
			throw new IOException("End Of Stream");
	}

	public SnapshotData(BufferedReader stream, SnapshotSchema schema, boolean b)
			throws IOException {

		idSnapshot = idSnapshotGenerator++;
		boolean flag = inputSnaphot(stream, schema);
		idSnapshotGenerator = idSnapshotGenerator - 1;
		if (!flag)
			throw new IOException("End Of Stream");
	}

	public SnapshotData(ArrayList<SensorPoint> data) {
		this.data = data;
	}

	public SensorPoint getSensorPoint(int index) {
		return data.get(index);
	}

	public int size() {
		return data.size();
	}

	public int getIdSnapshot() {
		return idSnapshot;
	}

	public int getSpatialFeaturesSize() {
		return numberOfSpatialFeatures;
	}

	public int getTargetFeaturesSize() {
		return numberOfTargetFeatures;
	}



	private ArrayList<SensorPoint> getData() {
		return (ArrayList<SensorPoint>) this.data;
	}

	public SnapshotData mergeSnapshotData(SnapshotData snap,
			SnapshotSchema schema) {

		ArrayList<SensorPoint> realData = this.getData();
		ArrayList<SensorPoint> networkData = snap.getData();
		ArrayList<SensorPoint> resultData = new ArrayList<SensorPoint>();

		int j = 0;
		for (int i = 0; i < realData.size(); i++) {
			boolean flag = false;

			if (realData.get(i).getId() == networkData.get(j).getId()) {
				SensorPoint sp = new SensorPoint(realData.get(i).getId());
				for (Feature f : schema.getSpatialList()) {
					double value = ((double) realData.get(i)
							.getMeasure(f.getIndexMining()).getValue());
					NumericValue nv = new NumericValue(value,
							f.getIndexMining());
					sp.addMeasure((Value) nv);
				}

				for (Feature f : schema.getTargetList()) {
					Double value = ((Double) realData.get(i)
							.getMeasure(f.getIndexMining()).getValue());
					if (value == null) {
						sp.addMeasure(networkData.get(j).getMeasure(
								f.getIndexMining()));
					} else {
						sp.addMeasure(realData.get(i).getMeasure(
								f.getIndexMining()));
					}
				}

				resultData.add(sp);
				j = j + 1;
			} else {
				while (!flag) {
					resultData.add(networkData.get(j));
					j = j + 1;
					if (realData.get(i).getId() == networkData.get(j).getId()) {
						resultData.add(realData.get(i));
						flag = true;
						j = j + 1;
					}
				}
			}
		}

		if (j != networkData.size()) {
			for (int i = j; i < networkData.size(); i++) {
				resultData.add(networkData.get(i));
			}
		}

		return new SnapshotData(resultData);
	}

	

	public void sort() {
		this.data.sort(null);
	}

	public String toString() {
		String str = "";
		Iterator<SensorPoint> it = data.iterator();
		while (it.hasNext()) {
			str += it.next() + "\n";
		}
		return str;

	}

	@Override
	public Iterator<SensorPoint> iterator() {
		// TODO Auto-generated method stub
		return data.iterator();
	}

	public SnapshotData initialize(SnapshotSchema schemaTrain) {
		/*
		 * Crea lo snapshot completo (reale + previsto), assegnando valore
		 * Double.MAX_VALUE a tutti le feature di tutti i SensorPoint
		 */
		ArrayList<SensorPoint> fusedNetwork = new ArrayList<SensorPoint>();
		for (SensorPoint sp : this) {
			SensorPoint sensorPoint = new SensorPoint(sp.getId());

			for (Feature f : schemaTrain.getSpatialList()) {
				double value = ((double) sp.getMeasure(f.getIndexMining())
						.getValue());
				NumericValue nv = new NumericValue(value, f.getIndexMining());
				sensorPoint.addMeasure((Value) nv);
			}

			for (Feature f : schemaTrain.getTargetList()) {
				NumericValue nv = new NumericValue(Double.MAX_VALUE,
						f.getIndexMining());
				sensorPoint.addMeasure((Value) nv);
			}

			fusedNetwork.add(sensorPoint);
		}

		SnapshotData snapNetwork = new SnapshotData(fusedNetwork);
		snapNetwork.sort();
		return snapNetwork;
	}

	public LinkedList<Double> computeRMSE(SnapshotData snap,
			SnapshotSchema schema) {
		LinkedList<Double> res = new LinkedList<Double>();

		for (Feature f : schema.getTargetList()) {
			res.add(0.0);
		}

		ArrayList<SensorPoint> realData = this.getData();
		ArrayList<SensorPoint> forecastedData = snap.getData();
		Double prev = 0.0;
		int j = 0;
		for (SensorPoint spReal : this) {
			boolean flag = false;

			if (spReal.getId() == forecastedData.get(j).getId()) {
				for (Feature f : schema.getTargetList()) {
					Double dif = Math.pow(
							(Double) ((Double) spReal.getMeasure(
									f.getIndexMining()).getValue())
									- ((Double) forecastedData.get(j)
											.getMeasure(f.getIndexMining())
											.getValue()), 2);
					prev = res.get(f.getFeatureIndex());
					res.set(f.getFeatureIndex(), dif + prev);
				}

				j = j + 1;

			} else {
				while (!flag) {
					j = j + 1;
					System.out.print(" SNAP : " + this.idSnapshot + "\n i: "
							+ spReal.getId() + " j: " + j);
					if (spReal.getId() == forecastedData.get(j).getId()) {
						for (Feature f : schema.getTargetList()) {
							Double dif = Math.pow((Double) ((Double) spReal
									.getMeasure(f.getIndexMining()).getValue())
									- ((Double) forecastedData.get(j)
											.getMeasure(f.getIndexMining())
											.getValue()), 2);
							prev = res.get(f.getFeatureIndex());
							res.set(f.getFeatureIndex(), dif + prev);
						}
						flag = true;
						j = j + 1;
					}
				}
			}
		}
		int i = 0;
		for (Double d : res) {
			prev = res.get(i);
			prev /= this.size();
			prev = Math.sqrt(prev);
			res.set(i, prev);
			i = i + 1;
		}
		return res;
	}
}
