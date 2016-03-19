package snapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import data.GetisAndOrdZ;
import data.SensorPoint;
import data.datavalue.NumericValue;
import data.datavalue.Value;
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
		schema.reset();
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
				sf.setMax(new Double(str[indexStream])); // update max
				sf.setMin(new Double(str[indexStream])); // update min
				sf.setMean(new Double(str[indexStream])); // update mean
			}

			for (Feature f : schema.getTargetList()) {
				int indexStream = f.getIndexStream();
				int indexMining = f.getIndexMining();
				if (f instanceof NumericFeature) {

					if (!str[indexStream].equals("?")) {
						sp.addMeasure(new NumericValue(new Double(
								str[indexStream]), indexMining));

						((NumericFeature) f)
								.setMax(new Double(str[indexStream])); // update
																		// max
						((NumericFeature) f)
								.setMin(new Double(str[indexStream])); // update
																		// min
						((NumericFeature) f).setMean(new Double(
								str[indexStream])); // update mean
					} else
						sp.addMeasure(new NumericValue(null, indexMining));

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

	public void scaledSchema(SnapshotSchema schema) {
		for (Feature f : schema.getTargetList()) {
			if (f instanceof NumericFeature) {
				NumericFeature ff = (NumericFeature) f;
				double min = ff.getMin();
				double max = ff.getMax();
				double mean = 0.0;
				int ct = 0;
				for (int i = 0; i < size(); i++) {
					Value v = getSensorPoint(i).getMeasure(f.getIndexMining());
					if (!v.isNull()) {
						ct++;
						double scaledV = ((NumericValue) v).scale(min, max);
						mean += scaledV;
					}

				}
				mean /= ct;
				ff.setScaledMean(mean);
			}
		}

	}

	public void updateGetisAndOrd(SnapshotWeigth W, SnapshotSchema schema) {
		for (int i = 0; i < data.size(); i++) {

			for (Feature f : schema.getTargetList()) {
				// replace value with getis and ord
				if (f instanceof NumericFeature) {

					double getisandOrd = new GetisAndOrdZ().compute(this, f, W,
							i, ((NumericFeature) f).getMin(),
							((NumericFeature) f).getMax());
					((NumericValue) data.get(i).getMeasure(f.getIndexMining()))
							.setGetis(getisandOrd);
				}
			}
		}
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

	public int countDistinct(Feature f, int begin, int end) {
		int ct = 1;

		Double xOld = (Double) (data.get(begin).getMeasure(f.getIndexMining()))
				.getValue();
		for (int j = begin + 1; j <= end; j++) {
			Double x = (Double) (data.get(j).getMeasure(f.getIndexMining()))
					.getValue();
			if (!x.equals(xOld)) {
				ct++;
				xOld = x;
			}

		}
		return ct;

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

	public void sort(Feature f, int begin, int end) {
		quicksort(f, begin, end);
	}

	public void sort() {
		this.data.sort(null);
	}

	private int partition(Feature attribute, int inf, int sup) {
		int i, j;

		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		double x = (Double) (data.get(med).getMeasure(attribute
				.getIndexMining())).getValue();

		swap(inf, med);

		while (true) {
			double xi = (Double) (data.get(i).getMeasure(attribute
					.getIndexMining())).getValue();

			while (i <= sup && xi <= x) {
				i++;
				if (i <= sup)
					xi = (Double) (data.get(i).getMeasure(attribute
							.getIndexMining())).getValue();

			}

			double xj = (Double) (data.get(j).getMeasure(attribute
					.getIndexMining())).getValue();
			while (xj > x) {
				j--;
				xj = (Double) (data.get(j).getMeasure(attribute
						.getIndexMining())).getValue();

			}

			if (i < j) {
				swap(i, j);
			} else
				break;
		}
		swap(inf, j);
		return j;

	}

	// scambio esempio i con esempio j
	private void swap(int i, int j) {
		SensorPoint temp;
		temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);

	}

	private void quicksort(Feature attribute, int inf, int sup) {

		if (sup >= inf) {

			int pos;
			pos = partition(attribute, inf, sup);
			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			} else {
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}
		}

	}

	public String toString() {
		String str = "";
		Iterator<SensorPoint> it = data.iterator();
		while (it.hasNext()) {
			str += it.next() + "\n";
		}
		return str;

	}

	public void updateNull(SnapshotWeigth W, SnapshotSchema schema) {

		for (int i = 0; i < data.size(); i++) {

			for (Feature f : schema.getTargetList()) {
				// replace value with getis and ord
				if (f instanceof NumericFeature) {

					if (data.get(i).getMeasure(f.getIndexMining()).isNull()) {

						double sumWijXj = 0.0, sumWi = 0.0;
						for (int j = 0; j <= data.size() - 1; j++) {

							int id2 = getSensorPoint(j).getId();

							Value Xj = getSensorPoint(j).getMeasure(
									f.getIndexMining());

							if (!Xj.isNull()) {

								double wij = W.getWeight(getSensorPoint(i)
										.getId(), id2);

								if (wij != 0) {

									sumWijXj += wij * (Double) Xj.getValue();
									// sumWijXj+=wij*scaledXj;
									sumWi += wij;

								}

							}
						}
						Double idw = 0.0;
						if (sumWi != 0)
							idw = new Double(sumWijXj / sumWi);

						data.get(i).getMeasure(f.getIndexMining())
								.setValue(idw);
						((NumericFeature) f).setMean(idw);
						((NumericFeature) f).setMin(idw);
						((NumericFeature) f).setMax(idw);

					}
				}
			}
		}
	}

	@Override
	public Iterator<SensorPoint> iterator() {
		// TODO Auto-generated method stub
		return data.iterator();
	}

	public SnapshotData initializeSnapshotForecast(SnapshotSchema schemaTrain) {
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
}
