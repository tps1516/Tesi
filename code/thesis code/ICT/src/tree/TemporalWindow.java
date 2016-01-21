package tree;



import org.apache.commons.collections4.queue.CircularFifoQueue;

import data.feature.Feature;

public class TemporalWindow<Object> extends CircularFifoQueue<Object> implements Cloneable{

	/*
	 * dimensione massima della 
	 * finestra temporale
	 */
	private static int windowMaxSize;
	
	private String featureName;
	private TemporalWindow(){
		super(windowMaxSize);
	}
	/*
	 * costruttore della classe
	 * istanzia la coda in base alla dimensione massima
	 * e memorizza il nome della feature a cui fa riferimento 
	 * la finestra temporale
	 */
	TemporalWindow(Feature f){
		super(windowMaxSize);
		this.featureName=f.getName();
	}
	
	/*
	 * metodo di classe
	 * setta l'attributo window max size
	 */
	public static void setWindowMaxSize(int dim){
		windowMaxSize=dim;
	}
	
	
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
	void insNull(){
		this.add((Object) "null");
	}
	
	
	public String toString(){
		String s=this.featureName+ ": ";
		
		for (Object o: this){
			s=s+o + " ";
		}
		
		return s;
	}
	
	
	public TemporalWindow clone() throws CloneNotSupportedException{
		
		TemporalWindow t=new TemporalWindow();
		for(Object o: this){
			t.add(o);
		};
		t.featureName=this.featureName;
		return t;
	}
	
	
}
