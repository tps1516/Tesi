package tree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import mbrModel.KNNModel;

import data.DistanceI;
import data.EuclideanDistance;
import data.SensorPoint;
import data.datavalue.Value;
import data.feature.AutocorrelationI;
import data.feature.CategoricalFeature;
import data.feature.Feature;

import data.feature.NumericFeature;

import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;

public class Tree implements Serializable, Comparable<Tree> {

	private static final long serialVersionUID = 1L;
	private Node root = null;
	private Node father = null;
	private Tree leftSubTree = null;
	private Tree rightsubTree = null;
	long computationTime = 0;

	Tree() {

	}

	/*
	 * public Tree(SnapshotData data, SnapshotSchema schema,int numberOfSplits,
	 * float centroidPerc,String centroidType, String testType){
	 * learnTre(data,schema
	 * ,0,data.size()-1,(int)(Math.sqrt(data.size()))*2,1,numberOfSplits
	 * ,null,centroidPerc,centroidType,testType);
	 * 
	 * 
	 * }
	 */

	// STEP SUCCESSIVI
	public Tree(SnapshotData data, SnapshotSchema schema,
			SnapshotWeigth weight, AutocorrelationI autocorrelation,
			int numberOfSplits, float ccentroidPerc, String centroidType,
			String testType) {
		learnTree(data, schema, weight, autocorrelation, 0, data.size() - 1,
				(int) (Math.sqrt(data.size())) * 2, 1, numberOfSplits, null,
				testType);

	}

	// FIRST STEP
	public Tree(SnapshotData data, SnapshotSchema schema,
			SnapshotWeigth weight, AutocorrelationI autocorrelation,
			int numberOfSplits, String testType) {
		learnTree(data, schema, weight, autocorrelation, 0, data.size() - 1,
				(int) (Math.sqrt(data.size())) * 2, 1, numberOfSplits, null,
				testType, this.root.getavg());

	}

	private List<Object> predict(SensorPoint sp) {

		LeafNode node = predictingNode(sp);
		return node.getModels();

	}

	private LeafNode predictingNode(SensorPoint pt) {

		if (root instanceof LeafNode)
			return (LeafNode) root;
		else if (((SplittingNode) root).branch(pt).equals("left"))
			return leftSubTree.predictingNode(pt);
		else
			return rightsubTree.predictingNode(pt);

	}

	private Boolean isLeaf(AutocorrelationI a, SnapshotSchema schema,
			int minExamples) {

		boolean flag = true;

		for (Feature f : schema.getTargetList()) {
			if (!f.getStopTree())
				if (f.getCountTuples() <= minExamples) {
					f.setStopTree(true);

				} else if (f instanceof NumericFeature)
					if (a.isMax(((NumericFeature) f).getAutocorrelation())) {
						f.setStopTree(true);

					} else

						flag = false;
				else {
					// check if all label in the same class
					if (((CategoricalFeature) f).getNumberOfClasses() == 1) {
						f.setStopTree(true);

					} else
						flag = false;
				}
		}

		return flag; // a final leaf is added to tree if all target attributes
						// satisfy the stop condition

	}

	private void setLeaf(SnapshotSchema schema) {
		for (Feature f : schema.getTargetList())
			if (!f.getStopTree()) {
				f.setStopTree(true);

			}

	}

	public String TestTree(SnapshotData data, SnapshotSchema schema,
			String outfileName) throws IOException {

		List<Double> MSE = new LinkedList<Double>();
		List<Double> MAE = new LinkedList<Double>();
		String str = "";
		for (Feature f : root.getSchema().getTargetList()) {
			if (!str.isEmpty())
				str += ";";
			str += f.getName() + "Pred," + f.getName();
			MSE.add(new Double(0.0));
			MAE.add(new Double(0.0));
		}

		for (int i = 0; i < data.size(); i++) {
			List out = predict(data.getSensorPoint(i));
			int index = 0;
			str = "";
			for (Feature f : root.getSchema().getTargetList()) {
				f.getIndexMining();
				Object o = out.get(index);
				if (!str.isEmpty())
					str += ";";
				str += (o + ";" + data.getSensorPoint(i)
						.getMeasure(f.getIndexMining()).getValue());
				int indexFeature = f.getIndexMining()
						- data.getSpatialFeaturesSize();

				Value v = data.getSensorPoint(i).getMeasure(f.getIndexMining());
				if (!v.isNull()) {
					if (o instanceof Double) {
						MSE.set(indexFeature,
								MSE.get(indexFeature)
										+ Math.pow(
												(Double) o
														- (Double) (data
																.getSensorPoint(
																		i)
																.getMeasure(
																		f.getIndexMining())
																.getValue()), 2));
						MAE.set(indexFeature,
								MAE.get(indexFeature)
										+ Math.abs((Double) o
												- (Double) (data
														.getSensorPoint(i)
														.getMeasure(
																f.getIndexMining())
														.getValue())));
					} else if (!o.equals((data.getSensorPoint(i).getMeasure(
							f.getIndexMining() - data.getSpatialFeaturesSize())
							.getValue())))
						MSE.set(f.getIndexMining()
								- data.getSpatialFeaturesSize(),
								MSE.get(f.getIndexMining()
										- data.getSpatialFeaturesSize()) + 1);
				}
				index++;
			}
			str = str.replace(".", ",");

		}
		String mse = "";
		String mae = "";
		int index = 0;
		// for(Feature f: root.getSchema().getTargetList()){
		for (Feature f : schema.getTargetList()) {
			mse += Math.sqrt(MSE.get(index) / f.getCountTuples()) + ";";
			if (f instanceof NumericFeature)
				mae += MAE.get(index) / f.getCountTuples() + ";";
			index++;
		}

		return (mse + mae);

	}

	private Boolean isLeaf(SnapshotSchema schema, int minExamples) {

		boolean flag = true;

		for (Feature f : schema.getTargetList()) {
			if (!f.getStopTree())
				if (f.getCountTuples() <= minExamples) {
					f.setStopTree(true);

				} else if (f instanceof NumericFeature)
					if (((NumericFeature) f).getAutocorrelation() == 0.0) {
						f.setStopTree(true);

					} else
						flag = false;
				else {
					// check if all label in the same class
					if (((CategoricalFeature) f).getNumberOfClasses() == 1) {
						f.setStopTree(true);

					} else
						flag = false;
				}
		}
		return flag; // a final leaf is added to tree if all target attributes
						// satisfy the stop condition

	}

	// STEP SUCCESSIVI
	private void learnTree(SnapshotData data, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI autocorrelation, int beginIndex,
			int endIndex, int minimumExamples, int depth, int numberOfSplits,
			Node father, String testType, int value) {

		this.father = father;
		Node testRoot = new LeafNode(autocorrelation, data, schema, W,
				beginIndex, endIndex, minimumExamples, depth, father);
		if (isLeaf(autocorrelation, testRoot.getSchema(), minimumExamples)) {
			if (root == null)
				root = testRoot;
			else {
				// root così come era diventa foglia
				setLeaf(root.getSchema());
			}
		} else // split node
		{
			try {
				root = new SplittingNode(testRoot, autocorrelation, data,
						testRoot.getSchema(), W, beginIndex, endIndex,
						minimumExamples, depth, numberOfSplits, father,
						testType);
			} catch (SplitException e) {

				setLeaf(testRoot.getSchema());
				root = testRoot;
				return;

			}

			leftSubTree = new Tree();
			rightsubTree = new Tree();
			leftSubTree.learnTree(data, root.getSchema(), W, autocorrelation,
					((SplittingNode) root).splitLeft.beginIndex,
					((SplittingNode) root).splitLeft.endIndex, minimumExamples,
					depth + 1, numberOfSplits, root, testType, 0);
			rightsubTree.learnTree(data, root.getSchema(), W, autocorrelation,
					((SplittingNode) root).splitRight.beginIndex,
					((SplittingNode) root).splitRight.endIndex,
					minimumExamples, depth + 1, numberOfSplits, root, testType,
					0);

		}
	}

	// FIRST STEP
	private void learnTree(SnapshotData data, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI autocorrelation, int beginIndex,
			int endIndex, int minimumExamples, int depth, int numberOfSplits,
			Node father, String testType, FeatureMeanNode avg) {

		this.father = father;
		Node testRoot = new LeafNode(autocorrelation, data, schema, W,
				beginIndex, endIndex, minimumExamples, depth, father);
		if (isLeaf(autocorrelation, testRoot.getSchema(), minimumExamples)) {
			if (root == null) {
				// sei in una chiamata ricorsiva di questo metodo,
				// cioè stai aggiungendo un livello. E questo nodo sarà foglia.
				root.set(father.getavg());
				/*
				 * set(avg){ this.avg=avg.clone();
				 */
				root.updateavg();
				root = testRoot;
				root.FeatureMN = new FeatureMeanNode();
			} else {
				// non stai aggiungendo un altro livello
				// questo nodo rimarrà foglia.
				// tutte le sue feature diventano foglia
				root.updateavg();
				setLeaf(root.getSchema());
				root.FeatureMN = new FeatureMeanNode();
			}
		} else // split node
		{
			try {

				root = new SplittingNode(testRoot, autocorrelation, data,
						testRoot.getSchema(), W, beginIndex, endIndex,
						minimumExamples, depth, numberOfSplits, father,
						testType);

			} catch (SplitException e) {
				
				setLeaf(testRoot.getSchema());
				root = testRoot;
				root.set();
				updateavg();
				root.FeatureMN = new FeatureMeanNode();
				return;

			}

			leftSubTree = new Tree();
			rightsubTree = new Tree();
			leftSubTree.learnTree(data, root.getSchema(), W, autocorrelation,
					((SplittingNode) root).splitLeft.beginIndex,
					((SplittingNode) root).splitLeft.endIndex, minimumExamples,
					depth + 1, numberOfSplits, root, testType, root.getavg());
			rightsubTree.learnTree(data, root.getSchema(), W, autocorrelation,
					((SplittingNode) root).splitRight.beginIndex,
					((SplittingNode) root).splitRight.endIndex,
					minimumExamples, depth + 1, numberOfSplits, root, testType,
					root.getavg());
			updateavg()

		}
	}

	/**
	 * Salva l'albero
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 */
	public void salva(String nomeFile) throws FileNotFoundException,
			IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				nomeFile));
		out.writeObject(this);
		out.close();

	}

	/**
	 * Carica l'albero
	 * 
	 */
	public static Tree carica(String nomeFile) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				nomeFile));
		Tree t = (Tree) in.readObject();
		in.close();
		return t;

	}

	public String toString() {
		String str = "";
		if (root.getFather() == null)
			str += "[" + getComputationTime() + "]\n";
		str += "Profondita': " + root.getDepth() + "\n " + root;
		if (root instanceof SplittingNode) {
			str += "\n";
			str += "SINISTRA: ";
			str += ((SplittingNode) root).splitLeft;
			str += leftSubTree;
			str += "DESTRA: ";
			str += ((SplittingNode) root).splitRight;
			str += rightsubTree;
			str += "\n";
		}
		/*
		 * else str+="\n";
		 */

		return str;
	}

	public void prune(SnapshotData snap, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI a) {
		// tree pruning + incremental learning

		this.prune(snap, W, a, 0, snap.size() - 1);

	}

	public void drift(SnapshotData snap, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI a, int splits, String testType) {
		// tree pruning + incremental learning
		this.learnDriftingTree(snap, W, a, 0, snap.size() - 1,
				(int) Math.sqrt(snap.size()) * 2, splits, testType);
		// System.out.print("DRIFTED"+this);

	}

	private void learnDriftingTree(SnapshotData snap, SnapshotWeigth W,
			AutocorrelationI a, int beginExampleIndex, int endExampleIndex,
			int minExamples, int splits, String testType) {
		if (beginExampleIndex == -1 || endExampleIndex == -1)
			// updatenull
			/*
			 * for(int i = 0 ; i < avg.size(); i++){ avg.get(i).inull();
			 */
			return;
		if (root instanceof SplittingNode) {
			snap.sort(((SplittingNode) root).getSplitFeature(),
					beginExampleIndex, endExampleIndex);
			// updateavg
			if (leftSubTree != null)
				leftSubTree.learnDriftingTree(snap, W, a,
						leftSubTree.root.getBeginExampleIndex(),
						leftSubTree.root.getEndExampleIndex(), minExamples,
						splits, testType);

			if (rightsubTree != null)
				rightsubTree.learnDriftingTree(snap, W, a,
						rightsubTree.root.getBeginExampleIndex(),
						rightsubTree.root.getEndExampleIndex(), minExamples,
						splits, testType);

		} else {
			// Devo capire se ci sono abbastanza esempi per riapprendere il
			// sottoalbero per quei nodi che sono ora foglia
			boolean reLearn = false;
			for (Feature f : root.getSchema().getTargetList()) {
				if (f.getStopTree() && f.getCountTuples() >= minExamples) {
					// this.learnTree(snap, father.getSchema(), W, a,
					// root.getBeginExampleIndex(), root.endExampleIndex,
					// minExamples, root.getDepth(), splits, root.getFather());
					// this.learnTree(snap, root.getSchema(), W, a,
					// root.getBeginExampleIndex(), root.endExampleIndex,
					// minExamples, root.getDepth(), splits, root.getFather());
					reLearn = true;
					f.setStopTree(false);
					// break;
				}

			}
			if (reLearn && root.getBeginExampleIndex() != -1
					&& root.endExampleIndex != -1)
				this.learnTree(snap, root.getSchema(), W, a,
						root.getBeginExampleIndex(), root.endExampleIndex,
						minExamples, root.getDepth(), splits, root.getFather(),
						testType, 0);
			else {
				setLeaf(root.getSchema());
				// updateavg
			}
			return;
		}

	}

	private void propagateModel(Feature f) {
		// propago il modello deciso per f nei nodi sottostanti
		int fIndex = f.getIndexMining()
				- root.getSchema().getSpatialList().size();
		root.getSchema().getTargetList().set(fIndex, (Feature) f.clone());

		if (leftSubTree != null)
			leftSubTree.propagateModel(root.getSchema().getTargetList()
					.get(fIndex));
		if (rightsubTree != null)
			rightsubTree.propagateModel(root
					.getSchema()
					.getTargetList()
					.get(f.getIndexMining()
							- root.getSchema().getSpatialList().size()));

	}

	private Map<Integer, ErrorStatistic> prune(SnapshotData snap,
			SnapshotWeigth W, AutocorrelationI a, int begin, int end) {

		root.update(snap, a, W, begin, end);
		Map<Integer, ErrorStatistic> prunedE = root.estimateGetisAndOrdError(
				snap, begin, end); // stima del getis and ord sui nuovi dati

		if (root instanceof SplittingNode) {
			int midindex = begin;
			Feature splitFeature = ((SplittingNode) root).getSplitFeature();
			snap.sort(splitFeature, begin, end);
			double splitValue = ((SplittingNode) root).getSplitThereshld();
			for (midindex = begin; midindex <= end
					&& (Double) (snap.getSensorPoint(midindex).getMeasure(
							splitFeature.getIndexMining()).getValue()) <= splitValue; midindex++) {

			}
			Map<Integer, ErrorStatistic> leftE = null;
			Map<Integer, ErrorStatistic> rightE = null;
			if (begin <= midindex - 1)
				leftE = leftSubTree.prune(snap, W, a, begin, midindex - 1);
			else
				leftSubTree.notifyNoExample();
			if (midindex <= end)
				rightE = rightsubTree.prune(snap, W, a, midindex, end);
			else
				rightsubTree.notifyNoExample();
			Map<Integer, ErrorStatistic> unprunedE;

			if (leftE == null)
				unprunedE = rightE;
			else if (rightE == null)
				unprunedE = leftE;
			else {
				unprunedE = new HashMap<Integer, ErrorStatistic>();
				for (Feature f : root.getSchema().getTargetList()) {
					int totalTuples = end - begin + 1;
					double avge = ((leftE.get(f.getIndexMining()).error * leftE
							.get(f.getIndexMining()).countTuples) + (rightE
							.get(f.getIndexMining()).error * rightE.get(f
							.getIndexMining()).countTuples))
							/ (totalTuples);

					unprunedE.put(f.getIndexMining(), new ErrorStatistic(avge,
							totalTuples));

				}
			}
			Map<Integer, ErrorStatistic> outputE = new HashMap<Integer, ErrorStatistic>();

			double prunedError = 0.0, unprunedError = 0.0;
			for (ErrorStatistic e : prunedE.values())
				prunedError += e.error;
			for (ErrorStatistic e : unprunedE.values())
				unprunedError += e.error;
			double compensationFactor = (double) (end - begin)
					/ (end - begin + 1);
			if (prunedError * compensationFactor <= unprunedError) {
				// System.out.println("Pruning");

				for (Integer f : unprunedE.keySet()) { // rendo foglie tutti
														// gli
														// attributi
					// pruning
					root.getSchema().getTargetList()
							.get(f - root.getSchema().getSpatialList().size())
							.setStopTree(true);
					outputE.put(f, prunedE.get(f));
				}
				/*
				 * CORRADO perché propagare il modello ai sottoalberi quando
				 * successivamente verranno settati a NULL?
				 */
				/*
				 * if (leftSubTree != null) leftSubTree.propagateModel(root
				 * .getSchema() .getTargetList() .get(f -
				 * root.getSchema().getSpatialList() .size())); if (rightsubTree
				 * != null) rightsubTree.propagateModel(root .getSchema()
				 * .getTargetList() .get(f - root.getSchema().getSpatialList()
				 * .size()));
				 */
				/*
				 * CORRADO: Perché non fare outputE=prunedE;(??)
				 */

				/*
				 * CORRADO: Serve per evitare di far rendere null i sottoalberi
				 * ad ogni passaggio in modo tale da preservare la complessità
				 * temporale.
				 */
				// if (f == root.getSchema().getTargetList().get(0)
				// .getIndexMining()) {
				leftSubTree = null;
				rightsubTree = null;
				// }
				// trovare il modo di mantenere la finestra temporale
				root = new LeafNode(root.getSchema(),
						root.getBeginExampleIndex(), root.getEndExampleIndex(),
						root.getDepth(), root.getFather(), root.getavg());

				// System.out.println("Pruning");
				return outputE;
			} else {
				if (((leftSubTree.root.beginExampleIndex == -1 || leftSubTree.root.endExampleIndex == -1) && (leftSubTree.root instanceof SplittingNode))
						|| ((rightsubTree.root.beginExampleIndex == -1 || rightsubTree.root.endExampleIndex == -1) && (rightsubTree.root instanceof SplittingNode)))
					System.err
							.println("NO PRUNE, SI SPLITTINGNODE SETTATO VUOTO");
				return unprunedE;
			}
		} else
			return prunedE;

	}

	private void notifyNoExample() {
		root.beginExampleIndex = -1;
		root.endExampleIndex = -1;
	}

	private int treeDepth(Feature f) {
		int depth = 1;

		if (root.getSchema()
				.getTargetList()
				.get(f.getIndexMining()
						- root.getSchema().getSpatialList().size())
				.getStopTree())
			return depth;
		else {
			int depthL = 0;
			if (leftSubTree != null)
				depthL = leftSubTree.treeDepth(f);
			int depthR = 0;
			if (rightsubTree != null)
				depthR = rightsubTree.treeDepth(f);
			if (depthL > depthR)
				depth += depthL;
			else
				depth += depthR;

		}

		return depth;
	}

	private Map<Feature, Integer> treeDepth() {
		// profondità dell'albero per ciascuna feature numeric
		Map<Feature, Integer> depthMap = new HashMap<Feature, Integer>();
		for (Feature f : root.getSchema().getTargetList())
			depthMap.put(f, treeDepth(f));
		return depthMap;

	}

	// confronta gli alberi sulla base della media delle profondità sui target
	// attributes
	public int compareTo(Tree o) {
		Map<Feature, Integer> depthThis = this.treeDepth();
		Map<Feature, Integer> depthO = o.treeDepth();
		double sumDepthThis = 0.0, sumDepthO = 0.0;
		for (Integer i : depthThis.values())
			sumDepthThis += i;
		for (Integer i : depthO.values())
			sumDepthO += i;
		return ((Double) sumDepthThis).compareTo(sumDepthO);

	}

	public long getComputationTime() {
		return computationTime;
	}

	public void setComputationTime(long time) {
		computationTime = time;
	}

	public int countNodes() {
		if (root instanceof SplittingNode) {
			int left = 0;
			int right = 0;
			if (leftSubTree != null)
				left = leftSubTree.countNodes();
			if (rightsubTree != null)
				right = rightsubTree.countNodes();
			return 1 + left + right;
		} else
			return 1;

	}

	public int countLeaves() {
		if (root instanceof SplittingNode) {

			int left = 0;
			int right = 0;
			if (leftSubTree != null)
				left = leftSubTree.countLeaves();
			if (rightsubTree != null)
				right = rightsubTree.countLeaves();

			return left + right;
		} else
			return 1;

	}

	public String symbolicClusterDescription(String str) {
		if (root instanceof SplittingNode) {

			String left = "";
			String right = "";
			if (leftSubTree != null)
				left = leftSubTree.symbolicClusterDescription(str
						+ ((SplittingNode) root).splitLeft.toString());
			if (rightsubTree != null)
				right = rightsubTree.symbolicClusterDescription(str
						+ ((SplittingNode) root).splitRight.toString());
			;

			return (left + right);
		} else
			return str.replace("-", "") + "\n";

	}

	public void populateKNNModel(KNNModel knn) {
		if (root.centroid != null)
			knn.add(root.getIdNode(), root.centroid, (root.getModels()));
		if (leftSubTree != null)
			leftSubTree.populateKNNModel(knn);
		if (rightsubTree != null)
			rightsubTree.populateKNNModel(knn);

	}

	/*
	 * public void populateKNNModel(KNNModel knn, SnapshotData data, int begin,
	 * int end,SnapshotSchema schema,SnapshotWeigth W){ if(root instanceof
	 * SplittingNode){ // int midindex=begin; Feature
	 * splitFeature=((SplittingNode)root).getSplitFeature();
	 * data.sort(splitFeature,begin,end); double splitValue=
	 * ((SplittingNode)root).getSplitThereshld();
	 * for(midindex=begin;midindex<=end &&
	 * (Double)(data.getSensorPoint(midindex)
	 * .getMeasure(splitFeature.getIndexMining
	 * ()).getValue())<=splitValue;midindex++){
	 * 
	 * } if(leftSubTree !=null) leftSubTree.populateKNNModel(knn,data,
	 * begin,midindex-1,schema,W); if(rightsubTree !=null)
	 * rightsubTree.populateKNNModel(knn,data,midindex,end,schema,W); } else
	 * if(root.centroid!=null)
	 * knn.add(root.getIdNode(),root.centroid,data,begin,end,schema,W);
	 * 
	 * 
	 * 
	 * }
	 */

	private double intraClusterDisperison(SnapshotData snap, int begin, int end) {

		if (begin > end)
			return 0.0;
		if (root instanceof SplittingNode) {
			double leftDisp = 0.0, rightDisp = 0.0;
			int midindex = begin;
			Feature splitFeature = ((SplittingNode) root).getSplitFeature();
			snap.sort(splitFeature, begin, end);
			double splitValue = ((SplittingNode) root).getSplitThereshld();
			for (midindex = begin; midindex <= end
					&& (Double) (snap.getSensorPoint(midindex).getMeasure(
							splitFeature.getIndexMining()).getValue()) <= splitValue; midindex++) {

			}

			if (begin <= midindex - 1)

				leftDisp = leftSubTree.intraClusterDisperison(snap, begin,
						midindex - 1);
			else
				leftDisp = 0.0; // no contribution

			if (midindex <= end)
				rightDisp = rightsubTree.intraClusterDisperison(snap, midindex,
						end);

			else
				rightDisp = 0.0;

			return leftDisp + rightDisp;
		} else //
		{
			EuclideanDistance euclide = new EuclideanDistance(2);
			double disp = 0;
			for (int i = begin; i <= end; i++)
				for (int j = begin; j <= end; j++) {
					double d = euclide.compute(snap.getSensorPoint(i),
							snap.getSensorPoint(j));
					disp += d;

				}

			return disp / (Math.pow(end - begin + 1, 2));

		}

	}

	/*
	 * Calcola il set dei cluster attivi rispetto a snap
	 */
	private int computeActiveClusters(SnapshotData snap, int begin, int end) {
		if (begin > end)
			return 0;
		if (root instanceof SplittingNode) {
			int left = 0, right = 0;
			int midindex = begin;
			Feature splitFeature = ((SplittingNode) root).getSplitFeature();
			snap.sort(splitFeature, begin, end);
			double splitValue = ((SplittingNode) root).getSplitThereshld();
			for (midindex = begin; midindex <= end
					&& (Double) (snap.getSensorPoint(midindex).getMeasure(
							splitFeature.getIndexMining()).getValue()) <= splitValue; midindex++) {
				// System.out.println((Double)(snap.getSensorPoint(midindex).getMeasure(splitFeature.getIndexMining()).getValue()));
			}

			if (begin <= midindex - 1) {

				left = leftSubTree.computeActiveClusters(snap, begin,
						midindex - 1);

			} else
				// empty cluster
				left = 0;
			if (midindex <= end)
				right = rightsubTree.computeActiveClusters(snap, midindex, end);

			else
				right = 0;

			return left + right;
		} else
			//

			return 1;

	}

	public double computeSpatialIntraClusterDispersion(SnapshotData snap) {

		double disp = intraClusterDisperison(snap, 0, snap.size() - 1);
		int countActiveClusters = computeActiveClusters(snap, 0,
				snap.size() - 1);
		System.out.println("Acive clusters:" + countActiveClusters + " Leaves:"
				+ countLeaves());
		return disp / countActiveClusters;

	}

	private double interClusterDisperison(SnapshotData snap, int begin, int end) {
		if (begin > end - 1)
			return 0.0;

		if (root instanceof SplittingNode) {
			double leftDisp = 0.0, rightDisp = 0.0;
			int midindex = begin;
			Feature splitFeature = ((SplittingNode) root).getSplitFeature();
			snap.sort(splitFeature, begin, end);
			double splitValue = ((SplittingNode) root).getSplitThereshld();
			for (midindex = begin; midindex <= end
					&& (Double) (snap.getSensorPoint(midindex).getMeasure(
							splitFeature.getIndexMining()).getValue()) <= splitValue; midindex++) {

			}

			if (begin <= midindex - 1)
				leftDisp = leftSubTree.interClusterDisperison(snap, begin,
						midindex - 1);

			else
				leftDisp = 0.0; // no contribution

			if (midindex <= end)

				rightDisp = rightsubTree.interClusterDisperison(snap, midindex,
						end);
			else
				rightDisp = 0.0; // no contribution

			return leftDisp + rightDisp;
		} else //
		{
			EuclideanDistance euclide = new EuclideanDistance(2);
			double disp = 0;
			for (int i = begin; i <= end; i++) {
				for (int j = 0; j < begin; j++) {
					double d = euclide.compute(snap.getSensorPoint(i),
							snap.getSensorPoint(j));
					disp += d;
				}
				for (int j = end + 1; j < snap.size() - 1; j++) {
					double d = euclide.compute(snap.getSensorPoint(i),
							snap.getSensorPoint(j));
					disp += d;
				}

			}

			return disp / ((end - begin + 1) * (begin + snap.size() - 1 - end));

		}

	}

	public double computeSpatialInterClusterDispersion(SnapshotData snap) {
		double disp = interClusterDisperison(snap, 0, snap.size() - 1);
		int countActiveClusters = computeActiveClusters(snap, 0,
				snap.size() - 1);
		return disp / countActiveClusters;

	}

	/*
	 * Salva un cluster/foglia nel relativo file
	 */
	private void saveCluster(PrintStream output, SnapshotData snap, int begin,
			int end) throws IOException {
		if (begin > end - 1)
			return; // nessun cluster da salvare
		if (root instanceof SplittingNode) {
			int midindex = begin;
			Feature splitFeature = ((SplittingNode) root).getSplitFeature();
			snap.sort(splitFeature, begin, end);
			double splitValue = ((SplittingNode) root).getSplitThereshld();
			for (midindex = begin; midindex <= end
					&& (Double) (snap.getSensorPoint(midindex).getMeasure(
							splitFeature.getIndexMining()).getValue()) <= splitValue; midindex++) {
			}

			if (begin <= midindex - 1)
				leftSubTree.saveCluster(output, snap, begin, midindex - 1);

			if (midindex <= end)
				rightsubTree.saveCluster(output, snap, midindex, end);
		} else // salvataggio delle tuple
		{
			for (int i = begin; i <= end; i++) {
				SensorPoint s = snap.getSensorPoint(i);
				output.println(root.getIdNode() + ";" + s);
			}
		}
	}

	/*
	 * Genera file csv con informazione sulla distribuzione spaziale dei cluster
	 */
	public void saveSpatialClustering(String outfileName, SnapshotData snap,
			SnapshotSchema schema) throws IOException {
		FileOutputStream file = new FileOutputStream(outfileName);
		PrintStream Output = new PrintStream(file);

		Output.print("clusterId; sensorId;");
		for (Feature f : schema.getSpatialList())
			Output.print(f.getName() + ";");
		for (Feature f : schema.getTargetList())
			Output.print(f.getName() + ";");
		Output.println("");

		saveCluster(Output, snap, 0, snap.size() - 1);

		Output.close();

	}

	/*
	 * Associa il campione dei centroidi ad ogni nodo foglia per il training
	 * dello IDW (N.b. Da usare quando l'albero è già appreso
	 */
	public void sampling(SnapshotData snap, SnapshotSchema schema,
			int beginIndex, int endIndex, float ccentroidPerc,
			String centroidType) throws SplitException {

		root.beginExampleIndex = beginIndex; // aggiorno indici sulla base del
												// training set
		root.endExampleIndex = endIndex;
		if (root instanceof SplittingNode) {
			int midindex = beginIndex;
			Feature splitFeature = ((SplittingNode) root).getSplitFeature();
			snap.sort(splitFeature, beginIndex, endIndex);
			double splitValue = ((SplittingNode) root).getSplitThereshld();
			for (midindex = beginIndex; midindex <= endIndex
					&& (Double) (snap.getSensorPoint(midindex).getMeasure(
							splitFeature.getIndexMining()).getValue()) <= splitValue; midindex++) {
			}

			if (beginIndex <= midindex - 1)
				leftSubTree.sampling(snap, schema, beginIndex, midindex - 1,
						ccentroidPerc, centroidType);
			else
				throw new SplitException("Empty  Set for Sampling");

			if (midindex <= endIndex)
				rightsubTree.sampling(snap, schema, midindex, endIndex,
						ccentroidPerc, centroidType);
			else
				throw new SplitException("Empty  Set for Sampling");

		} else {// leaf node

			root.sampling(snap, centroidType, ccentroidPerc);

		}

	}

	public String Iterator() {
		String str = "";
		if (this.root.getFather() == null) {
			str += "Profondita': " + root.getDepth() + " ID: "
					+ root.getIdNode() + " [" + root.getBeginExampleIndex()
					+ ":" + root.getEndExampleIndex() + "] ";
			if (root instanceof SplittingNode) {
				str += ((SplittingNode) this.root).getSplitFeature().getName()
						+ "<=" + ((SplittingNode) root).getSplitThereshld()
						+ " \n";
				str += "FS di: " + root.getIdNode() + " Profondita': "
						+ leftSubTree.root.getDepth() + " ID: "
						+ leftSubTree.root.getIdNode() + " ["
						+ leftSubTree.root.getBeginExampleIndex() + ":"
						+ leftSubTree.root.getEndExampleIndex() + "]\n";
				str += "FD di: " + root.getIdNode() + " Profondita': "
						+ rightsubTree.root.getDepth() + " ID: "
						+ rightsubTree.root.getIdNode() + " ["
						+ rightsubTree.root.getBeginExampleIndex() + ":"
						+ rightsubTree.root.getEndExampleIndex() + "]\n";
				if (leftSubTree.root instanceof SplittingNode)
					str += leftSubTree.Iterator();
				if (rightsubTree.root instanceof SplittingNode)
					str += rightsubTree.Iterator();
			}
		} else {
			str += "Split di profondita': " + root.getDepth() + " SPLIT: "
					+ ((SplittingNode) this.root).getSplitFeature().getName()
					+ "<=" + ((SplittingNode) root).getSplitThereshld() + " \n";
			str += "FS di: " + root.getIdNode() + " Profondita': "
					+ leftSubTree.root.getDepth() + " ID: "
					+ leftSubTree.root.getIdNode() + " ["
					+ leftSubTree.root.getBeginExampleIndex() + ":"
					+ leftSubTree.root.getEndExampleIndex() + "]\n";
			str += "FD di: " + root.getIdNode() + " Profondita': "
					+ rightsubTree.root.getDepth() + " ID: "
					+ rightsubTree.root.getIdNode() + " ["
					+ rightsubTree.root.getBeginExampleIndex() + ":"
					+ rightsubTree.root.getEndExampleIndex() + "]\n";
			if (leftSubTree.root instanceof SplittingNode)
				str += leftSubTree.Iterator();
			if (rightsubTree.root instanceof SplittingNode)
				str += rightsubTree.Iterator();
		}
		return str;
	}
}
