package tree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

//import mbrModel.KNNModel;

import data.DistanceI;
import data.EuclideanDistance;
import data.SensorPoint;
import data.datavalue.Value;
import data.feature.AutocorrelationI;
import data.feature.CategoricalFeature;
import data.feature.Feature;
import data.feature.NumericFeature;
import forecast.FeatureForecastingModel;
import forecast.ForecastingModel;
import rForecast.RVar;
import rForecast.VARParameter;
import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import windowStructure.FeatureWindow;
import forecast.FeatureVARForecastingModel;

public class Tree implements Serializable, Comparable<Tree>, Iterable<Node> {

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
	 * learnTree(data,schema
	 * ,0,data.size()-1,(int)(Math.sqrt(data.size()))*2,1,numberOfSplits
	 * ,null,centroidPerc,centroidType,testType);
	 * 
	 * 
	 * }
	 */

	public Tree(SnapshotData data, SnapshotSchema schema,
			SnapshotWeigth weight, AutocorrelationI autocorrelation,
			int numberOfSplits, float ccentroidPerc, String centroidType,
			String testType, int dim, ArrayList<Object> rParameters) {
		learnTree(data, schema, weight, autocorrelation, 0, data.size() - 1,
				(int) (Math.sqrt(data.size())) * 2, 1, numberOfSplits, null,
				ccentroidPerc, centroidType, testType, dim, rParameters);

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

	// chiamato negli albari spaziali
	private void learnTree(SnapshotData data, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI autocorrelation, int beginIndex,
			int endIndex, int minimumExamples, int depth, int numberOfSplits,
			Node father, float ccentroidPerc, String centroidType,
			String testType, int dim, ArrayList<Object> rParameters) {

		this.father = father;
		Node testRoot = new LeafNode(autocorrelation, data, schema, W,
				beginIndex, endIndex, minimumExamples, depth, father);
		if (isLeaf(autocorrelation, testRoot.getSchema(), minimumExamples)) {

			root = testRoot;
			root.initializedFeatureAvgNode(dim);
			root.updateFeatureAvgNode(rParameters);

			// root.sampling(data,centroidType,ccentroidPerc);

		} else // split node
		{
			try {
				root = new SplittingNode(testRoot, autocorrelation, data,
						testRoot.getSchema(), W, beginIndex, endIndex,
						minimumExamples, depth, numberOfSplits, father,
						testType, dim);
			} catch (SplitException e) {

				setLeaf(testRoot.getSchema());
				root = testRoot;
				// root.sampling(data,centroidType,ccentroidPerc);
				root.initializedFeatureAvgNode(dim);
				root.updateFeatureAvgNode(rParameters);
				return;

			}

			root.updateFeatureAvgNode(rParameters);
			leftSubTree = new Tree();
			rightsubTree = new Tree();
			leftSubTree.learnTree(data, root.getSchema(), W, autocorrelation,
					((SplittingNode) root).splitLeft.beginIndex,
					((SplittingNode) root).splitLeft.endIndex, minimumExamples,
					depth + 1, numberOfSplits, root, ccentroidPerc,
					centroidType, testType, dim, rParameters);
			rightsubTree.learnTree(data, root.getSchema(), W, autocorrelation,
					((SplittingNode) root).splitRight.beginIndex,
					((SplittingNode) root).splitRight.endIndex,
					minimumExamples, depth + 1, numberOfSplits, root,
					ccentroidPerc, centroidType, testType, dim, rParameters);

		}

	}

	private void learnTreeOnPreexistModel(SnapshotData data,
			SnapshotSchema schema, SnapshotWeigth W,
			AutocorrelationI autocorrelation, int beginIndex, int endIndex,
			int minimumExamples, int depth, int numberOfSplits, Node father,
			float ccentroidPerc, String centroidType, String testType,
			FeatureWindow fAvgNode, ArrayList<Object> rParameters) {

		this.father = father;
		Node testRoot = new LeafNode(autocorrelation, data, schema, W,
				beginIndex, endIndex, minimumExamples, depth, father);
		if (isLeaf(autocorrelation, testRoot.getSchema(), minimumExamples)) {
			if (root == null) {
				root = testRoot;
				root.setFeatureAvgNode(fAvgNode);
				root.updateFeatureAvgNode(rParameters);
			} else {
				// root così come era diventa foglia
				setLeaf(root.getSchema());
				root.updateFeatureAvgNode(rParameters);
			}
		} else // split node
		{
			try {
				root = new SplittingNode(testRoot, autocorrelation, data,
						testRoot.getSchema(), W, beginIndex, endIndex,
						minimumExamples, depth, numberOfSplits, father,
						testType, fAvgNode);
			} catch (SplitException e) {

				setLeaf(testRoot.getSchema());
				if (root == null) {
					root = testRoot;
					root.setFeatureAvgNode(fAvgNode);
					root.updateFeatureAvgNode(rParameters);
				} else {
					setLeaf(root.getSchema());
					root.updateFeatureAvgNode(rParameters);
				}
				return;

			}

			leftSubTree = new Tree();
			rightsubTree = new Tree();
			leftSubTree.learnTreeOnPreexistModel(data, root.getSchema(), W,
					autocorrelation,
					((SplittingNode) root).splitLeft.beginIndex,
					((SplittingNode) root).splitLeft.endIndex, minimumExamples,
					depth + 1, numberOfSplits, root, ccentroidPerc,
					centroidType, testType, fAvgNode, rParameters);
			rightsubTree.learnTreeOnPreexistModel(data, root.getSchema(), W,
					autocorrelation,
					((SplittingNode) root).splitRight.beginIndex,
					((SplittingNode) root).splitRight.endIndex,
					minimumExamples, depth + 1, numberOfSplits, root,
					ccentroidPerc, centroidType, testType, fAvgNode,
					rParameters);
			root.updateFeatureAvgNode(rParameters);
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

	/*
	 * public String toString(){ String str=""; if(root.getFather()==null)
	 * str+="["+getComputationTime()+"]\n"; str+=root; if(root instanceof
	 * SplittingNode){ str+=((SplittingNode)root).splitLeft+"\n";
	 * str+=leftSubTree; str+=((SplittingNode)root).splitRight+"\n";
	 * str+=rightsubTree; } else str+="\n";
	 * 
	 * 
	 * return str; }
	 */

	public String toString() {
		String str = "";

		for (Node n : this) {
			str = str + n.toString() + "\n";
		}

		return str;
	}

	public void prune(SnapshotData snap, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI a) {
		// tree pruning + incremental learning

		this.prune(snap, W, a, 0, snap.size() - 1);

	}

	public void drift(SnapshotData snap, SnapshotSchema schema,
			SnapshotWeigth W, AutocorrelationI a, int splits,
			float ccentroidPerc, String centroidType, String testType,
			ArrayList<Object> rParameters) {
		// tree pruning + incremental learning
		this.learnDriftingTree(snap, W, a, 0, snap.size() - 1,
				(int) Math.sqrt(snap.size()) * 2, splits, ccentroidPerc,
				centroidType, testType, rParameters);
		// System.out.print("DRIFTED"+this);

	}

	private void learnDriftingTree(SnapshotData snap, SnapshotWeigth W,
			AutocorrelationI a, int beginExampleIndex, int endExampleIndex,
			int minExamples, int splits, float ccentroidPerc,
			String centroidType, String testType, ArrayList<Object> rParameters) {
		if (beginExampleIndex == -1 || endExampleIndex == -1) {
			root.insLastOfFather(rParameters);
			return;
		}
		if (root instanceof SplittingNode) {
			root.updateFeatureAvgNode(rParameters);
			snap.sort(((SplittingNode) root).getSplitFeature(),
					beginExampleIndex, endExampleIndex);

			{

				if (leftSubTree != null)
					leftSubTree.learnDriftingTree(snap, W, a,
							leftSubTree.root.getBeginExampleIndex(),
							leftSubTree.root.getEndExampleIndex(), minExamples,
							splits, ccentroidPerc, centroidType, testType,
							rParameters);
				if (rightsubTree != null)
					rightsubTree.learnDriftingTree(snap, W, a,
							rightsubTree.root.getBeginExampleIndex(),
							rightsubTree.root.getEndExampleIndex(),
							minExamples, splits, ccentroidPerc, centroidType,
							testType, rParameters);
			}
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
				this.learnTreeOnPreexistModel(snap, root.getSchema(), W, a,
						root.getBeginExampleIndex(), root.endExampleIndex,
						minExamples, root.getDepth(), splits, root.getFather(),
						ccentroidPerc, centroidType, testType,
						root.getFeatureAvgNode(), rParameters);
			else {
				root.updateFeatureAvgNode(rParameters);
				setLeaf(root.getSchema());
				// root.sampling(snap,centroidType,ccentroidPerc);

			}
			return;
		}

	}

	/*
	 * private void propagateModel(Feature f){ // propago il modello deciso per
	 * f nei ndi sottostanti int
	 * fIndex=f.getIndexMining()-root.getSchema().getSpatialList().size();
	 * root.getSchema().getTargetList().set(fIndex,(Feature)f.clone());
	 * 
	 * if(leftSubTree!=null)
	 * leftSubTree.propagateModel(root.getSchema().getTargetList().get(fIndex));
	 * if(rightsubTree!=null)
	 * rightsubTree.propagateModel(root.getSchema().getTargetList
	 * ().get(f.getIndexMining()-root.getSchema().getSpatialList().size()));
	 * 
	 * }
	 */

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
				for (Integer f : unprunedE.keySet()) { // rendo foglie tutti gl
														// iattributi
					// pruning
					root.getSchema().getTargetList()
							.get(f - root.getSchema().getSpatialList().size())
							.setStopTree(true);

					outputE.put(f, prunedE.get(f));
				}
				leftSubTree = null;
				rightsubTree = null;
				root = new LeafNode(root.getSchema(),
						root.getBeginExampleIndex(), root.getEndExampleIndex(),
						root.getDepth(), root.getFather(),
						root.getFeatureAvgNode());

				return outputE;
			} else
				return unprunedE;
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

	@Override
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
	 * classe interna realizza l'iteratore per l'alberp
	 */
	public class TreeIterator implements Iterator<Node> {

		private Tree tree;
		private LinkedList<Node> nodiVisitati;
		private int i;

		public TreeIterator(Tree t) {
			this.tree = t;
			nodiVisitati = new LinkedList<Node>();
			nodiVisitati = visitaInAmpiezza();
			i = 0;

		}

		@Override
		public boolean hasNext() {
			return i < nodiVisitati.size();
		}

		@Override
		public Node next() {
			int j = i;
			i++;
			return nodiVisitati.get(j);
		}

		private LinkedList<Node> visitaInAmpiezza() {
			LinkedList<Tree> daVisitare = new LinkedList<Tree>();
			LinkedList<Node> nodiVisitati = new LinkedList<Node>();

			daVisitare.add(tree);
			while (!daVisitare.isEmpty()) {
				Tree t = daVisitare.get(0);
				daVisitare.remove(0);
				if (t != null) {
					nodiVisitati.add(t.root);
					daVisitare.add(t.leftSubTree);
					daVisitare.add(t.rightsubTree);
				}
			}

			return nodiVisitati;
		}
	}

	/*
	 * restituisce l'iteratore per l'albero
	 */
	public Iterator<Node> iterator() {
		return new TreeIterator(this);
	}

	public ArrayList<Object> countPar() {
		SnapshotSchema schema = this.root.getSchema();
		HashMap<String, HashMap<String, Integer>> hmCombResult = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> counterByPar;
		HashMap<String, Double> hmOptimalRMSEByFeature = new HashMap<String, Double>();
		for (Feature f : schema.getTargetList()) {
			counterByPar = new HashMap<String, Integer>();
			for (VARParameter s : RVar.getVARParameters()) {
				String par = s.toString();
				counterByPar.put(par, 0);
			}
			hmCombResult.put(f.getName(), counterByPar);
			hmOptimalRMSEByFeature.put(f.getName(), 0.0);
		}

		String pars;

		for (Node n : this) {
			if (n instanceof LeafNode) {
				ForecastingModel model = ((LeafNode) n).getVARModel();
				if (model == null) {
					ArrayList<Object> finalResult = new ArrayList<Object>();
					finalResult.add(0, hmCombResult);
					finalResult.add(1, hmOptimalRMSEByFeature);
					return finalResult;
				}
				for (Feature f : schema.getTargetList()) {
					FeatureVARForecastingModel fmodel = (FeatureVARForecastingModel) model
							.getFeatureForecastingModel(f);
					pars = fmodel.getVARParameters();
					hmCombResult.get(f.getName()).put(pars,
							hmCombResult.get(f.getName()).get(pars) + 1);

					hmOptimalRMSEByFeature.put(
							f.getName(),
							hmOptimalRMSEByFeature.get(f.getName())
									+ fmodel.getRMSE());
				}
			}
		}
		ArrayList<Object> finalResult = new ArrayList<Object>();
		finalResult.add(0, hmCombResult);
		finalResult.add(1, hmOptimalRMSEByFeature);
		return finalResult;
	}

	public HashMap<Integer, ForecastingModel> deriveForecastingModel(
			SnapshotData data) {

		HashMap<Integer, ForecastingModel> result = new HashMap<Integer, ForecastingModel>();
		res(this, data, 0, data.size() - 1, result);

		return result;
	}

	private void res(Tree tree, SnapshotData data, int begin, int end,
			HashMap<Integer, ForecastingModel> hm) {
		if (tree.root instanceof SplittingNode) {
			// PASSO RICORSIVO
			SplittingNode node = (SplittingNode) this.root;
			Feature featureSplit = node.getSplitFeature();
			data.sort(featureSplit, begin, end);
			for (int i = begin; i <= end; i++) {
				/*
				 * controllo se il valore per quella feature per quel sensore
				 * point è maggiore della soglia
				 */
				if (((Double) data.getSensorPoint(i)
						.getMeasure(featureSplit.getIndexMining()).getValue()) > node
						.getSplitThereshld()) {
					res(tree.leftSubTree, data, begin, i-1, hm);
					res(tree.rightsubTree, data, i, end, hm);
					break;
				}
				if(i==end){
					res(tree.rightsubTree, data, begin, end, hm);
				}
			}
		} else {
			// PASSO BASE
			for (int i = begin; i <= end; i++) {
				hm.put(data.getSensorPoint(i).getId(),
						((LeafNode) tree.root).getVARModel());
			}
		}
		//CONTROLLO SE TUTTE LE ISTANZE RICADONO IN UN NODO
		
	}

	public boolean existVARModel() {
		return this.root.getFeatureAvgNode().temporalWindowsIsFull();
	}
}
