package data.feature;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class KeyFeature extends Feature implements Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public KeyFeature(String name,int indexStream){
		super(name,indexStream);
	}
	public Object clone(){
		
		return super.clone();
			
}
	@Override
	public void computeAutocorrelation(AutocorrelationI a, SnapshotData data,
			SnapshotWeigth W, int beginIndex, int endIndex) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Object getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
