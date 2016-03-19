package snapshot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.GregorianCalendar;
import data.DistanceI;

abstract public class SnapshotWeigth implements Serializable {

	/**
	 * 
	 */

	protected Double b;
	protected GregorianCalendar start;
	protected GregorianCalendar end;

	public SnapshotWeigth(double b) {
		start = new GregorianCalendar();
		this.b = b;

	}

	public static double maxDist(SnapshotData data, DistanceI distance) {
		double maxD = -1.0;

		for (int i = 0; i < data.size(); i++)
			for (int j = i; j < data.size(); j++) {
				String key = data.getSensorPoint(i).getId() + ":"
						+ data.getSensorPoint(j).getId();
				if (data.getSensorPoint(i).getId() != data.getSensorPoint(j)
						.getId()) {
					double d = distance.compute(data.getSensorPoint(i),
							data.getSensorPoint(j));
					if (d > maxD)
						maxD = d;
				}
			}
		// System.out.println("Maximum distance is:"+maxD);
		return maxD;
	}

	/*
	 * public static double maxofMinDist(SnapshotData data, DistanceI distance)
	 * { double maxD=-1.0;
	 * 
	 * 
	 * for (int i=0;i<data.size();i++){ double minI=Double.MAX_VALUE; for(int
	 * j=0;j<data.size();j++) { String
	 * key=data.getSensorPoint(i).getId()+":"+data.getSensorPoint(j).getId();
	 * if(data.getSensorPoint(i).getId()!=data.getSensorPoint(j).getId()) {
	 * double d=distance.compute(data.getSensorPoint(i),
	 * data.getSensorPoint(j)); if(d<minI) minI=d; } } if(minI>maxD) maxD=minI;
	 * } System.out.println("Maximum of minimum distance is:"+maxD); return
	 * maxD; }
	 * 
	 * 
	 * public static double percentileofMinDist(SnapshotData data, DistanceI
	 * distance, double percentile) { double maxD=-1.0;
	 * 
	 * double dist[]=new double [data.size()]; //mantengo in dist (1-percentile)
	 * minime distanze più grandi
	 * 
	 * 
	 * for (int i=0;i<data.size();i++){ double minI=Double.MAX_VALUE; for(int
	 * j=0;j<data.size();j++) { String
	 * key=data.getSensorPoint(i).getId()+":"+data.getSensorPoint(j).getId();
	 * if(data.getSensorPoint(i).getId()!=data.getSensorPoint(j).getId()) {
	 * double d=distance.compute(data.getSensorPoint(i),
	 * data.getSensorPoint(j)); if(d<minI) minI=d; } } /*if(minI>maxD)
	 * maxD=minI;
	 * 
	 * dist[i]=minI; }
	 * 
	 * Arrays.sort(dist);
	 * 
	 * int size= (int)(percentile* dist.length);
	 * System.out.println("Maximum of minimum distance at "+percentile+
	 * " percentile:"+dist[size]); return dist[size]; }
	 */
	abstract public void updateSnapshotWeigth(SnapshotData data,
			DistanceI distance);

	abstract public Double getWeight(Integer id1, Integer id2);

	abstract public Double getDistance(Integer id1, Integer id2);

	public void salva(String nomeFile) throws FileNotFoundException,
			IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				nomeFile));
		out.writeObject(this);
		out.close();

	}

	public static SnapshotWeigth carica(String nomeFile)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				nomeFile));
		SnapshotWeigth t = (SnapshotWeigth) in.readObject();
		in.close();
		return t;

	}

	/*
	 * public long getTimeInMillis(){ return
	 * (end.getTimeInMillis()-start.getTimeInMillis()); }
	 */

}
