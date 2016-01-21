package tree;




import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;

import data.feature.AutocorrelationI;


public class LeafNode extends Node {

	
	public LeafNode(AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex,
			int endExampleIndex, int minExamples, int depth,Node father) {
		super(a,trainingSet, schema, W, beginExampleIndex, endExampleIndex,
				minExamples,depth,father);
	
	}
	
	/*public LeafNode(AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex,
			int endExampleIndex, int minExamples, int depth,Node father, boolean temp) {
		super(a,trainingSet, schema, W, beginExampleIndex, endExampleIndex,
				minExamples,depth,father, temp);
	
	}*/
	public LeafNode( SnapshotData trainingSet,
			SnapshotSchema schema,  int beginExampleIndex,
			int endExampleIndex, int minExamples, int depth,Node father) {
		super( trainingSet, schema, beginExampleIndex, endExampleIndex,
				minExamples,depth,father);
	}
	
	public LeafNode(SnapshotSchema schema, int begin, int end, int depth, Node father, FeatureAveragesNode fAvgNode){
		super(schema,begin,end,depth,father);
		//super(AutocorrelationI a, PrototypeI p,SnapshotData trainingSet, SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex, int endExampleIndex, int minExamples, int depth, Node father){
		super.setFeatureAvgNode(fAvgNode);
	}
	



}
