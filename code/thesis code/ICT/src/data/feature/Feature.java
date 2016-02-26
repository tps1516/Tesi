package data.feature;

import java.io.Serializable;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public abstract class Feature implements Serializable, Cloneable {
	protected String name;
	private int indexStream = -1;
	private int indexMining = -1;
	private int featureIndex = -1;
	protected int countTuples = 0;

	protected double autocorrelationMeasure = Double.NEGATIVE_INFINITY;

	private boolean stopTree = false;

	Feature(String name, int indexStream) {
		this.name = name;
		this.setIndexStream(indexStream);

	}

	public Boolean getStopTree() {
		return stopTree;
	}

	public void setStopTree(Boolean cond) {
		stopTree = cond;
	}

	void setIndex(int index) {
		indexMining = index;
	}

	public String getName() {
		return name;
	}

	public void setIndexStream(int indexStream) {
		this.indexStream = indexStream;
	}

	public int getIndexStream() {
		return indexStream;
	}

	public void setFeatureIndex(int nSpatial) {
		this.featureIndex = this.indexMining - nSpatial;
	}

	public int getFeatureIndex() {
		return this.featureIndex;
	}

	public void setIndexMining(int indexMining) {
		this.indexMining = indexMining;
	}

	public int getIndexMining() {
		return indexMining;
	}

	public int getCountTuples() {
		return countTuples;
	}

	// public abstract Object getPrototype();
	// public abstract Object getStreamPrototype();

	public abstract Object getPrototype();

	public abstract void computeAutocorrelation(AutocorrelationI a,
			SnapshotData data, SnapshotWeigth W, int beginIndex, int endIndex);

	// Annalisa Added for optimization
	public void setAutocorrelation(double a) {
		autocorrelationMeasure = a;
	}

	public double getAutocorrelation() {
		return autocorrelationMeasure;
	}

	public void clear() {
		countTuples = 0;
	}

	public String toString() {
		return name;
	}

	public Object clone() {
		try {
			Object o = super.clone();
			((Feature) o).stopTree = stopTree;
			((Feature) o).autocorrelationMeasure = autocorrelationMeasure;
			((Feature) o).indexMining = indexMining;
			((Feature) o).indexStream = indexStream;

			return o;
		} catch (CloneNotSupportedException e) {

			e.printStackTrace();
		}
		return null;

	}

	// abstract public void setPrototype(SnapshotData data, SnapshotWeigth W,
	// int beginIndex, int endIndex) ;

}
