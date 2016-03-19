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

	/*
	 * aggiorna la finestra temporale aggiungendo ad essa la nuova media
	 */
	void updateTemporalWindow(Feature f) {
		this.add((Double) f.getPrototype());
	}

	void updateTemporalWindow(Double value) {
		this.add(value);
	}

	/*
	 * aggiorna la finestra temporale aggiungendo ad essa il valore assunto dal
	 * padre
	 */
	public void insLast(TemporalWindow tw) {
		this.add((Double) tw.get(tw.size() - 1));
	}

	public String toString() {
		String s = this.feature + ": ";

		for (Double d : this) {
			s = s + d + " ";
		}

		return s;
	}

	public TemporalWindow clone() throws CloneNotSupportedException {

		TemporalWindow t = new TemporalWindow(this.getwindowMaxSize());
		for (Double d : this) {
			t.add(d);
		}
		t.feature = this.feature;
		t.windowMaxSize = this.windowMaxSize;
		return t;
	}

	public void setValue(Double d) {
		this.updateTemporalWindow(d);
	}

	public String getFeature() {
		return this.feature;
	}
}
