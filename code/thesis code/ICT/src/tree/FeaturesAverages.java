package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import data.feature.Feature;

public class FeaturesAverages implements Iterable<TemporalWindow>, Cloneable,
		Serializable {

	private ArrayList<TemporalWindow> avgNode;

	private FeaturesAverages() {
	}

	FeaturesAverages(Node n, int dim) {
		avgNode = new ArrayList<TemporalWindow>(n.getSchema().getTargetList()
				.size());
		int spazialFeatureSize = n.getSchema().getSpatialList().size();
		for (Feature f : n.getSchema().getTargetList()) {
			avgNode.add(f.getIndexMining() - spazialFeatureSize,
					new TemporalWindow(f, dim));
		}
	}

	void updateAverages(Node n) {
		int spazialFeatureSize = n.getSchema().getSpatialList().size();
		for (Feature f : n.getSchema().getTargetList()) {
			avgNode.get(f.getIndexMining() - spazialFeatureSize)
					.updateTemporalWindow(f);
			;
		}
	}

	void insLastOfFather(FeaturesAverages fatherAvg) {
		ArrayList<TemporalWindow> arrayListFather = fatherAvg.getArrayList();
		for (int i = 0; i < avgNode.size(); i++) {
			avgNode.get(i).insLast(arrayListFather.get(i));
		}
	}

	private ArrayList<TemporalWindow> getArrayList() {
		return this.avgNode;
	}

	public Iterator<TemporalWindow> iterator() {
		return avgNode.iterator();
	}

	public String toString() {
		String s = "";

		for (TemporalWindow t : avgNode) {
			s = s + t.toString() + "\n";
		}

		return s;
	}

	public FeaturesAverages Clone() throws CloneNotSupportedException {

		FeaturesAverages fan = new FeaturesAverages();
		ArrayList<TemporalWindow> avgCopy = new ArrayList<TemporalWindow>(
				this.avgNode.size());
		for (TemporalWindow t : avgNode) {
			avgCopy.add(t.clone());
		}
		fan.avgNode = avgCopy;
		return fan;

	}

	boolean temporalWindowsIsFull() {

		TemporalWindow tw = avgNode.get(0);

		return (tw.size() == tw.maxSize());

	}

	double[][] exportInMatrixForm() {

		double[][] dataset = new double[this.avgNode.get(0).size()][this.avgNode
				.size()];

		for (int i = 0; i < avgNode.size(); i++) {
			int j = 0;
			TemporalWindow<Double> tw = avgNode.get(i);
			for (double value : tw) {
				dataset[j][i] = value;
				j++;
			}
		}

		return dataset;

	}
}
