package windowStructure;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import data.feature.Feature;

public class TemporalWindow<Double> extends CircularFifoQueue<Double> implements
		Cloneable {

	/*
	 * dimensione massima della finestra temporale
	 */
	private int windowMaxSize;

	private String feature;

	private TemporalWindow(int dim) {
		super(dim);
	}

	/*
	 * costruttore della classe istanzia la coda in base alla dimensione massima
	 * e memorizza il nome della feature a cui fa riferimento la finestra
	 * temporale
	 */
	TemporalWindow(Feature f, int dim) {
		super(dim);
		this.feature = f.getName();
		windowMaxSize = dim;
	}

	/*
	 * restituisce la dimensione massima raggiungile dalla coda
	 */
	int getwindowMaxSize() {
		return windowMaxSize;
	}

	void updateTemporalWindow(Double value) {
		this.add(value);
	}

	public String toString() {
		String s = this.feature + ": ";

		for (Double d : this) {
			s = s + d + " ";
		}

		return s;
	}

}
