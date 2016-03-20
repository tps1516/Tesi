package data.feature;

import java.io.Serializable;

public abstract class Feature implements Serializable {
	protected String name;
	private int indexStream = -1;
	private int indexMining = -1;
	private int featureIndex = -1;

	Feature(String name, int indexStream) {
		this.name = name;
		this.setIndexStream(indexStream);

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

	public String toString() {
		return name;
	}
}
