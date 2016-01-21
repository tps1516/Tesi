package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import data.feature.Feature;

public class FeatureAveragesNode implements Iterable<TemporalWindow>,Cloneable,Serializable {
	
	private ArrayList<TemporalWindow> avgNode;
	
	
	private FeatureAveragesNode(){
		
	}
	
	
	FeatureAveragesNode(Node n){
		avgNode= new ArrayList<TemporalWindow>(n.getSchema().getTargetList().size());
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
		String s = "";
		
		for (TemporalWindow t: avgNode){
			s=s+t.toString()+"\n";
		}
		
		return s;
	}
	
	
	public FeatureAveragesNode Clone() throws CloneNotSupportedException{
		
		FeatureAveragesNode fan= new FeatureAveragesNode();
		ArrayList<TemporalWindow> avgCopy=new ArrayList<TemporalWindow>(this.avgNode.size());
		for (TemporalWindow t: avgNode){
			avgCopy.add(t.clone());
		}
		fan.avgNode=avgCopy;
		return fan;
		
	}
}
