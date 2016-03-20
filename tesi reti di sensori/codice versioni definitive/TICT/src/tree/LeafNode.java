package tree;

import java.util.ArrayList;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import windowStructure.FeatureWindow;
import data.feature.AutocorrelationI;
import forecast.ForecastingModel;

public class LeafNode extends Node {

	private ForecastingModel VARModel;

	public LeafNode(AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex,
			int endExampleIndex, int minExamples, int depth, Node father) {
		super(a, trainingSet, schema, W, beginExampleIndex, endExampleIndex,
				minExamples, depth, father);
		this.VARModel = null;
	}

	void learnVARModels(ArrayList<Object> rParameters) {
		double[][] dataset = super.featureAvgNode.exportInMatrixForm();
		this.VARModel = new ForecastingModel(dataset, this.getSchema(),
				rParameters);
	}

	/*
	 * public LeafNode(AutocorrelationI a, SnapshotData trainingSet,
	 * SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex, int
	 * endExampleIndex, int minExamples, int depth,Node father, boolean temp) {
	 * super(a,trainingSet, schema, W, beginExampleIndex, endExampleIndex,
	 * minExamples,depth,father, temp);
	 * 
	 * }
	 */
	public LeafNode(SnapshotData trainingSet, SnapshotSchema schema,
			int beginExampleIndex, int endExampleIndex, int minExamples,
			int depth, Node father) {
		super(trainingSet, schema, beginExampleIndex, endExampleIndex,
				minExamples, depth, father);
		this.VARModel = null;
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
