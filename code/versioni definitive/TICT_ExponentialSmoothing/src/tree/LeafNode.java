package tree;

import java.util.ArrayList;

import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import windowStructure.FeatureWindow;
import data.feature.AutocorrelationI;
import forecast.ForecastingModel;
import forecast.NotForecastingException;

public class LeafNode extends Node {

	private ForecastingModel VARModel;

	public LeafNode(AutocorrelationI a, SnapshotData trainingSet,
			SnapshotData snapSmoot, SnapshotSchema schema, SnapshotWeigth W,
			int beginExampleIndex, int endExampleIndex, int minExamples,
			int depth, Node father) {
		super(a, trainingSet, snapSmoot, schema, W, beginExampleIndex,
				endExampleIndex, minExamples, depth, father);
		this.VARModel = null;
	}

	void learnVARModels(ArrayList<Object> rParameters) {
		try {
			double[][] dataset = super.featureAvgNode.exportInMatrixForm();
			this.VARModel = new ForecastingModel(dataset, this.getSchema(),
					rParameters);
		} catch (NotForecastingException e) {
			VARModel = null;
		}
	}

	public LeafNode(SnapshotSchema schema, int begin, int end, int depth,
			Node father, FeatureWindow fAvgNode) {
		super(schema, begin, end, depth, father);
		// super(AutocorrelationI a, PrototypeI p,SnapshotData trainingSet,
		// SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex, int
		// endExampleIndex, int minExamples, int depth, Node father){
		super.setFeatureAvgNode(fAvgNode);
		this.VARModel = null;
	}

	ForecastingModel getVARModel() {
		return this.VARModel;
	}

}
