package tree;

import java.io.Serializable;

public class FeatureMeanNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final int[] l = { 0 };


	public void maxSize(int c) {
		l[0] = c;
	}

	public Object get() {
		return null;
	}

	public void put(Node n) {
		final int MAX = l[0];
		
	}
}
