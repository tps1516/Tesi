package data.feature;

import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public interface AutocorrelationI {

	boolean isMax(double v);

	Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,
			int beginIndex, int endIndex);

	// Added with the optimization of Var , GO and MoranAndVar
	Double get();

}
