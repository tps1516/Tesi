package data.feature;

import data.datavalue.NumericValue;
import data.datavalue.Value;
import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class ResubstitutionIndex implements AutocorrelationI {

	private double sumX = 0;
	private double sumX2 = 0;
	private int N = 0;

	public ResubstitutionIndex() {

	}

	public ResubstitutionIndex(double sumX, double sumX2, int N) {
		// TODO Auto-generated constructor stub
		this.sumX = sumX;
		this.sumX2 = sumX2;
		this.N = N;
	}

	public ResubstitutionIndex(SnapshotData data, Feature feature,
			SnapshotWeigth W, int beginIndex, int endIndex) {
		Double R = 0.0;
		NumericFeature f = (NumericFeature) feature;
		sumX = 0.0;
		sumX2 = 0.0;
		int featureIndex = f.getIndexMining();
		N = 0;
		for (int i = beginIndex; i <= endIndex; i++) {

			int id1 = data.getSensorPoint(i).getId();
			Value Xi = data.getSensorPoint(i).getMeasure(featureIndex);
			if (!Xi.isNull()) {
				N++;
				double scaledValue = ((NumericValue) Xi).scale(f.getMin(),
						f.getMax());
				// sumX+=(Double)(Xi.getValue());
				sumX += scaledValue;
				// sumX2+=Math.pow((Double)(Xi.getValue()),2);
				sumX2 += Math.pow(scaledValue, 2);
			}
		}
		// R=sumX2/N-Math.pow(sumX/N,2);

	}

	public void addX(SnapshotData trainingSet, int begin, int end,
			SnapshotWeigth W, Feature feature) // X è in posizione end+1
	// public void addX(Value X,Feature feature)
	{
		NumericFeature f = (NumericFeature) feature;
		Value X = trainingSet.getSensorPoint(end + 1).getMeasure(
				feature.getIndexMining());
		if (!X.isNull()) {
			double scaledValue = ((NumericValue) X).scale(f.getMin(),
					f.getMax());
			sumX += scaledValue;
			sumX2 += Math.pow(scaledValue, 2);
			N++;
		}
	}

	public void subX(SnapshotData trainingSet, int begin, int end,
			SnapshotWeigth W, Feature feature) // X è in posizione begin -1
	// public void subX(Value X, Feature feature)
	{
		NumericFeature f = (NumericFeature) feature;
		Value X = trainingSet.getSensorPoint(begin - 1).getMeasure(
				feature.getIndexMining());
		if (!X.isNull()) {
			double scaledValue = ((NumericValue) X).scale(f.getMin(),
					f.getMax());
			sumX -= scaledValue;
			sumX2 -= Math.pow(scaledValue, 2);
			N--;
		}
	}

	public double getSumX() {
		return sumX;
	}

	public double getSumX2() {
		return sumX2;
	}

	public int getN() {
		return N;
	}

	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,
			int beginIndex, int endIndex) {
		System.out.println("****Non mi fermo mai qui******");
		Double R = 0.0;
		NumericFeature f = (NumericFeature) feature;
		double sumX = 0.0;
		double sumX2 = 0.0;
		int featureIndex = f.getIndexMining();
		N = 0;
		for (int i = beginIndex; i <= endIndex; i++) {

			int id1 = data.getSensorPoint(i).getId();
			Value Xi = data.getSensorPoint(i).getMeasure(featureIndex);
			if (!Xi.isNull()) {
				N++;
				double scaledValue = ((NumericValue) Xi).scale(f.getMin(),
						f.getMax());
				// sumX+=(Double)(Xi.getValue());
				sumX += scaledValue;
				// sumX2+=Math.pow((Double)(Xi.getValue()),2);
				sumX2 += Math.pow(scaledValue, 2);
			}
		}
		R = sumX2 / N - Math.pow(sumX / N, 2);
		return R;
	}

	public Double get() {
		double R = sumX2 / N - Math.pow(sumX / N, 2);
		return R;
	}

	@Override
	public boolean isMax(double v) {
		return v == 0.0;
	}

}
