package tree;

import java.util.ArrayList;
import java.util.Iterator;

import data.feature.Feature;

public class FeatureAveragesNode implements Iterable<TemporalWindow>,Cloneable {
	
	private ArrayList<TemporalWindow> avgNode;
	
	FeatureAveragesNode(Node n){
		avgNode= new ArrayList<TemporalWindow>();
		int spazialFeatureSize=n.getSchema().getSpatialList().size();
		for (Feature f: n.getSchema().getTargetList()) {
			avgNode.add(f.getIndexMining()-spazialFeatureSize, new TemporalWindow(f));
		}
	}
	
	void updateAverages(Node n){
		int spazialFeatureSize=n.getSchema().getSpatialList().size();
		for (Feature f: n.getSchema().getTargetList()) {
			avgNode.get(f.getIndexMining()-spazialFeatureSize).updateTemporalWindow(f);;
		}
	}
	
	void avgNull(){
		for (TemporalWindow t: avgNode){
			t.insNull();
		}
	}

	
	public Iterator<TemporalWindow> iterator() {
		return avgNode.iterator();
	}
	
	
	public String toString(){
		String s = null;
		
		for (TemporalWindow t: avgNode){
			s=s+t.toString()+"\n";
		}
		
		return s;
	}
	
	/*
	 * implementare il metodo
	 */
	public FeatureAveragesNode Clone(){
		return null;
	}
}
