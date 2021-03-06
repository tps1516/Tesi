package windowStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import snapshot.SnapshotSchema;
import data.SensorPoint;
import data.feature.Feature;

/*
 * ha la responsabilitą di tener traccia dei flusso di dati per feature (nello schema)
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
		 * che conterrą i suoi valori nel tempo e la memorizzo nell'arrayList in
		 * base all'indice assunto dalla feature stessa nello schema per
		 * facilitarne il ritrovamento
		 */
		for (Feature f : schema.getTargetList()) {
			featureWindow.add(f.getFeatureIndex(), new TemporalWindow(f, dim));
		}
	}

	/*
	 * tale metodo ha la responsabilitą di aggiornare la timeseries associata ad
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
}
