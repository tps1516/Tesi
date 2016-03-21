package windowStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import snapshot.SnapshotSchema;
import tree.Node;
import data.SensorPoint;
import data.feature.Feature;

/*
 * ha la responsabilità di tener traccia dei flusso di dati per feature (nello schema)
 * nel tempo memorizzando per ciascuna feature i suo valori in una coda di dimensione massima
 */
public class FeatureWindow implements Iterable<TemporalWindow>, Cloneable,
		Serializable {

	private ArrayList<TemporalWindow> featureWindow;

	/*
	 * costruttore privato viene usato solo per il clone
	 */
	private FeatureWindow() {
	}

	/*
	 * costruttore della classe tale costruttore viene utilizzato quando la
	 * timeseries viene associata a ciascun sensore nella rete
	 */
	public FeatureWindow(SnapshotSchema schema, int dim) {

		// istanzio l'arrayList di temporalwindow
		featureWindow = new ArrayList<TemporalWindow>(schema.getTargetList()
				.size());

		/*
		 * per ciasucna feature nello schema istanzio una nuova temporalWindow
		 * che conterrà i suoi valori nel tempo e la memorizzo nell'arrayList in
		 * base all'indice assunto dalla feature stessa nello schema per
		 * facilitarne il ritrovamento
		 */
		for (Feature f : schema.getTargetList()) {
			featureWindow.add(f.getFeatureIndex(), new TemporalWindow<Double>(
					f, dim));
		}
	}

	/*
	 * costruttore della classe esso viene utilizzato quando la timeseries viene
	 * associata ad un cluster e serve per memorizzare per ciascuna feature la
	 * media che essa assume nel cluster
	 */
	public FeatureWindow(Node n, int dim) {
		// istanzio l'arrayList di temporalwindow
		featureWindow = new ArrayList<TemporalWindow>(n.getSchema()
				.getTargetList().size());
		int spazialFeatureSize = n.getSchema().getSpatialList().size();

		/*
		 * per ciasucna feature nello schema istanzio una nuova temporalWindow
		 * che conterrà i suoi valori nel tempo e la memorizzo nell'arrayList in
		 * base all'indice assunto dalla feature stessa nello schema per
		 * facilitarne il ritrovamento
		 */
		for (Feature f : n.getSchema().getTargetList()) {
			featureWindow.add(f.getIndexMining() - spazialFeatureSize,
					new TemporalWindow(f, dim));
		}
	}

	/*
	 * tale metodo ha la responsabilità di aggiornare la timeseries associata ad
	 * un nodo (cluster) aggiungendo nella coda di valori di ciascuna feature il
	 * valore della media dalla feature nel cluster
	 */
	public void updateAverages(Node n) {
		int spazialFeatureSize = n.getSchema().getSpatialList().size();
		for (Feature f : n.getSchema().getTargetList()) {
			featureWindow.get(f.getIndexMining() - spazialFeatureSize)
					.updateTemporalWindow(f);
			;
		}
	}

	/*
	 * tale metodo ha la responsabilità di aggiornare la timeseries associata ad
	 * uno specifico sensore aggiungendo nella coda di valori di ciascuna
	 * feature il valore rilevato dal sensore per quella feature
	 */
	public void updateSensorFeature(SensorPoint sensorPoint,
			SnapshotSchema schema) {

		/*
		 * ciclo sulle feature per aggiornare le corrispondenti code
		 */
		for (Feature f : schema.getTargetList()) {

			/*
			 * recupero il valore memorizzato nel sensore per quella feature e
			 * aggiorno la coda
			 */
			Double value = (Double) sensorPoint.getMeasure(f.getIndexMining())
					.getValue();
			if (value == null)
				value = Double.MAX_VALUE;
			featureWindow.get(f.getFeatureIndex()).updateTemporalWindow(value);
		}

	}

	public void insLastOfFather(FeatureWindow fatherAvg) {
		ArrayList<TemporalWindow> arrayListFather = fatherAvg.getArrayList();
		for (int i = 0; i < featureWindow.size(); i++) {
			featureWindow.get(i).insLast(arrayListFather.get(i));
		}
	}

	private ArrayList<TemporalWindow> getArrayList() {
		return this.featureWindow;
	}

	public Iterator<TemporalWindow> iterator() {
		return featureWindow.iterator();
	}

	public String toString() {
		String s = "";

		for (TemporalWindow t : featureWindow) {
			s = s + t.toString() + "\n";
		}

		return s;
	}

	public FeatureWindow Clone() throws CloneNotSupportedException {

		FeatureWindow fw = new FeatureWindow();
		ArrayList<TemporalWindow> copy = new ArrayList<TemporalWindow>(
				this.featureWindow.size());
		for (TemporalWindow t : featureWindow) {
			copy.add(t.clone());
		}
		fw.featureWindow = copy;
		return fw;

	}

	/*
	 * verifica se le code memorizzate hanno raggiunto la loro dimensione
	 * massima
	 */
	public boolean temporalWindowsIsFull() {

		TemporalWindow tw = featureWindow.get(0);

		return (tw.size() == tw.maxSize());

	}

	public double[][] exportInMatrixForm() {

		double[][] dataset = new double[this.featureWindow.get(0).size()][this.featureWindow
				.size()];

		for (int i = 0; i < featureWindow.size(); i++) {
			int j = 0;
			TemporalWindow<Double> tw = featureWindow.get(i);
			for (double value : tw) {
				dataset[j][i] = value;
				j++;
			}
		}

		return dataset;

	}

	public int getTWMaxSize() {
		return featureWindow.get(0).maxSize();
	}

	public TemporalWindow getTemporalWindow(Feature f) {
		return featureWindow.get(f.getFeatureIndex());
	}

	public int size() {
		return featureWindow.get(0).size();
	}

}
