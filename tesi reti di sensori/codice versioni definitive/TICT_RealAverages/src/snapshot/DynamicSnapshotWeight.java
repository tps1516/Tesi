package snapshot;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import data.DistanceI;

public class DynamicSnapshotWeight extends SnapshotWeigth {
	private Map<Integer, Map<Integer, Float>> D = new HashMap<Integer, Map<Integer, Float>>(); // triangular
																								// Superior
																								// matrix
																								// <idI-idJ,
																								// w>

	public DynamicSnapshotWeight(double b) {
		super(b);

	}

	public void updateSnapshotWeigth(SnapshotData data, DistanceI distance) {

		for (int i = 0; i < data.size(); i++) {

			// System.out.println(i);
			for (int j = i; j < data.size(); j++) {
				// System.out.print(j+",");
				// String
				// key=data.getSensorPoint(i).getId()+":"+data.getSensorPoint(j).getId();
				boolean keyI = D.containsKey(data.getSensorPoint(i).getId());
				boolean keyJ = D.containsKey(data.getSensorPoint(j).getId());
				if (data.getSensorPoint(i).getId() != data.getSensorPoint(j)
						.getId())
					if (!keyI || !keyJ) { // a new sensor in the current
											// snapshot with respect to the past

						double d = distance.compute(data.getSensorPoint(i),
								data.getSensorPoint(j));
						if (d <= b) {

							if (keyI) {
								D.get(data.getSensorPoint(i).getId()).put(
										data.getSensorPoint(j).getId(),
										(float) d);
							}

							else {
								Map<Integer, Float> m = new HashMap<Integer, Float>();
								m.put(data.getSensorPoint(j).getId(), (float) d);
								D.put(data.getSensorPoint(i).getId(), m);
							}

						}

					}
			}
		}
		end = new GregorianCalendar();

	}

	public Double getWeight(Integer id1, Integer id2) {

		Double w = null;
		if (id1.intValue() == id2.intValue())
			return 0.0;
		else {
			try {
				if (id1 < id2)

					w = (double) D.get(id1).get(id2);
				else
					w = (double) D.get(id2).get(id1);
			} catch (NullPointerException e) {
				return 0.0;
			}
			if (w == null)
				w = b; // approximate the distance with the boundary
			if (w.equals(0.0))
				return 1.0;
			else
				return 1.0 / Math.pow(w, 3);

		}

	}

	public Double getDistance(Integer id1, Integer id2) {
		Double d;
		if (id1.intValue() == id2.intValue())
			return 0.0;
		else {

			if (id1 < id2)
				d = (double) D.get(id1).get(id2);
			else
				d = (double) D.get(id2).get(id1);

			if (d == null)
				return b;
			else
				return d;
		}

	}

	public String toString() {
		String str = "Weight Matrix\n";
		for (Integer i : D.keySet()) {
			str += (i + ":" + D.get(i).size() + ",");
			for (Integer k : D.get(i).keySet())
				str += (getWeight(i, k) + ",");

			str += "\n";
		}

		return str;
	}

}
