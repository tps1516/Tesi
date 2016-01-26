package tree;

import java.io.Serializable;

import java.util.Comparator;
import java.util.HashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.TreeSet;

import javax.management.RuntimeErrorException;

import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;
import data.SensorPoint;
import data.datavalue.NumericValue;
import data.datavalue.Value;

import data.feature.AutocorrelationI;
import data.feature.CategoricalFeature;
import data.feature.Feature;
import data.feature.GetisOrdIndex;
import data.feature.MoranIndex;
import data.feature.NumericFeature;


import data.feature.ResubstitutionIndex;
import data.feature.ResubstitutionIndexOnGetisOrd;

import data.feature.SpatialResubstitutionIndex;

class SplitPoint {
	Feature f;
	double splitThreshold;
	int midindex; //begin - mid ... mid+1-end
	double autocorrelation;
	int rank=-1;
	SplitPoint(){};
	SplitPoint(Feature f, double splitThreshold, int midindex, double autocorrelation){
		
		this.f=f;
		this.splitThreshold=splitThreshold;
		this.midindex=midindex;
		this.autocorrelation=autocorrelation;
	}
	public String toString(){
		return f.getName()+":"+splitThreshold+":"+midindex+":"+autocorrelation+":"+rank+"\n";
	}
	
	
		

}

class ComparatorAutocorrelationAsc implements Comparator<SplitPoint>{

	
	public int compare(SplitPoint o1, SplitPoint o2) {
		// TODO Auto-generated method stub
		int v=((Double)o2.autocorrelation).compareTo(o1.autocorrelation);
		if(v==0)
			return 1;
			else return v;
	}
	
}

class ComparatorAutocorrelationDesc implements Comparator<SplitPoint>{

	
	public int compare(SplitPoint o1, SplitPoint o2) {
		// TODO Auto-generated method stub
		int v= ((Double)o1.autocorrelation).compareTo(o2.autocorrelation);
		if(v==0)
			return 1;
			else return v;
	}
	
	
}

class ComparatorThreshold implements Comparator<SplitPoint>{

	
	public int compare(SplitPoint o1, SplitPoint o2) {
		// TODO Auto-generated method stub
		String ke1=o1.f.getName()+o1.splitThreshold;
		String ke2=o2.f.getName()+o2.splitThreshold;
		int v= ke1.compareTo(ke2);
		if(v==0)
			return 1;
			else return v;
	}
	
	
}


public class SplittingNode extends Node {

	class SplitInfo implements Serializable{
		class SplitCondition implements Serializable{
			Feature feature;
			Double splitValue;
			String comparator ="<=";
			
			SplitCondition(Feature feature,Double splitValue){
				this.feature=feature;
				this.splitValue=splitValue;
			}
			SplitCondition(Feature feature,Double splitValue, String comparator){
				this(feature,splitValue);
				this.comparator=comparator;
			}
		
			public String toString(){
				String str="";
				for(int i=0;i<getDepth();i++)
					str+="-";
				return str+"("+feature.getName()+comparator+splitValue+")";
			}
			
			public boolean checkCondition(SensorPoint sp){
				Value spValue=sp.getMeasure(feature.getIndexMining());
				if(comparator.equals("<="))
					return ((Double)((NumericValue)spValue).getValue())<=((Double)splitValue);
				else
					return ((Double)((NumericValue)spValue).getValue())>((Double)splitValue);
			}
		}
		SplitCondition split;

		int beginIndex;
		int endIndex;
		
		SplitInfo(Feature f, Double value,int beginIndex, int endIndex){
			split=new SplitCondition(f,value);
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
		}

		SplitInfo(Feature f, Double value,int beginIndex, int endIndex,String comparator){
			split=new SplitCondition(f,value,comparator);
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
		}
	
		
		int getBeginindex(){
			return beginIndex;			
		}
		int getEndIndex(){
			return endIndex;
		}
		Double getSplitValue(int i){
			return split.splitValue;
		}
		Feature getSplitFeature(int i){
			return split.feature;
		}
		public String toString(){
			return split.toString();
			
		}

 
	}

	
	SplitInfo splitLeft;
	SplitInfo splitRight;
	
	
	String branch(SensorPoint sp){
		if(splitLeft.split.checkCondition(sp))
			return "left";
		else
			return "right";
	}

	
	Map<Feature,Double> heuristic=new HashMap<Feature, Double>();
	
	public SplittingNode( SnapshotData trainingSet,
			SnapshotSchema schema, int beginExampleIndex,
			int endExampleIndex, int minExamples,int depth,int step, Node father, String testType) throws SplitException {
		super(trainingSet, schema, beginExampleIndex, endExampleIndex,
				minExamples,depth,father);
		
		
		setSplitInfo(new ResubstitutionIndex(),trainingSet, null, beginExampleIndex, endExampleIndex,minExamples, step,testType);
	//	System.out.println("RESUB2");			
		
		
		
	}
	
	
	
	public SplittingNode(AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W,int beginExampleIndex,
			int endExampleIndex, int minExamples,int depth,int step, Node father, String testType) throws SplitException {
		super(a, trainingSet, schema, W, beginExampleIndex, endExampleIndex,
				minExamples,depth,father);
		
		
		setSplitInfo(a,trainingSet, W,beginExampleIndex, endExampleIndex,minExamples, step,testType);
					
		
		
		
	}
	
	
	//Annalisa Added for optimization
	
	public SplittingNode(Node n, AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W,int beginExampleIndex,
			int endExampleIndex, int minExamples,int depth,int step, Node father, String testType, int dim) throws SplitException {
		super(n, a, trainingSet, schema, W, beginExampleIndex, endExampleIndex,
				minExamples,depth,father);
		
		
		setSplitInfo(a,trainingSet, W,beginExampleIndex, endExampleIndex,minExamples, step,testType);
		super.initializedFeatureAvgNode(dim);
		
	}
	
	public SplittingNode(Node n, AutocorrelationI a, SnapshotData trainingSet,
			SnapshotSchema schema, SnapshotWeigth W,int beginExampleIndex,
			int endExampleIndex, int minExamples,int depth,int step, Node father, String testType, FeatureAveragesNode fAvgNode) throws SplitException {
		super(n, a, trainingSet, schema, W, beginExampleIndex, endExampleIndex,
				minExamples,depth,father);
		
		
		setSplitInfo(a,trainingSet, W,beginExampleIndex, endExampleIndex,minExamples, step,testType);
		super.setFeatureAvgNode(fAvgNode);
		
	}	
	
	
	private void setSplitInfo(AutocorrelationI a, SnapshotData trainingSet,
	 SnapshotWeigth W,int beginExampleIndex,
	int endExampleIndex, int minExample, int numberOfSplits, String testType)throws SplitException{
		
		
		
		
		
		//new annalisa
		//List<TreeSet<SplitPoint>> splitMap=new LinkedList<TreeSet<SplitPoint>>();
		
		
		//TreeSet<SplitPoint> splitMap=new TreeSet<SplitPoint>(new ComparatorAutocorrelationDesc());
		
		
		TreeSet<SplitPoint> splitMap;
		if(a  instanceof GetisOrdIndex || a instanceof ResubstitutionIndex || a instanceof SpatialResubstitutionIndex || a instanceof ResubstitutionIndexOnGetisOrd)
				splitMap=new TreeSet<SplitPoint>(new ComparatorAutocorrelationDesc());
		else
			splitMap=new TreeSet<SplitPoint>(new ComparatorAutocorrelationAsc());
	
		
		
		int splitTestN=0;
		int ct=0;
		int step;
		
		List<Feature> splitList=new LinkedList<Feature>();
		
		
		HashMap<Integer,ResubstitutionIndex> resubstituitionError=new HashMap<Integer,ResubstitutionIndex>();
		HashMap<Integer,MoranIndex> moranIndex=new HashMap<Integer,MoranIndex>();
		HashMap<Integer,ResubstitutionIndexOnGetisOrd> resubstituitionGOError=new HashMap<Integer,ResubstitutionIndexOnGetisOrd>();
		
		if(a instanceof MoranIndex){
			for(Feature t:schema.getTargetListNotLeaf()){
				resubstituitionError.put(t.getIndexMining(), new ResubstitutionIndex(trainingSet, t, W, beginExampleIndex, endExampleIndex));
				moranIndex.put(t.getIndexMining(), new MoranIndex(trainingSet, t, W, beginExampleIndex, endExampleIndex));
				}
		}
		else if(a instanceof ResubstitutionIndexOnGetisOrd){
			for(Feature t:schema.getTargetListNotLeaf()){
				resubstituitionGOError.put(t.getIndexMining(), new ResubstitutionIndexOnGetisOrd(trainingSet, t, W, beginExampleIndex, endExampleIndex));
			}
		}
		else if(a instanceof ResubstitutionIndex){
			for(Feature t:schema.getTargetListNotLeaf()){
				resubstituitionError.put(t.getIndexMining(), new ResubstitutionIndex(trainingSet, t, W, beginExampleIndex, endExampleIndex));				
				}
		}
		else throw new RuntimeException("Unknown split evalaution heuristic");
		
		if(testType.equals("spatial"))
			splitList.addAll(schema.getSpatialList());
		else if(testType.equals("target"))
			splitList.addAll(schema.getTargetList());
		else{
			splitList.addAll(schema.getSpatialList());	
			splitList.addAll(schema.getTargetList());
		}
		
		
		//for(Feature fi:schema.getSpatialList()){
		//for(Feature fi:schema.getTargetList()){
		for(Feature fi:splitList){
			HashMap<Integer,ResubstitutionIndex> resubstituitionErrorLeft=new HashMap<Integer,ResubstitutionIndex>();
			HashMap<Integer,ResubstitutionIndex> resubstituitionErrorRight=new HashMap<Integer,ResubstitutionIndex>();
			
			HashMap<Integer,MoranIndex> moranIndexLeft=new HashMap<Integer,MoranIndex>();
			HashMap<Integer,MoranIndex> moranIndexRight=new HashMap<Integer,MoranIndex>();
			
			HashMap<Integer,ResubstitutionIndexOnGetisOrd> resubstituitionGOErrorLeft=new HashMap<Integer,ResubstitutionIndexOnGetisOrd>();
			HashMap<Integer,ResubstitutionIndexOnGetisOrd> resubstituitionGOErrorRight=new HashMap<Integer,ResubstitutionIndexOnGetisOrd>();
			
			
			
			if(a instanceof MoranIndex){
				
				for(Feature t:schema.getTargetListNotLeaf()){
					resubstituitionErrorLeft.put(t.getIndexMining(), new ResubstitutionIndex());
					resubstituitionErrorRight.put(t.getIndexMining(), new ResubstitutionIndex(resubstituitionError.get(t.getIndexMining()).getSumX(), resubstituitionError.get(t.getIndexMining()).getSumX2(), resubstituitionError.get(t.getIndexMining()).getN()));
					
					moranIndexLeft.put(t.getIndexMining(), new MoranIndex());
					moranIndexRight.put(t.getIndexMining(), 
						new MoranIndex(moranIndex.get(t.getIndexMining()).getN(),
							moranIndex.get(t.getIndexMining()).getsumWij(),
							moranIndex.get(t.getIndexMining()).getSumWXYij(),
							moranIndex.get(t.getIndexMining()).getSumX(),
							moranIndex.get(t.getIndexMining()).getSumX2(),
							moranIndex.get(t.getIndexMining()).sumWijXi()));
					
					//System.out.println(moranIndexRight.get(t.getIndexMining()).get() + " "+ t.getAutocorrelation());
					
				}
			}
			else 			if(a instanceof ResubstitutionIndexOnGetisOrd){
				for(Feature t:schema.getTargetListNotLeaf()){
					resubstituitionGOErrorLeft.put(t.getIndexMining(), new ResubstitutionIndexOnGetisOrd());
					resubstituitionGOErrorRight.put(t.getIndexMining(), new ResubstitutionIndexOnGetisOrd(resubstituitionGOError.get(t.getIndexMining()).getSumX(), resubstituitionGOError.get(t.getIndexMining()).getSumX2(), resubstituitionGOError.get(t.getIndexMining()).getN()));
							
					//System.out.println(resubstituitionGOErrorRight.get(t.getIndexMining()).get() + " "+ t.getAutocorrelation());
					
				}
			} else
			if(a instanceof ResubstitutionIndex){
				
				for(Feature t:schema.getTargetListNotLeaf()){
					resubstituitionErrorLeft.put(t.getIndexMining(), new ResubstitutionIndex());
					resubstituitionErrorRight.put(t.getIndexMining(), new ResubstitutionIndex(resubstituitionError.get(t.getIndexMining()).getSumX(), resubstituitionError.get(t.getIndexMining()).getSumX2(), resubstituitionError.get(t.getIndexMining()).getN()));
					
				//	System.out.println(resubstituitionErrorRight.get(t.getIndexMining()).get() + " "+ t.getAutocorrelation());
				}
			}
			else throw new RuntimeException("Unknown split evalaution heuristic");
			trainingSet.sort(fi,beginExampleIndex, endExampleIndex); // order by attribute
			int cDistinct=trainingSet.countDistinct(fi, beginExampleIndex, endExampleIndex);
			if(numberOfSplits>0){
				step=cDistinct/numberOfSplits;
				if(step<1)
					step=1;
			}
			else
				step=1;
			//System.out.println("Depth:"+getDepth()+":Step:"+step+"Begin:"+beginExampleIndex+"End:"+endExampleIndex+"Distinct values"+cDistinct);
			double currentSplitValue= (Double)(trainingSet.getSensorPoint(beginExampleIndex).getMeasure(fi.getIndexMining()).getValue());
			for(int i=beginExampleIndex;i<=endExampleIndex;i++)
			{
				double value=(Double)(trainingSet.getSensorPoint(i).getMeasure(fi.getIndexMining()).getValue());
				if(value!=currentSplitValue){
					ct++;
					if(ct>=step){
						if((i-beginExampleIndex)>=minExample && (endExampleIndex-i+1)>=minExample)
						{
						
							
							//System.out.println("Evaluating "+ fi.getName()+"<="+currentSplitValue);
							
						//	LeafNode left=new LeafNode(a, trainingSet, schema, W, beginExampleIndex, i-1,minExample,getDepth()+1,this);
						//	LeafNode right=new LeafNode(a, trainingSet, schema, W, i, endExampleIndex,minExample,getDepth()+1,this);
							
							
							double p=((double)((i-1)-beginExampleIndex+1))/(endExampleIndex-beginExampleIndex+1);
							splitTestN++;
							int id=0; 
							double h=0.0; 
							for(Feature t:schema.getTargetListNotLeaf()){
								if((schema.getTargetList().size()>1 && (t.getIndexMining()!=fi.getIndexMining())) // se cisono molteplici split evito di riconsiderare la variabile dello split nella euristica
										||
										(schema.getTargetList().size()==1)
									)
										
								{
									//double ht=p*left.getHeuristic(t)+(1-p)*right.getHeuristic(t);
									double ht=0;
									
									
									if(a instanceof MoranIndex){
										ht=p*moranIndexLeft.get(t.getIndexMining()).get()+(1-p)*moranIndexRight.get(t.getIndexMining()).get();
										//System.out.println("M:"+left.getHeuristic(t)+" "+ moranIndexLeft.get(t.getIndexMining()).get() + " " +right.getHeuristic(t)+ " " +moranIndexRight.get(t.getIndexMining()).get() );
										
										// devo combinare con resubstitution error reduction
										//double leftR=new ResubstitutionIndex().compute(trainingSet, t, W, beginExampleIndex, i-1);
										//double rightR=new ResubstitutionIndex().compute(trainingSet, t, W, i, endExampleIndex);
										 
										 double leftRO=resubstituitionErrorLeft.get(t.getIndexMining()).get();
										 double rightRO=resubstituitionErrorRight.get(t.getIndexMining()).get();
										 
										// System.out.println("R:"+ leftR+ " "+leftRO+ " "+ rightR+ " "+rightRO);
										 
										 //double varianceReduction=resubstituitionError.get(t.getIndexMining()).get()-(p*leftR+(1-p)*rightR);
										 double varianceReduction=resubstituitionError.get(t.getIndexMining()).get()-(p*leftRO+(1-p)*rightRO);
										 ht+=varianceReduction;
										 ht/=2;
									}
									else if(a instanceof ResubstitutionIndexOnGetisOrd){
										
										//System.out.println("R:"+ left.getHeuristic(t)+ " "+resubstituitionGOErrorLeft.get(t.getIndexMining()).get()+ " "+ right.getHeuristic(t)+ " "+resubstituitionGOErrorRight.get(t.getIndexMining()).get());
										ht=p*resubstituitionGOErrorLeft.get(t.getIndexMining()).get()+(1-p)*resubstituitionGOErrorRight.get(t.getIndexMining()).get();
									}
									else if(a instanceof ResubstitutionIndex){
										double leftRO=resubstituitionErrorLeft.get(t.getIndexMining()).get();
										double rightRO=resubstituitionErrorRight.get(t.getIndexMining()).get();
									//	System.out.println("R:"+ left.getHeuristic(t)+ " "+leftRO+ " "+ right.getHeuristic(t)+ " "+rightRO);
										ht=p*leftRO+(1-p)*rightRO;
										
									}
									else throw new RuntimeException("Unknown split evalaution heuristic");
									//splitMap.get(id).add(new SplitPoint(fi, currentSplitValue, i-1, ht));
									//System.out.println(fi.getName()+currentSplitValue+t.getName()+":"+ht);
									id++;
									h+=ht;
								}
							}
							//new Annalisa
							splitMap.add(new SplitPoint(fi, currentSplitValue, i-1, h/id));
							
				
							
						}
						ct=0;
					}
				}
				currentSplitValue=value;
				
				
				if(a instanceof MoranIndex){
					// update resubstitutionErrorLeft and resubstitutionErrorRight
					for(Feature t:schema.getTargetListNotLeaf()){
						if((schema.getTargetList().size()>1 && (t.getIndexMining()!=fi.getIndexMining())) // se cisono molteplici split evito di riconsiderare la variabile dello split nella euristica
								||
								(schema.getTargetList().size()==1)
							){
						
							resubstituitionErrorLeft.get(t.getIndexMining()).addX(trainingSet, beginExampleIndex, i-1, W, t);
							resubstituitionErrorRight.get(t.getIndexMining()).subX(trainingSet, i+1, endExampleIndex,  W, t);
							
							moranIndexLeft.get(t.getIndexMining()).addX(trainingSet, beginExampleIndex, i-1,  W, t);
							moranIndexRight.get(t.getIndexMining()).subX(trainingSet, i+1, endExampleIndex,  W, t);
						}
								
					
					}
				}
				else if(a instanceof ResubstitutionIndexOnGetisOrd){
					for(Feature t:schema.getTargetListNotLeaf()){
						if((schema.getTargetList().size()>1 && (t.getIndexMining()!=fi.getIndexMining())) // se ci sono molteplici split evito di riconsiderare la variabile dello split nella euristica
								||
								(schema.getTargetList().size()==1)
							){
							
							resubstituitionGOErrorLeft.get(t.getIndexMining()).addX(trainingSet, beginExampleIndex, i-1, W, t);
							resubstituitionGOErrorRight.get(t.getIndexMining()).subX(trainingSet, i+1, endExampleIndex,  W, t);
							
							
						}
								
					
					}
				}
				else if(a instanceof ResubstitutionIndex){
					for(Feature t:schema.getTargetListNotLeaf()){
						if((schema.getTargetList().size()>1 && (t.getIndexMining()!=fi.getIndexMining())) // se ci sono molteplici split evito di riconsiderare la variabile dello split nella euristica
								||
								(schema.getTargetList().size()==1)
							){
							
							resubstituitionErrorLeft.get(t.getIndexMining()).addX(trainingSet, beginExampleIndex, i-1, W, t);
							resubstituitionErrorRight.get(t.getIndexMining()).subX(trainingSet, i+1, endExampleIndex,  W, t);
						
							
						}
								
					
					}
				}
				else throw new RuntimeException("Unknown split evalaution heuristic");
			}
			
		}
		if(splitTestN==0)
			throw new SplitException();
		
		SplitPoint bestSplitPoint=splitMap.first();
		
	
		trainingSet.sort(bestSplitPoint.f,beginExampleIndex,endExampleIndex);
	
		
		splitLeft=new SplitInfo(bestSplitPoint.f, bestSplitPoint.splitThreshold,beginExampleIndex, bestSplitPoint.midindex);
		splitRight=new SplitInfo(bestSplitPoint.f, bestSplitPoint.splitThreshold,bestSplitPoint.midindex+1,endExampleIndex,">");
	
		
	
		boolean isLeaf=true; // VALUTARE SE MANTENERE IL PRE PRUNING
		isLeaf=prepruning(trainingSet, W,beginExampleIndex,endExampleIndex,minExample,bestSplitPoint.midindex+1);
		
		
		if(bestSplitPoint.midindex==endExampleIndex)
			throw new SplitException("False splitting node");
		if(!isLeaf)
			throw new SplitException("Pre-pruning");
	}
			
	private boolean prepruning(SnapshotData trainingSet,SnapshotWeigth W,int beginExampleIndex,
			int endExampleIndex, int minExample, int i){
	boolean isLeaf=false;
	LeafNode left=new LeafNode(null,trainingSet, schema, W, beginExampleIndex, i-1,minExample,getDepth()+1,this);
	LeafNode right=new LeafNode(null,trainingSet, schema,W, i, endExampleIndex,minExample,getDepth()+1,this);

	Map<Integer,ErrorStatistic> prunedE=this.estimateError(trainingSet, beginExampleIndex, endExampleIndex);
	
	Map<Integer,ErrorStatistic> leftE=left.estimateError(trainingSet, beginExampleIndex, i-1);
	Map<Integer,ErrorStatistic> rightE=right.estimateError(trainingSet, i, endExampleIndex);
	Map<Integer,ErrorStatistic> unprunedEunprunedE=new HashMap<Integer, ErrorStatistic>();
	for(Feature f:getSchema().getTargetList())
	{
		if(!f.getStopTree()){
			double avge=(leftE.get(f.getIndexMining()).error+rightE.get(f.getIndexMining()).error)/(leftE.get(f.getIndexMining()).countTuples+rightE.get(f.getIndexMining()).countTuples);
			 
			double adjustingFactor=(double)(prunedE.get(f.getIndexMining()).countTuples-1)/(prunedE.get(f.getIndexMining()).countTuples+1);
			double prunedErr=prunedE.get(f.getIndexMining()).error/prunedE.get(f.getIndexMining()).countTuples;
			if (adjustingFactor*prunedErr<=avge)
			{
				this.getSchema().getTargetList().get(f.getIndexMining()-getSchema().getSpatialList().size()).setStopTree(true);
	//			System.out.println("Pre pruning");
			}
			else{
				isLeaf=true;
			//	System.out.println("No Pre pruning");
			}
		}
	}
	return isLeaf;
}
	
	Feature getSplitFeature(){
		return splitLeft.split.feature;
	}
	
	Double getSplitThereshld(){
		return splitLeft.split.splitValue;
	}
	

}
