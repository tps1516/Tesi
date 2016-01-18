package snapshot;

import java.io.BufferedReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import mbrModel.MBR;



import data.GetisAndOrdZ;
import data.SensorPoint;
import data.datavalue.CategoricalValue;
import data.datavalue.NumericValue;
import data.datavalue.Value;
import data.feature.CategoricalFeature;
import data.feature.Feature;
import data.feature.NumericFeature;
import data.feature.SpatialFeature;

public class SnapshotData implements Iterable<SensorPoint>{
 private List<SensorPoint> data=new ArrayList<SensorPoint>();
 private int idSnapshot;
 private static int idSnapshotGenerator=1;
 
 private int numberOfSpatialFeatures=2;
 private int numberOfTargetFeatures=1;
 private static int idSensorGenerator=1; 
 static int idCode=0;
 
 
 public static void resetIdSnapshotGenerator(){
	 idSnapshotGenerator=1;
 }
 
 
 /*Skip inactive nodes
  * 
  */
 public  SnapshotData(BufferedReader stream, SnapshotSchema schema,Set<Integer>inactive) throws IOException{
	 String inLine = stream.readLine(); // prima tupla
	
	 numberOfSpatialFeatures=schema.getSpatialList().size();
	 numberOfTargetFeatures=schema.getTargetList().size();
	 
	 Set<Integer> idSet =new TreeSet<Integer>();
	 schema.reset();
	 //System.out.println(schema);

	 
	 while(inLine!=null && !inLine.equals("@")){
				 
		 String str[]=inLine.split(",");
		 
		 SensorPoint sp;
		 if(schema.getKey()!=null)
		 {
			 Integer idKey=new Integer(str[schema.getKey().getIndexStream()]);
			 if(idSet.contains(idKey)){
				 System.out.println("DUPLICATE:"+	 inLine);
				 inLine=stream.readLine(); // skip line
				 
				 continue;
			 }
			 if(inactive.contains(idKey)){
				 System.out.println("inactive:"+	 inLine);
				 inLine=stream.readLine(); // skip line
				 
				 continue;
			 }
			 sp=new SensorPoint(idKey);
			 idSet.add(idKey);
		 }
		 else
			 sp=new SensorPoint(idSensorGenerator++);
		 
		 for(SpatialFeature sf:schema.getSpatialList()){
			 int indexStream =sf.getIndexStream();
			 int indexMining=sf.getIndexMining();
			 sp.addMeasure(new NumericValue(new Double(str[indexStream]),indexMining));
			 sf.setMax(new Double(str[indexStream])); // update max
			 sf.setMin(new Double(str[indexStream])); //update min
			 sf.setMean(new Double(str[indexStream])); //update mean
		 }
		
		 for(Feature f:schema.getTargetList()){
			 int indexStream =f.getIndexStream();
			 int indexMining=f.getIndexMining();
			 if(f instanceof NumericFeature) {
				
				if(!str[indexStream].equals("?")){
					sp.addMeasure(new NumericValue(new Double(str[indexStream]),indexMining));
					
					((NumericFeature)f).setMax(new Double(str[indexStream])); // update max
					((NumericFeature)f).setMin(new Double(str[indexStream])); //update min
					((NumericFeature)f).setMean(new Double(str[indexStream])); //update mean
				}
				else
					sp.addMeasure(new NumericValue(null,indexMining));
				
			 }
			 else if (f instanceof CategoricalFeature) {
				
				if(!str[indexStream].equals("?")){
					sp.addMeasure(new CategoricalValue(str[indexStream],indexMining));
					((CategoricalFeature)f).addCategory(str[indexStream]);
				}
				else
					sp.addMeasure(new CategoricalValue(null,indexMining));
			 }
		 }
		 
		 data.add(sp);
		 inLine=stream.readLine();
		 
	 }
	// System.out.println(schema);
	 if (inLine==null){
		// System.out.println(SnapshotData.idSnapshotGenerator);
		// throw new EOFException();
	 }


 }
 
 

 private boolean inputSnaphot(BufferedReader stream, SnapshotSchema schema) throws IOException{
	 String inLine = stream.readLine(); // prima tupla
	 if(inLine==null) return false;
	 numberOfSpatialFeatures=schema.getSpatialList().size();
	 numberOfTargetFeatures=schema.getTargetList().size();
	 
	 Set<Integer> idSet =new TreeSet<Integer>();
	 schema.reset();
	 //System.out.println(schema);

	 
	 while(inLine!=null && !inLine.equals("@")){
				 
		 String str[]=inLine.split(",");
		 
		 SensorPoint sp;
		 if(schema.getKey()!=null)
		 {
			 Integer idKey=new Integer(str[schema.getKey().getIndexStream()]);
			 if(idSet.contains(idKey)){
				 System.out.println("DUPLICATE:"+	 inLine);
				 inLine=stream.readLine(); // skip line
				 
				 continue;
			 }
			 sp=new SensorPoint(idKey);
			 idSet.add(idKey);
		 }
		 else
			 sp=new SensorPoint(idSensorGenerator++);
		 
		 for(SpatialFeature sf:schema.getSpatialList()){
			 int indexStream =sf.getIndexStream();
			 int indexMining=sf.getIndexMining();
			 sp.addMeasure(new NumericValue(new Double(str[indexStream]),indexMining));
			 sf.setMax(new Double(str[indexStream])); // update max
			 sf.setMin(new Double(str[indexStream])); //update min
			 sf.setMean(new Double(str[indexStream])); //update mean
		 }
		
		 for(Feature f:schema.getTargetList()){
			 int indexStream =f.getIndexStream();
			 int indexMining=f.getIndexMining();
			 if(f instanceof NumericFeature) {
				
				if(!str[indexStream].equals("?")){
					sp.addMeasure(new NumericValue(new Double(str[indexStream]),indexMining));
					
					((NumericFeature)f).setMax(new Double(str[indexStream])); // update max
					((NumericFeature)f).setMin(new Double(str[indexStream])); //update min
					((NumericFeature)f).setMean(new Double(str[indexStream])); //update mean
				}
				else
					sp.addMeasure(new NumericValue(null,indexMining));
				
			 }
			 else if (f instanceof CategoricalFeature) {
				
				if(!str[indexStream].equals("?")){
					sp.addMeasure(new CategoricalValue(str[indexStream],indexMining));
					((CategoricalFeature)f).addCategory(str[indexStream]);
				}
				else
					sp.addMeasure(new CategoricalValue(null,indexMining));
			 }
		 }
		 
		 data.add(sp);
		 inLine=stream.readLine();
		 
	 }
	// System.out.println(schema);
	 if (inLine==null){
		// System.out.println(SnapshotData.idSnapshotGenerator);
		// throw new EOFException();
	 }
	 return true;
	 

 }
 
 public void scaledSchema(SnapshotSchema schema){
	 for (Feature f:schema.getTargetList()){
		 if(f instanceof NumericFeature){
			 NumericFeature ff=(NumericFeature)f;
			 double min=ff.getMin();
			 double max=ff.getMax();
			 double mean=0.0;
			 int ct=0;
			 for(int i=0;i<size();i++)
			 {
				Value v =getSensorPoint(i).getMeasure(f.getIndexMining());
				if(!v.isNull()){
					ct++;
					double scaledV=((NumericValue)v).scale(min, max);
					mean+=scaledV;
				}
				
			 }
			 mean/=ct;
			 ff.setScaledMean(mean);
		 }
	 }
	 
 }
 public void updateGetisAndOrd(SnapshotWeigth W,SnapshotSchema schema) {
	 
	// scaledSchema(schema);
	 
	 
	 for(int i=0;i<data.size();i++){
		 
		 for(Feature f:schema.getTargetList()){
			 	//replace value with getis and ord
			 if(f instanceof NumericFeature){
				 
			
				 double getisandOrd=new GetisAndOrdZ().compute(this, f, W,	i,((NumericFeature)f).getMin(), ((NumericFeature)f).getMax()); 
			/*	if(getisandOrd>-2 && getisandOrd<2 )
					 getisandOrd=0;
				 else
					 if(getisandOrd>=2)
						 getisandOrd=2;
					 else
						 getisandOrd=-2;
				 
				*/
				 ((NumericValue)data.get(i).getMeasure(f.getIndexMining())).setGetis(getisandOrd);
			 }
		 }
	 }
//	 System.out.println(this);
	 
	 
 }
 
 public SnapshotData(BufferedReader stream, SnapshotSchema schema) throws IOException {
	 
	 idSnapshot=idSnapshotGenerator++;
	 boolean flag =inputSnaphot(stream, schema);
	 if(!flag)
		 throw new IOException("End Of Stream");
 }
 

 public SnapshotData(BufferedReader stream, SnapshotSchema schema, boolean testingSnap) throws IOException {
	 
	 idSnapshot=idSnapshotGenerator-1;
	 inputSnaphot(stream, schema);
	 
	 
 }
 
 public SensorPoint getSensorPoint(int index){
	 return data.get(index);
 }
 
 public int size(){
	 return data.size();
 }
 public int getIdSnapshot(){
	 return idSnapshot;
 }
 public int getSpatialFeaturesSize(){
	 return numberOfSpatialFeatures;
 }
 public int getTargetFeaturesSize(){
	 return numberOfTargetFeatures;
 }
 
 public int countDistinct(Feature f,int begin,int end){
 int ct=1;
 
 Double xOld=(Double)(data.get(begin).getMeasure(f.getIndexMining())).getValue();
 for(int j=begin+1;j<=end;j++){
	 Double x=(Double)(data.get(j).getMeasure(f.getIndexMining())).getValue();
	 if(!x.equals(xOld))
	 {
		 ct++;
		 xOld=x;
	 }
	 
 }
 return ct;
	
 }
public void sort(Feature f,int begin,int end){
	 quicksort(f, begin, end);
 }
 
 private  int partition(Feature attribute, int inf, int sup){
		int i,j;
	
		i=inf; 
		j=sup; 
		int	med=(inf+sup)/2;
		double x=(Double)(data.get(med).getMeasure(attribute.getIndexMining())).getValue();
		
		swap(inf,med);
	
		while (true) 
		{
			double xi=(Double)(data.get(i).getMeasure(attribute.getIndexMining())).getValue();
			
			while(i<=sup && xi<=x){ 
				i++; 
				if(i<=sup)
					xi=(Double)(data.get(i).getMeasure(attribute.getIndexMining())).getValue();
				
			}
		
			double xj=(Double)(data.get(j).getMeasure(attribute.getIndexMining())).getValue();
			while(xj>x) {
				j--;
				xj=(Double)(data.get(j).getMeasure(attribute.getIndexMining())).getValue();
			
			}
			
			if(i<j) { 
				swap(i,j);
			}
			else break;
		}
		swap(inf,j);
		return j;

	}
 
	// scambio esempio i con esempio j
	private void swap(int i,int j){
				SensorPoint temp;
		temp=data.get(i);
		data.set(i, data.get(j));
		data.set(j,temp);
		
		
	}
	

 private void quicksort(Feature attribute, int inf, int sup){
		
		if(sup>=inf){
			
			int pos;
			pos=partition(attribute, inf, sup);
			if ((pos-inf) < (sup-pos+1)) {
				quicksort(attribute, inf, pos-1); 
				quicksort(attribute, pos+1,sup);
			}
			else
			{
				quicksort(attribute, pos+1, sup); 
				quicksort(attribute, inf, pos-1);
			}
		}
		
	}

 
 /*public Map<Integer,ErrorStatistic> estimateError(List<Feature> targetList, int begin,int end){
		Map<Integer,ErrorStatistic> error=new HashMap<Integer,ErrorStatistic>();
		for(Feature f:targetList){
			
			if(f instanceof NumericFeature){
				double sumY=0.0;
				double sumY2=0.0;
				int ctTuples=0;
				for(int i=begin;i<=end;i++){
					Value v=getSensorPoint(i).getMeasure(f.getIndexMining());
					
					if(!v.isNull())
					{
						ctTuples++;
						sumY+=(Double)(v.getValue());
						sumY2+=Math.pow((Double)(v.getValue()),2);
					}
					//else
					//	System.out.println("Null Value on "+ f.getName()+"["+getSensorPoint(i).getId()+"]");
				}
				System.out.println(f.getName()+ " avg:"+sumY/ctTuples);
					double e=sumY2-ctTuples*Math.pow(sumY/ctTuples, 2);
					error.put(f.getIndexMining(), new ErrorStatistic(e,ctTuples));
				}
				else
				{
					Map<String,Integer> frequency=new HashMap<String, Integer>();
					int ctTuples=0;
					for(int i=begin;i<=end;i++){
						Value v=getSensorPoint(i).getMeasure(f.getIndexMining()-getSpatialFeaturesSize());
						if(!v.isNull()){
							ctTuples++;
							if(frequency.containsKey(v))
								frequency.put((String)v.getValue(), frequency.get(v)+1);
							else
								frequency.put((String)v.getValue(), 1);
						}
					}
				
					int max=-1;
					for(String i:frequency.keySet()){
						if(frequency.get(i)>=max)
							max=frequency.get(i);
					}
					error.put(f.getIndexMining(), new ErrorStatistic((double)(ctTuples-max),ctTuples));
				}
			}
		
		
		
		return error;
 }
		
*/
 public String toString(){
		String str="";
		Iterator<SensorPoint> it=data.iterator();
		while(it.hasNext()){
			str+=it.next()+"\n";
		}
		return str;
		
		
	}
 
 
	public SensorPoint computeCentroid(SnapshotSchema schema, int beginExampleIndex, int endExampleIndex)
	{
		int size=schema.getSpatialList().size();
		SensorPoint point=new SensorPoint(-1);
		
		for(int i=0;i<size;i++)
			point.addMeasure(new NumericValue(0.0,i));
		
		for(int i=beginExampleIndex;i<=endExampleIndex;i++){
			int j=0;
			for(SpatialFeature f:schema.getSpatialList()){
				NumericValue v=(NumericValue)getSensorPoint(i).getMeasure(f.getIndexMining());
				NumericValue pointV=(NumericValue)point.getMeasure(f.getIndexMining());
				NumericValue newV=new NumericValue((Double)v.getValue()+(Double)pointV.getValue(), v.getAttributeIndex());
				point.setMeasure(newV);
				j++;
			}
		}
		for(int i=0;i<size;i++){
			NumericValue pointV=(NumericValue)point.getMeasure(i);
			NumericValue newV=new NumericValue((Double)pointV.getValue()/(endExampleIndex-beginExampleIndex+1), pointV.getAttributeIndex());
			point.setMeasure(newV);
			
		}
		return point;
		
	}

	
/*
	private void populateQUADTREE(SnapshotSchema schema, List<SensorPoint> centroid, Set<Integer> mbrData, int beginExampleIndex, int endExampleIndex, MBR mbr, int currentDepth, int maxDepth){
		
		
	
	//	float density=(float)mbr.getCardinality()/mbrData.size();
	
//		if(density>0.75 || currentDepth==maxDepth)
		if(currentDepth==maxDepth)
			//stop quadtree decomposition
		{
			SensorPoint centre=mbr.determineCentre(this,beginExampleIndex,endExampleIndex);
			centroid.add(centre);
		}
		else
			//quadtree decomposition of mbr
		{
			//if(mbr.getCardinality()<5)
			if(mbr.getCardinality()<1)
				return;
			MBR mbr1=null,mbr2=null,mbr3=null,mbr4=null;
			Set<Integer>mbrData1=null,mbrData2=null,mbrData3=null,mbrData4=null;
			boolean q1=false,q2=false,q3=false,q4=false;
			double cX=mbr.getCentreX();
			double cY=mbr.getCentreY();
			//sorting on X
			quicksort(schema.getSpatialList().get(0), beginExampleIndex,endExampleIndex);
			int splitX=endExampleIndex;
			int splitY1=-1,splitY2=-1;
			for(int i=beginExampleIndex;i<=endExampleIndex;i++)
				if(((Double)getSensorPoint(i).getMeasure(0).getValue())>cX){
					splitX=i-1;
					break;
				}
			
			
			//sorting on Y
			if(splitX>=beginExampleIndex){//esistono Q1 e Q3
				quicksort(schema.getSpatialList().get(1), beginExampleIndex, splitX);
				q1=true;
				q3=true;
			
				splitY1=splitX;
			
				for(int i=beginExampleIndex;i<=splitX;i++)
					if(((Double)getSensorPoint(i).getMeasure(1).getValue())>cY){
						splitY1=i-1;
						break;
				}
				if(splitY1==splitX) // non esiste q1, ma un unico quadrante che unisce q1 e q3
				{
					q1=false;
					mbr3=computeMBR(schema,beginExampleIndex,splitY1); //mbr3=new MBR(mbr.getMinX(),mbr.getMinY(),cX,cY,splitY1-beginExampleIndex+1);
					mbrData3=new TreeSet<Integer>();
					for(int i=beginExampleIndex;i<=splitY1;i++)
						mbrData3.add(i);
					
				}
				else if(splitY1==beginExampleIndex-1)// non esiste q3
				{
					q3=false;
					mbr1=computeMBR(schema, splitY1+1, splitX);//mbr1=new MBR(mbr.getMinX(), cY, cX, mbr.getMaxY(), splitX-splitY1+1);
					mbrData1=new TreeSet<Integer>();
					for(int i=splitY1+1;i<=splitX;i++)
						mbrData1.add(i);
					
				}
				else{						
					mbr1=computeMBR(schema, splitY1+1, splitX);//mbr1=new MBR(mbr.getMinX(), cY, cX, mbr.getMaxY(), splitX-splitY1+1);
					mbr3=computeMBR(schema,beginExampleIndex,splitY1); //mbr3=new MBR(mbr.getMinX(),mbr.getMinY(),cX,cY,splitY1-beginExampleIndex+1);
					mbrData1=new TreeSet<Integer>();
					mbrData3=new TreeSet<Integer>();
					for(int i=beginExampleIndex;i<=splitY1;i++)
						mbrData3.add(i);
				
					for(int i=splitY1+1;i<=splitX;i++)
						mbrData1.add(i);
			
				}
			}
			
			if(splitX+1<=endExampleIndex){ //esistono Q2 ae Q4
				q2=true;
				q4=true;
			
				quicksort(schema.getSpatialList().get(1), splitX+1, endExampleIndex);
				splitY2=endExampleIndex;
				for(int i=splitX+1;i<=endExampleIndex;i++){
					if(((Double)getSensorPoint(i).getMeasure(1).getValue())>cY){
						splitY2=i-1;
						break;
					}
				}
				if(splitY2==endExampleIndex) // non esiste q2, ma un unico quadrante che unisce q2 e q4
				{
					q2=false;
					mbr4=computeMBR(schema, splitX+1, splitY2); //mbr4=new MBR(cX, mbr.getMinY(), mbr.getMaxX(), cY, splitY2-splitX+1);
					mbrData4=new TreeSet<Integer>();
					for(int i=splitX+1;i<=splitY2;i++)
						mbrData4.add(i);
				}
				else if(splitY2==splitX) //non esiste q4
				{
					q4=false;
					mbr2=computeMBR(schema, splitY2+1, endExampleIndex);
					mbrData2=new TreeSet<Integer>();
					for(int i=splitY2+1;i<=endExampleIndex;i++)
						mbrData2.add(i);
					
				}
				else{ //esiste q2 e q4
					mbr2=computeMBR(schema, splitY2+1, endExampleIndex);
				//mbr2=new MBR(cX, cY, mbr.getMaxX(), mbr.getMaxY(), endExampleIndex-splitY2+1);
				
					mbr4=computeMBR(schema, splitX+1, splitY2); //mbr4=new MBR(cX, mbr.getMinY(), mbr.getMaxX(), cY, splitY2-splitX+1);
					mbrData2=new TreeSet<Integer>();
					for(int i=splitY2+1;i<=endExampleIndex;i++)
						mbrData2.add(i);
				
					mbrData4=new TreeSet<Integer>();
					for(int i=splitX+1;i<=splitY2;i++)
						mbrData4.add(i);
				}
			}
		
			

		
			for(Integer i:mbrData){
				if(i<beginExampleIndex || i> endExampleIndex)
				//examples not falling in the current leaf (these examples haven't  be sorted again during the mbr decomposition)
				{
					SensorPoint s=data.get(i);
					double sX=(Double)(s.getMeasure(0).getValue());
					double sY=(Double)(s.getMeasure(1).getValue());
					if(q1 && sX>=mbr1.getMinX() && sX<=mbr1.getMaxX() && sY>=mbr1.getMinY() && sY<=mbr1.getMaxY())
						mbrData1.add(i);
					else if(q2 && sX>=mbr2.getMinX() && sX<=mbr2.getMaxX() && sY>=mbr2.getMinY() && sY<=mbr2.getMaxY())
						mbrData2.add(i);
					else if(q3 && sX>=mbr3.getMinX() && sX<=mbr3.getMaxX() && sY>=mbr3.getMinY() && sY<=mbr3.getMaxY())
					mbrData3.add(i);
					else if(q4 && sX>=mbr4.getMinX() && sX<=mbr4.getMaxX() && sY>=mbr4.getMinY() && sY<=mbr4.getMaxY())
						mbrData4.add(i);
					
				}
			}//end for
					
	
			if(q3)
				populateQUADTREE(schema, centroid, mbrData3, beginExampleIndex, splitY1,mbr3,currentDepth+1,maxDepth);
			if(q1)
				populateQUADTREE(schema, centroid, mbrData1, splitY1+1, splitX,mbr1,currentDepth+1,maxDepth);
			if(q4)
				populateQUADTREE(schema, centroid, mbrData4, splitX+1, splitY2,mbr4,currentDepth+1,maxDepth);
			if(q2)
				populateQUADTREE(schema, centroid, mbrData2, splitY2+1, endExampleIndex,mbr2,currentDepth+1,maxDepth);
			
			
			
		}
			
	}*/
	
	class QUADTREESplit implements Comparable<QUADTREESplit>{
		int beginExampleIndex;
		int endExampleIndex;
		MBR mbr;
		int depth;
		int father;
		Set<QUADTREESplit> children=new TreeSet<SnapshotData.QUADTREESplit>();
		

		int id=idCode++;
		
		Set<Integer> mbrData;
		QUADTREESplit(Set<Integer> mbrData,	int beginExampleIndex,
		int endExampleIndex,
		MBR mbr,
		int depth, int father){
			this.mbr =mbr;
			this.beginExampleIndex=beginExampleIndex;
			this.endExampleIndex=endExampleIndex;
			this.mbrData=mbrData;
			this.depth=depth;
			this.father=father;
			
		}
		public String toString(){
			//return id+ " father:"+father+ " children:"+children+ " numExample:"+(endExampleIndex-beginExampleIndex+1);
			return id+ " father:"+father+ " numExample:"+(endExampleIndex-beginExampleIndex+1);
		}
		@Override
		public int compareTo(QUADTREESplit o) {
			// TODO Auto-generated method stub
			/*if(father<=this.father)
				return -1;
			else return +1;*/
			if(endExampleIndex-beginExampleIndex+1<=o.endExampleIndex-o.beginExampleIndex+1) 
				return 1; 
						else return -1;
		}
	}
	private void populateQUADTREE(SnapshotSchema schema, List<SensorPoint> centroid, Map<Integer,Set<QUADTREESplit>> quad, int numCentroids){
		//int maxDepthEst=(int)(Math.log10(numCentroids)/Math.log10(4));
				
		
		int first=0;
	
	
		while(centroid.size()+quad.get(first).size()<numCentroids )
		{
			if(first>0 && quad.get(first).size() == quad.get(first-1).size())
				break;
				
			
			Set<QUADTREESplit> childQuadSet=new TreeSet<SnapshotData.QUADTREESplit>();
			quad.put(first+1, childQuadSet); //nuovo livello di quadtree da popolare
			Set<QUADTREESplit> currentQuadSet=quad.get(first); //set MBR livello 0
			
			
			for(QUADTREESplit currentQuad:currentQuadSet){
				MBR mbr =currentQuad.mbr;
				int beginExampleIndex=currentQuad.beginExampleIndex;
				int endExampleIndex=currentQuad.endExampleIndex;
				Set<Integer> mbrData=currentQuad.mbrData;
				int currentDepth=currentQuad.depth;
				if(mbr.getCardinality()==1)
				{
					/*SensorPoint centre=mbr.determineCentre(this,beginExampleIndex,endExampleIndex);
					centroid.add(centre);
					*/
				
					quad.get(first+1).add(currentQuad);
					currentQuad.children.add(currentQuad);
				}
				else{
					//decompongo i quadrati
					MBR mbr1=null,mbr2=null,mbr3=null,mbr4=null;
					Set<Integer>mbrData1=null,mbrData2=null,mbrData3=null,mbrData4=null;
					boolean q1=false,q2=false,q3=false,q4=false;
					double cX=mbr.getCentreX();
					double cY=mbr.getCentreY();
					//sorting on X
					quicksort(schema.getSpatialList().get(0), beginExampleIndex,endExampleIndex);
					int splitX=endExampleIndex;
					int splitY1=-1,splitY2=-1;
					for(int i=beginExampleIndex;i<=endExampleIndex;i++)
						if(((Double)getSensorPoint(i).getMeasure(0).getValue())>cX){
							splitX=i-1;
							break;
						}
					
					
					//sorting on Y
					if(splitX>=beginExampleIndex){//esistono Q1 e Q3
						quicksort(schema.getSpatialList().get(1), beginExampleIndex, splitX);
						q1=true;
						q3=true;
					
						splitY1=splitX;
					
						for(int i=beginExampleIndex;i<=splitX;i++)
							if(((Double)getSensorPoint(i).getMeasure(1).getValue())>cY){
								splitY1=i-1;
								break;
						}
						if(splitY1==splitX) // non esiste q1, ma un unico quadrante che unisce q1 e q3
						{
							q1=false;
							mbr3=computeMBR(schema,beginExampleIndex,splitY1); //mbr3=new MBR(mbr.getMinX(),mbr.getMinY(),cX,cY,splitY1-beginExampleIndex+1);
							mbrData3=new TreeSet<Integer>();
							for(int i=beginExampleIndex;i<=splitY1;i++)
								mbrData3.add(i);
							
						}
						else if(splitY1==beginExampleIndex-1)// non esiste q3
						{
							q3=false;
							mbr1=computeMBR(schema, splitY1+1, splitX);//mbr1=new MBR(mbr.getMinX(), cY, cX, mbr.getMaxY(), splitX-splitY1+1);
							mbrData1=new TreeSet<Integer>();
							for(int i=splitY1+1;i<=splitX;i++)
								mbrData1.add(i);
							
						}
						else{						
							mbr1=computeMBR(schema, splitY1+1, splitX);//mbr1=new MBR(mbr.getMinX(), cY, cX, mbr.getMaxY(), splitX-splitY1+1);
							mbr3=computeMBR(schema,beginExampleIndex,splitY1); //mbr3=new MBR(mbr.getMinX(),mbr.getMinY(),cX,cY,splitY1-beginExampleIndex+1);
							mbrData1=new TreeSet<Integer>();
							mbrData3=new TreeSet<Integer>();
							for(int i=beginExampleIndex;i<=splitY1;i++)
								mbrData3.add(i);
						
							for(int i=splitY1+1;i<=splitX;i++)
								mbrData1.add(i);
					
						}
					}
					
					if(splitX+1<=endExampleIndex){ //esistono Q2 ae Q4
						q2=true;
						q4=true;
					
						quicksort(schema.getSpatialList().get(1), splitX+1, endExampleIndex);
						splitY2=endExampleIndex;
						for(int i=splitX+1;i<=endExampleIndex;i++){
							if(((Double)getSensorPoint(i).getMeasure(1).getValue())>cY){
								splitY2=i-1;
								break;
							}
						}
						if(splitY2==endExampleIndex) // non esiste q2, ma un unico quadrante che unisce q2 e q4
						{
							q2=false;
							mbr4=computeMBR(schema, splitX+1, splitY2); //mbr4=new MBR(cX, mbr.getMinY(), mbr.getMaxX(), cY, splitY2-splitX+1);
							mbrData4=new TreeSet<Integer>();
							for(int i=splitX+1;i<=splitY2;i++)
								mbrData4.add(i);
						}
						else if(splitY2==splitX) //non esiste q4
						{
							q4=false;
							mbr2=computeMBR(schema, splitY2+1, endExampleIndex);
							mbrData2=new TreeSet<Integer>();
							for(int i=splitY2+1;i<=endExampleIndex;i++)
								mbrData2.add(i);
							
						}
						else{ //esiste q2 e q4
							mbr2=computeMBR(schema, splitY2+1, endExampleIndex);
						//mbr2=new MBR(cX, cY, mbr.getMaxX(), mbr.getMaxY(), endExampleIndex-splitY2+1);
						
							mbr4=computeMBR(schema, splitX+1, splitY2); //mbr4=new MBR(cX, mbr.getMinY(), mbr.getMaxX(), cY, splitY2-splitX+1);
							mbrData2=new TreeSet<Integer>();
							for(int i=splitY2+1;i<=endExampleIndex;i++)
								mbrData2.add(i);
						
							mbrData4=new TreeSet<Integer>();
							for(int i=splitX+1;i<=splitY2;i++)
								mbrData4.add(i);
						}
					}
				
					

				
					for(Integer i:mbrData){
						if(i<beginExampleIndex || i> endExampleIndex)
						//examples not falling in the current leaf (these examples haven't  be sorted again during the mbr decomposition)
						{
							SensorPoint s=data.get(i);
							double sX=(Double)(s.getMeasure(0).getValue());
							double sY=(Double)(s.getMeasure(1).getValue());
							if(q1 && sX>=mbr1.getMinX() && sX<=mbr1.getMaxX() && sY>=mbr1.getMinY() && sY<=mbr1.getMaxY())
								mbrData1.add(i);
							else if(q2 && sX>=mbr2.getMinX() && sX<=mbr2.getMaxX() && sY>=mbr2.getMinY() && sY<=mbr2.getMaxY())
								mbrData2.add(i);
							else if(q3 && sX>=mbr3.getMinX() && sX<=mbr3.getMaxX() && sY>=mbr3.getMinY() && sY<=mbr3.getMaxY())
							mbrData3.add(i);
							else if(q4 && sX>=mbr4.getMinX() && sX<=mbr4.getMaxX() && sY>=mbr4.getMinY() && sY<=mbr4.getMaxY())
								mbrData4.add(i);
							
						}
					}//end for
					
					if(q3){
							QUADTREESplit sp=new QUADTREESplit( mbrData3, beginExampleIndex, splitY1,mbr3,currentDepth+1,currentQuad.id);
							quad.get(first+1).add(sp);
							currentQuad.children.add(sp);
						
					}
					//populateQUADTREE(schema, centroid, mbrData3, beginExampleIndex, splitY1,mbr3,currentDepth+1,maxDepth);
					if(q1){
					
						QUADTREESplit sp=(new QUADTREESplit(mbrData1, splitY1+1, splitX,mbr1,currentDepth+1,currentQuad.id));
						quad.get(first+1).add(sp);
						currentQuad.children.add(sp);
						
					}
					if(q4){
						QUADTREESplit sp=(new QUADTREESplit(mbrData4, splitX+1, splitY2,mbr4,currentDepth+1,currentQuad.id));
						quad.get(first+1).add(sp);
						currentQuad.children.add(sp);
					}
					if(q2)
						{
					
						QUADTREESplit sp=(new QUADTREESplit(mbrData2, splitY2+1, endExampleIndex,mbr2,currentDepth+1,currentQuad.id));
						quad.get(first+1).add(sp);
						currentQuad.children.add(sp);
					}
					
					
						
					
					}
						
				}
			first++;	
		}
			
			
		
		
		//first rappresenta l'ultimo livello costruito
		//devo capire se splittare ancora qualche nodo
		
		
		
		if(quad.get(first).size()==numCentroids){
			for(QUADTREESplit singleQuad:quad.get(first))
				{
					SensorPoint centre=singleQuad.mbr.determineCentre(this,singleQuad.beginExampleIndex,singleQuad.endExampleIndex);
					centroid.add(centre);
				}
		}
		else{ 
			// i centroidi sono tra i livelli first-1 e first in  [first-1] i quadranti sono ordinati per cardinalità
		
			
			int numQuadrantPrevious=quad.get(first-1).size();
			boolean flag=true;
			 for(QUADTREESplit sp:quad.get(first-1)){
				 if(flag){
					 // aggiungo i centroidi dei figli di sp
					for(QUADTREESplit childSp:sp.children){
						SensorPoint centre=childSp.mbr.determineCentre(this,childSp.beginExampleIndex,childSp.endExampleIndex);
						centroid.add(centre);
					}
					 if(centroid.size()+numQuadrantPrevious-1>=numCentroids)
						 flag=false;
					
				 }
				 else{
					 //aggiungo il centroide del nodo corrente
					 SensorPoint centre=sp.mbr.determineCentre(this,sp.beginExampleIndex,sp.endExampleIndex);
						centroid.add(centre);
				 }
				 numQuadrantPrevious--;  // ne ho processato uno				 
			 }
			
			
		}
			
		

	}
		
		
		
				
		
	public List<SensorPoint> quadSampleCentroid(SnapshotSchema schema, int beginExampleIndex, int endExampleIndex, MBR mbr, float perc)
	{
		List<SensorPoint> centroid=new LinkedList<SensorPoint>();

		
		// initialize set of examples spatially contained in the mbr.
		TreeSet<Integer> mbrData=new TreeSet<Integer>();
		for(int i=0;i<data.size();i++){
			SensorPoint s=data.get(i);
			double sX=(Double)(s.getMeasure(0).getValue());
			double sY=(Double)(s.getMeasure(1).getValue());
			if(sX>=mbr.getMinX() && sX<=mbr.getMaxX() && sY>=mbr.getMinY() && sY<=mbr.getMaxY() )
				mbrData.add(i);
		}
		
		
		int numCentroids=(int)((endExampleIndex-beginExampleIndex+1)*perc);
		if(perc<1.0f)
			numCentroids+=1;
		//int depth=(int)(Math.log10(numCentroids)/Math.log10(4));
		//if((float)depth<(Math.log10(numCentroids)/Math.log10(4)))
		//	depth+=1;
		
		//if(depth<=1)
		//	centroid.add(computeCentroid(schema,beginExampleIndex,endExampleIndex));
		//else
			//populateQUADTREE(schema,centroid,mbrData, beginExampleIndex, endExampleIndex, mbr,0,depth);
		Map<Integer,Set<QUADTREESplit>> quadList=new HashMap<Integer, Set<QUADTREESplit>>();
		Set<QUADTREESplit> set=new TreeSet<SnapshotData.QUADTREESplit>();
		set.add(new QUADTREESplit(mbrData, beginExampleIndex, endExampleIndex, mbr, 0,-1));
		quadList.put(0,set);
	
		populateQUADTREE(schema,centroid,quadList,numCentroids);
		
		return centroid;
	}
	public List<SensorPoint> sampleCentroid(SnapshotSchema schema, int beginExampleIndex, int endExampleIndex, float perc)
	{
		List<SensorPoint> centroid=new LinkedList<SensorPoint>();
		int numCentroids=(int)((endExampleIndex-beginExampleIndex+1)*perc);
		if(perc<=1.0f)
			numCentroids+=1;
			
		Date date = new Date();
        long time = date.getTime();
		Random rd=new Random(time);
		
		Set<Integer> centroindIndex=new TreeSet<Integer>();
		while(centroindIndex.size()<numCentroids){
			int position=rd.nextInt(endExampleIndex-beginExampleIndex+1);
			centroindIndex.add(beginExampleIndex+position);
		}
		
		for(Integer i:centroindIndex){
			SensorPoint s=new SensorPoint(-1);
			for(SpatialFeature f:schema.getSpatialList()){
				NumericValue v=(NumericValue)getSensorPoint(i).getMeasure(f.getIndexMining());
				s.addMeasure(v);
				
			}
			
			centroid.add(s);
			
		}
		return centroid;
		
	}
	public MBR computeMBR(SnapshotSchema schema, int beginExampleIndex, int endExampleIndex)
	{
		double minX =(Double)	getSensorPoint(beginExampleIndex).getMeasure(0).getValue();
		double minY = (Double)	getSensorPoint(beginExampleIndex).getMeasure(1).getValue();
		double maxX =(Double)	getSensorPoint(beginExampleIndex).getMeasure(0).getValue();
		double maxY = (Double)	getSensorPoint(beginExampleIndex).getMeasure(1).getValue();
		
		for(int i=beginExampleIndex+1;i<=endExampleIndex;i++){
			double x=(Double)	getSensorPoint(i).getMeasure(0).getValue();
			double y=(Double)	getSensorPoint(i).getMeasure(1).getValue();
			if(x<minX)
				minX=x;
			if(x>maxX)
				maxX=x;
			if(y<minY)
				minY=y;
			if(y>maxY)
				maxY=y;
			
		}
		return new MBR(minX,minY,maxX,maxY,endExampleIndex-beginExampleIndex+1);
	}
	public void updateNull(SnapshotWeigth W,SnapshotSchema schema) {
		 
		// scaledSchema(schema);
		 
		 
		 for(int i=0;i<data.size();i++){
			 
			 for(Feature f:schema.getTargetList()){
				 	//replace value with getis and ord
				 if(f instanceof NumericFeature){
					 
					 if(data.get(i).getMeasure(f.getIndexMining()).isNull()){
						// System.out.println("UPDATE NULL");
						 
						 double sumWijXj=0.0, sumWi=0.0;
						 for(int j=0;j<=data.size()-1;j++)
						{
								
								int id2=getSensorPoint(j).getId();
								
								Value Xj=getSensorPoint(j).getMeasure(f.getIndexMining());
								
							
								if(!Xj.isNull()  )
								{
											
										double wij=W.getWeight(getSensorPoint(i).getId(), id2);
									
										
										
											if(wij!=0){
										
													sumWijXj+=wij*(Double)Xj.getValue();
													//sumWijXj+=wij*scaledXj;
													sumWi+=wij;
													
												}
												
												
									}
							}
							Double idw=0.0;
							if(sumWi!=0)
									idw=new Double(sumWijXj/sumWi);		
							
							data.get(i).getMeasure(f.getIndexMining()).setValue(idw);
							((NumericFeature) f).setMean(idw);
							((NumericFeature) f).setMin(idw);
							((NumericFeature) f).setMax(idw);
							
					 }
				 }
			 }
		 }
//		 System.out.println(this);
		 
		 
	 }
@Override
public Iterator<SensorPoint> iterator() {
	// TODO Auto-generated method stub
	return data.iterator();
}


	

}
