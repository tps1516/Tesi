package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import windowStructure.FeatureWindow;
import windowStructure.TemporalWindow;
import data.Network;
import data.feature.AutocorrelationI;
import data.feature.Feature;
import data.feature.NumericFeature;
import data.feature.ResubstitutionIndex;
import data.feature.SpatialFeature;

public abstract class Node implements Serializable {

	static Integer idNodeCount = new Integer(0);
	protected Integer idNode = idNodeCount++;
	protected Integer beginExampleIndex;
	protected Integer endExampleIndex;
	protected SnapshotSchema schema; // which includes the model
	protected FeatureWindow featureAvgNode = null;

	private Node father;
	private int depth = 0;

	public Node(SnapshotSchema schema, int beginExampleIndex,
			int endExampleIndex, int depth, Node father) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;
		this.setSchema((SnapshotSchema) schema.clone());
		this.father = father;
		this.depth = depth;

	}

	public Node(SnapshotData trainingSet, SnapshotSchema schema,
			int beginExampleIndex, int endExampleIndex, int minExamples,
			int depth, Node father) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;
		this.setSchema((SnapshotSchema) schema.clone());
		this.father = father;
		this.depth = depth;
		this.getSchema().reset(); // Reset the spatial attributes and all the
									// target attributes which are now available
									// for the tree construction

		// Create the schema at the current node

		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			for (SpatialFeature sf : this.getSchema().getSpatialList()) {
				int index = sf.getIndexMining();
				sf.setMax((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue()); // update max
				sf.setMin((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue()); // update min
				sf.setMean((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue());
				; // update mean

			}

			for (Feature f : this.getSchema().getTargetList()) {

				if (!f.getStopTree()) {
					int index = f.getIndexMining();
					if (!(trainingSet.getSensorPoint(i).getMeasure(index)
							.isNull())) {
						if (f instanceof NumericFeature) {
							((NumericFeature) f).setMin((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update max
							((NumericFeature) f).setMax((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update min
							((NumericFeature) f).setMean((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update mean
						}
					}
				}
			}
		}

		// Compute Autocorrelation Measure over current Target Attributes and
		// Identify the Target Leaf

		for (Feature f : this.getSchema().getTargetListNotLeaf()) {
			f.computeAutocorrelation(new ResubstitutionIndex(), trainingSet,
					null, beginExampleIndex, endExampleIndex);
		}

	}

	// temporaneo

	/*
	 * public Node(AutocorrelationI a, SnapshotData trainingSet, SnapshotSchema
	 * schema, SnapshotWeigth W, int beginExampleIndex, int endExampleIndex, int
	 * minExamples, int depth, Node father, boolean temp){
	 * this.beginExampleIndex=beginExampleIndex;
	 * this.endExampleIndex=endExampleIndex;
	 * this.setSchema((SnapshotSchema)schema.clone()); this.father=father;
	 * this.depth=depth;
	 * 
	 * 
	 * // Compute Autocorrelation Measure over current Target Attributes and
	 * Identify the Target Leaf
	 * 
	 * for(Feature f:this.getSchema().getTargetListNotLeaf()){ //
	 * System.out.println("Evaluating ..."+ f.getName()); if(a!=null){
	 * f.computeAutocorrelation(a, trainingSet, W, beginExampleIndex,
	 * endExampleIndex);
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * }
	 */

	public Node(AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex,
			int endExampleIndex, int minExamples, int depth, Node father) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;
		this.setSchema((SnapshotSchema) schema.clone());
		this.father = father;
		this.depth = depth;
		this.getSchema().reset(); // Reset the spatial attributes and all the
									// target attributes which are now available
									// for the tree construction

		// Create the schema at the current node

		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			for (SpatialFeature sf : this.getSchema().getSpatialList()) {
				int index = sf.getIndexMining();
				sf.setMax((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue()); // update max
				sf.setMin((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue()); // update min
				sf.setMean((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue());
				; // update mean

			}

			for (Feature f : this.getSchema().getTargetList()) {

				if (!f.getStopTree()) {
					int index = f.getIndexMining();
					if (!(trainingSet.getSensorPoint(i).getMeasure(index)
							.isNull())) {
						if (f instanceof NumericFeature) {
							((NumericFeature) f).setMin((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update max
							((NumericFeature) f).setMax((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update min
							((NumericFeature) f).setMean((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update mean
						}
					}
				}
			}
		}

		// Compute Autocorrelation Measure over current Target Attributes and
		// Identify the Target Leaf

		for (Feature f : this.getSchema().getTargetListNotLeaf()) {
			// System.out.println("Evaluating ..."+ f.getName());
			if (a != null) {
				f.computeAutocorrelation(a, trainingSet, W, beginExampleIndex,
						endExampleIndex);

			}

		}

	}

	// Annalisa Added for optimization

	public Node(Node n, AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W, int beginExampleIndex,
			int endExampleIndex, int minExamples, int depth, Node father) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;
		this.setSchema((SnapshotSchema) schema.clone());
		this.father = father;
		this.depth = depth;
		this.getSchema().reset(); // Reset the spatial attributes and all the
									// target attributes which are now available
									// for the tree construction

		// Create the schema at the current node

		int i = 0;
		for (SpatialFeature sf : this.getSchema().getSpatialList()) {
			sf.setMax(n.getSchema().getSpatialList().get(i).getMax());
			sf.setMin(n.getSchema().getSpatialList().get(i).getMin());
			sf.setMean(n.getSchema().getSpatialList().get(i).getMean());
			i++;
		}

		i = 0;
		for (Feature f : this.getSchema().getTargetList()) {
			if (!f.getStopTree()) {

				if (f instanceof NumericFeature) {
					((NumericFeature) f).setMax(((NumericFeature) (n
							.getSchema().getTargetList().get(i))).getMax());
					((NumericFeature) f).setMin(((NumericFeature) (n
							.getSchema().getTargetList().get(i))).getMin());
					((NumericFeature) f).setMean(((NumericFeature) (n
							.getSchema().getTargetList().get(i))).getMean());
				} else
					throw new RuntimeException();
			}
			i++;
		}

		// Compute Autocorrelation Measure over current Target Attributes and
		// Identify the Target Leaf
		i = 0;
		for (Feature f : this.getSchema().getTargetListNotLeaf()) {
			// System.out.println("Evaluating ..."+ f.getName());
			if (a != null)
				f.setAutocorrelation(n.getSchema().getTargetListNotLeaf()
						.get(i).getAutocorrelation());
			i++;

		}

	}

	int getDepth() {
		return depth;
	}

	private List<Feature> isNotLeaf() {
		List<Feature> leafFeature = new ArrayList<Feature>();
		for (Feature f : getSchema().getTargetList()) {
			if (!f.getStopTree())
				leafFeature.add(f);

		}
		return leafFeature;
	}

	Boolean isLeaf() {
		return (isNotLeaf().isEmpty());
	}

	boolean containLeaf() {

		for (Feature f : getSchema().getTargetList())
			if (f.getStopTree())
				return true;
		return false;

	}

	public int getIdNode() {
		return idNode;
	}

	int getBeginExampleIndex() {
		return beginExampleIndex;
	}

	int getEndExampleIndex() {
		return endExampleIndex;
	}

	double getHeuristic(Feature f) {
		int index = f.getIndexMining();
		return getSchema().getTargetList()
				.get(index - getSchema().getSpatialList().size())
				.getAutocorrelation();
	}

	public String toString() {
		String str = "";
		/*
		 * for(int i=0;i<getDepth()-1;i++) str+="-";
		 * str+=(beginExampleIndex+":"+endExampleIndex+"["); for(Feature
		 * f:getSchema().getTargetList()){
		 * //str+=f.getName()+"=(asp)"+f.getPrototype
		 * (a)+"[Stream:"+f.getStreamPrototype
		 * (a)+"](spat)"+f.getPrototype(s)+"[Stream:"
		 * +f.getStreamPrototype(s)+"]"; str+=f.getName()+"="+f.getPrototype();
		 * if(f.getStopTree()) str+="(L)"; } str+="]"+centroid+"\n";
		 */

		if (this instanceof SplittingNode) {
			str += "ID: " + this.getIdNode() + ": SPLITTING NODE. ";
			if (this.father != null)
				str += "Figlio di: " + father.getIdNode();
			str += " --- [" + this.getBeginExampleIndex() + ":"
					+ this.getEndExampleIndex() + "] --- SPLIT SU: "
					+ ((SplittingNode) this).getSplitFeature().getName() + "<="
					+ ((SplittingNode) this).getSplitThereshld();

			str += "\n" + "Averages:";
			str += "\n" + featureAvgNode.toString();
		} else {
			str += "ID: " + this.getIdNode() + ": LEAF NODE. ";
			if (this.father != null)
				str += "Figlio di: " + father.getIdNode();
			str += " --- [" + this.getBeginExampleIndex() + ":"
					+ this.getEndExampleIndex() + "]";
			str += "\n" + "Averages:";
			str += "\n" + featureAvgNode.toString();

			LeafNode leaf = (LeafNode) this;
			if (leaf.getVARModel() != null) {
				str += "\n" + "MODELLO VAR: ";
				str += "\n" + leaf.getVARModel().toString();
			} else {
				str += "\n" + "modello VAR non ancora avvalorato";
			}
		}

		return str;
	}

	public void setSchema(SnapshotSchema schema) {
		this.schema = schema;
	}

	public SnapshotSchema getSchema() {
		return schema;
	}

	public Node getFather() {
		return father;
	}

	List<Object> getModels() {
		List<Object> output = new LinkedList<Object>();
		for (Feature f : schema.getTargetList()) {
			output.add(f.getPrototype());
		}
		return output;
	}

	Map<Integer, ErrorStatistic> estimateGetisAndOrdError(SnapshotData snap,
			int begin, int end) {

		Map<Integer, ErrorStatistic> error = new HashMap<Integer, ErrorStatistic>();

		for (Feature f : getSchema().getTargetList()) {

			error.put(f.getIndexMining(),
					new ErrorStatistic(f.getAutocorrelation(), end - begin + 1));
		}

		return error;
	}

	Map<Integer, ErrorStatistic> estimateError(SnapshotData snap, int begin,
			int end) {

		Map<Integer, ErrorStatistic> error = new HashMap<Integer, ErrorStatistic>();

		for (Feature f : getSchema().getTargetList()) {

			error.put(f.getIndexMining(), new ErrorStatistic(0.0, 0));
		}
		for (int i = begin; i <= end; i++) {
			List<Object> predictedValueList = getModels();
			int indexFeature = 0;

			for (Feature f : getSchema().getTargetList()) {
				if (!snap.getSensorPoint(i).getMeasure(f.getIndexMining())
						.isNull()) {
					error.get(f.getIndexMining()).countTuples++;
					Object predictedValue = predictedValueList
							.get(indexFeature);
					Object realValue = snap.getSensorPoint(i)
							.getMeasure(f.getIndexMining()).getValue();
					if (f instanceof NumericFeature) {
						error.get(f.getIndexMining()).error += Math
								.pow((Double) realValue
										- (Double) predictedValue, 2);
					} else {
						if (!predictedValue.equals(realValue))
							error.get(f.getIndexMining()).error += 1;
					}

				}
				indexFeature++;
			}
		}

		return error;
	}

	// Update the node on the basis of the new snapshot
	void update(SnapshotData trainingSet, AutocorrelationI a, SnapshotWeigth W,
			int beginExampleIndex, int endExampleIndex) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;

		this.schema.reset(); // resed all attributes excepted for leaf
								// attributes
		// Update the schema at the current node
		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			for (SpatialFeature sf : this.getSchema().getSpatialList()) {
				int index = sf.getIndexMining();
				sf.setMax((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue()); // update max
				sf.setMin((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue()); // update min
				sf.setMean((Double) trainingSet.getSensorPoint(i)
						.getMeasure(index).getValue());
				; // update mean

			}

		}
		for (Feature f : this.getSchema().getTargetList()) {
			if (!f.getStopTree()) {
				for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
					int index = f.getIndexMining();
					if (!(trainingSet.getSensorPoint(i).getMeasure(index)
							.isNull())) {
						if (f instanceof NumericFeature) {
							((NumericFeature) f).setMin((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update max
							((NumericFeature) f).setMax((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update min
							((NumericFeature) f).setMean((Double) trainingSet
									.getSensorPoint(i).getMeasure(index)
									.getValue()); // update mean
						}
					}
				}
			} else // in caso di foglia vado a prendere il modello dal padre
					// (father)
			{
				if (father != null
						&& father
								.getSchema()
								.getTargetList()
								.get(f.getIndexMining()
										- father.getSchema().getSpatialList()
												.size()).getStopTree())
				// era foglia gi� al passo precedente
				{
					getSchema().getTargetList().set(
							f.getIndexMining()
									- getSchema().getSpatialList().size(),
							(Feature) father
									.getSchema()
									.getTargetList()
									.get(f.getIndexMining()
											- father.getSchema()
													.getSpatialList().size())
									.clone());
				}
				// � diventato foglia a questo passo
				else {
					f.clear();
					for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
						int index = f.getIndexMining();
						if (!(trainingSet.getSensorPoint(i).getMeasure(index)
								.isNull())) {
							if (f instanceof NumericFeature) {
								((NumericFeature) f)
										.setMin((Double) trainingSet
												.getSensorPoint(i)
												.getMeasure(index).getValue()); // update
																				// max
								((NumericFeature) f)
										.setMax((Double) trainingSet
												.getSensorPoint(i)
												.getMeasure(index).getValue()); // update
																				// min
								((NumericFeature) f)
										.setMean((Double) trainingSet
												.getSensorPoint(i)
												.getMeasure(index).getValue()); // update
																				// mean
							}
						}
					}
				}

			}
		}

		// Compute Autocorrelation Measure over current Target Attributes and
		// Identify the Target Leaf
		int index = 0;
		for (Feature f : this.getSchema().getTargetList()) {

			if (!f.getStopTree())
				f.computeAutocorrelation(a, trainingSet, W, beginExampleIndex,
						endExampleIndex);
			else if (father == null
					|| !father.getSchema().getTargetList().get(index)
							.getStopTree()) // non era gi� foglia
				f.computeAutocorrelation(a, trainingSet, W, beginExampleIndex,
						endExampleIndex);
			index++;
		}

	}

	/*
	 * void sampling(SnapshotData data,String centroidType, float centroidPerc){
	 * 
	 * 
	 * this.mbr=data.computeMBR(schema,beginExampleIndex,endExampleIndex);
	 * if(centroidType.equals("random"))
	 * centroid=data.sampleCentroid(schema,beginExampleIndex,endExampleIndex,
	 * centroidPerc); else centroid=data.quadSampleCentroid(schema,
	 * beginExampleIndex, endExampleIndex, mbr,centroidPerc); }
	 */

	void initializedFeatureAvgNode(int dim) {
		featureAvgNode = new FeatureWindow(this, dim);
	}

	void updateFeatureAvgNode() {
		featureAvgNode.updateAverages(this);
	}

	/*
	 * public ForecastingModel getModel() { return this.VARModel; }
	 */

	public FeatureWindow getFeatureAvgNode() {
		return featureAvgNode;
	}

	/*
	
*/
	void setFeatureAvgNode(FeatureWindow fAvgNode) {
		try {
			featureAvgNode = fAvgNode.Clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Clonazione non supportata, riga 500 Node");
			e.printStackTrace();
		}
	}

	void insLastOfFather() {
		featureAvgNode.insLastOfFather(father.getFeatureAvgNode());
	}

	FeatureWindow getRealFeatureWindow(String s, Network network,
			SnapshotData snap, FeatureWindow FatherFW) {

		if (s.equalsIgnoreCase("median"))
			return getFeatureMedianNode(network, snap, FatherFW);
		else
			return getRealFeatureAvgNode(network, snap, FatherFW);

	}

	private FeatureWindow getFeatureMedianNode(Network network,
			SnapshotData snap, FeatureWindow FatherFW) {
		int size = FatherFW.getTWMaxSize();
		FeatureWindow res = new FeatureWindow(this.schema, size);
		LinkedList<HashMap<Integer, LinkedList<Double>>> series = new LinkedList<HashMap<Integer, LinkedList<Double>>>();
		HashMap<Integer, LinkedList<Double>> valueOfFeature = new HashMap<Integer, LinkedList<Double>>();

		for (int nsize = 0; nsize < network.size(); nsize++) {
			valueOfFeature = new HashMap<Integer, LinkedList<Double>>();
			for (Feature f : this.schema.getTargetList()) {
				valueOfFeature.put(f.getFeatureIndex(),
						new LinkedList<Double>());
			}
			series.add(valueOfFeature);
		}

		for (int i = this.beginExampleIndex; i <= this.endExampleIndex; i++) {
			FeatureWindow fw = network.getFeatureWindow(snap.getSensorPoint(i)
					.getId());

			for (Feature f : schema.getTargetList()) {

				int j = 0;
				TemporalWindow<Double> tw = fw.getTemporalWindow(f);

				for (Double d : tw) {
					if (d == Double.MAX_VALUE) {
						j++;
						continue;
					}
					HashMap<Integer, LinkedList<Double>> support = series
							.get(j);
					support.put(f.getFeatureIndex(),
							support.get(f.getFeatureIndex())).add(d);
					series.set(j, support);
					j++;
				}
			}
		}

		for (int nsize = 0; nsize < network.size(); nsize++) {
			for (Feature f : schema.getTargetList()) {
				LinkedList<Double> serie = series.get(nsize).get(
						f.getFeatureIndex());
				serie.sort(null);
				int listsize = serie.size();
				if (listsize != 0) {
					if (listsize % 2 != 0) {
						double d = serie.get((listsize - 1) / 2);
						res.getTemporalWindow(f).setValue(d);
					} else {
						double d1 = serie.get(listsize / 2);
						double d2 = serie.get((listsize / 2) - 1);
						res.getTemporalWindow(f).setValue((d1 + d2) / 2);
					}
				} else
					res.getTemporalWindow(f).setValue(Double.MAX_VALUE);
			}
		}
		return res;
	}

	private FeatureWindow getRealFeatureAvgNode(Network network,
			SnapshotData snap, FeatureWindow FatherFW) {
		int size = FatherFW.getTWMaxSize();
		FeatureWindow res = new FeatureWindow(this.schema, size);
		HashMap<Integer, LinkedList<Double>> averages = new HashMap<Integer, LinkedList<Double>>();
		HashMap<Integer, ArrayList<Integer>> counterNOfInstancesForFeature = new HashMap<Integer, ArrayList<Integer>>();

		for (int i = 0; i < schema.getTargetList().size(); i++) {
			LinkedList<Double> inAverages = new LinkedList<Double>();
			ArrayList<Integer> counterNOfInstances = new ArrayList<Integer>();
			for (int k = 0; k < network.size(); k++) {
				inAverages.add(0.0);
				counterNOfInstances.add(0);
			}
			averages.put(i, inAverages);
			counterNOfInstancesForFeature.put(i, counterNOfInstances);
		}

		for (int i = this.beginExampleIndex; i <= this.endExampleIndex; i++) {
			FeatureWindow fw = network.getFeatureWindow(snap.getSensorPoint(i)
					.getId());
			for (Feature f : schema.getTargetList()) {
				TemporalWindow<Double> tw = fw.getTemporalWindow(f);
				int j = 0;
				for (Double d : tw) {
					if (d == Double.MAX_VALUE) {
						j = j + 1;
						continue;
					}
					Double value = averages.get(f.getFeatureIndex()).get(j);
					Integer count = counterNOfInstancesForFeature.get(
							f.getFeatureIndex()).get(j);
					counterNOfInstancesForFeature.get(f.getFeatureIndex()).set(
							j, count + 1);
					averages.get(f.getFeatureIndex()).set(j, value + d);
					j++;
				}
			}
		}

		for (Feature f : schema.getTargetList()) {
			ArrayList<Integer> ListNOfInstance = counterNOfInstancesForFeature
					.get(f.getFeatureIndex());
			int j = 0;
			int nOfInstances = 0;
			for (Double d : averages.get(f.getFeatureIndex())) {
				nOfInstances = ListNOfInstance.get(j);
				if (nOfInstances == 0)
					res.getTemporalWindow(f).setValue(Double.MAX_VALUE);
				else
					res.getTemporalWindow(f).setValue(d / nOfInstances);
				j = j + 1;
			}
		}

		return res;
	}
}
