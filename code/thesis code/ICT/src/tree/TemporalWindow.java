package tree;



import org.apache.commons.collections4.queue.CircularFifoQueue;

import data.feature.Feature;

public class TemporalWindow<Object> extends CircularFifoQueue<Object> implements Cloneable{

	/*
	 * dimensione massima della 
	 * finestra temporale
	 */
	private int windowMaxSize;
	
	private String featureName;
	private TemporalWindow(int dim){
		super(dim);
	}
	/*
	 * costruttore della classe
	 * istanzia la coda in base alla dimensione massima
	 * e memorizza il nome della feature a cui fa riferimento 
	 * la finestra temporale
	 */
	TemporalWindow(Feature f, int dim){
		super(dim);
		this.featureName=f.getName();
		windowMaxSize=dim;
	}
	
	/*
	 *  restituisce la dimensione massima
	 *  raggiungile dalla coda
	 */
	public int getwindowMaxSize(){
		return windowMaxSize;
	}
	
	/*
	 * aggiorna la finestra temporale 
	 * aggiungendo ad essa la nuova media
	 */
	void updateTemporalWindow(Feature f) {
		this.add((Object) f.getPrototype());
	}
	
	/*
	 * aggiorna la finestra temporale
	 * aggiungendo ad essa null
	 */
	void insLast(TemporalWindow tw){
		this.add((Object) tw.get(tw.size()-1));
	}
	
	
	public String toString(){
		String s=this.featureName+ ": ";
		
		for (Object o: this){
			s=s+o + " ";
		}
		
		return s;
	}
	
	
	public TemporalWindow clone() throws CloneNotSupportedException{
		
		TemporalWindow t=new TemporalWindow(this.getwindowMaxSize());
		for(Object o: this){
			t.add(o);
		};
		t.featureName=this.featureName;
		t.windowMaxSize=this.windowMaxSize;
		return t;
	}
	
	
}
