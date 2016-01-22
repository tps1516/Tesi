/*package mbrModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import data.EuclideanDistance;
import data.SensorPoint;

import data.datavalue.Value;
import data.feature.Feature;
import data.feature.NumericFeature;


import snapshot.SnapshotData;
import snapshot.SnapshotSchema;
import snapshot.SnapshotWeigth;


public class KNNModel {
	

	
	class Model implements Comparable<Model>{
		SensorPoint centroid;
		List<Double> model;
		int clusterId;
		double distance=-1.0;
		Model(int clusterId, SensorPoint centroid, List<Double> model){ // come modello uso il proptotipo del cluster
			this.centroid=centroid;
			this.model=model;
			this.clusterId=clusterId;
		}
	
		
		/*Model(int clusterId, SensorPoint centroid,SnapshotData data, SnapshotSchema schema, int begin, int end, SnapshotWeigth W){ // come modello uso le misurazioni del sensore
			this.centroid=centroid;
	
			//this.model=model;
	
			
			if(W!=null)
				model=interpolate(centroid, data, schema, begin, end, W);
			else
				model=interpolate(centroid, data, schema, begin, end);
			
			this.clusterId=clusterId;
		}*/
		/*
		 * interpolatore senza pesi
		 */
		/*private List<Double> interpolate(SensorPoint sp, SnapshotData data, SnapshotSchema schema, int begin, int end){
			
			EuclideanDistance d=new EuclideanDistance(schema.getSpatialList().size());
			List<Double> ls =new LinkedList<Double>();
			for(Feature f:schema.getTargetList()){
				ls.add(new Double(0.0));
			}
			double sumW=0.0;
			for(int i=begin;i<=end;i++){
				SensorPoint neighbor=data.getSensorPoint(i);
				double dist=d.compute(sp, neighbor);	
				
				double weight=1.0;			
				
				
				sumW+=weight;
				int j=0;
				for(Feature f:schema.getTargetList()){
					ls.set(j,ls.get(j)+weight *(Double)(neighbor.getMeasure(f.getIndexMining()).getValue()));
					j++;
				}
				
			}
			int j=0;
			for(Feature f:schema.getTargetList())
			{
				ls.set(j,ls.get(j)/sumW);
				j++;
			}
				
			return ls;
			
		}
		*/
		/*
		 * inteprolatore con pesi
		 */
		/*
		private List<Double> interpolate(SensorPoint sp, SnapshotData data, SnapshotSchema schema, int begin, int end,SnapshotWeigth W){
			
			EuclideanDistance d=new EuclideanDistance(schema.getSpatialList().size());
			List<Double> ls =new LinkedList<Double>();
			for(Feature f:schema.getTargetList()){
				ls.add(new Double(0.0));
			}
			double sumW=0.0;
			for(int i=begin;i<=end;i++){
				SensorPoint neighbor=data.getSensorPoint(i);
				//double dist=d.compute(sp, neighbor);
				double dist=W.getDistance(sp.getId(), neighbor.getId());
			//	if(neighbor.getId()!=sp.getId())
				{
			
					//double dist=W.getDistance(sp.getId(), neighbor.getId()); // controllare
					double peso=W.getWeight(sp.getId(), neighbor.getId());
					
					double weight=1.0;
					if(dist>0)
						weight=1.0/Math.pow(dist, 3);
					
					
					
					sumW+=weight;
					int j=0;
					for(Feature f:schema.getTargetList()){
						ls.set(j,ls.get(j)+weight *(Double)(neighbor.getMeasure(f.getIndexMining()).getValue()));
						j++;
					}
				}
				
			}
			int j=0;
			for(Feature f:schema.getTargetList())
			{
				ls.set(j,ls.get(j)/sumW);
				j++;
			}
				
			return ls;
			
		}
		*/
		/*
		
		public String toString(){
			//return ""+distance;// 
			//return centroid+ "[d=]"+ " "+ model;
			//return model.toString()+"\n";
			return clusterId+","+centroid.getMeasure(0).getValue()+","+centroid.getMeasure(1).getValue()+","+model;
		}

		@Override
		public int compareTo(Model o) {
			// TODO Auto-generated method stub
			if (distance<=o.distance) return -1;
			else return +1;
		}
	}
	
	private List<Model> model=new LinkedList<KNNModel.Model>();
	
	public int size(){
	return model.size();
	}
	public void add(int clusterId, List<SensorPoint> centre, List model){
		

		
		for(SensorPoint s:centre)
			this.model.add(new Model(clusterId, s,model));
	}

	
	/*
	 * Sceglie come prototipi le combinazioni spaziali dei punti nel cluster intorno al centroide selezionato
	 */
	/*public void add(int clusterId, List<SensorPoint> centre,SnapshotData data,int begin,int end, SnapshotSchema schema, SnapshotWeigth W){
		

		
		for(SensorPoint s:centre)
			this.model.add(new Model(clusterId, s,data,schema,begin,end,W));
	}
	*/
	/*
	private 	List<Double> predict(SensorPoint sp, SnapshotSchema schema){
		
		EuclideanDistance d=new EuclideanDistance(schema.getSpatialList().size());
		List<Double> ls =new LinkedList<Double>();
		for(Feature f:schema.getTargetList()){
			ls.add(new Double(0.0));
		}
		double sumW=0.0;
		for(Model m:model){
			double dist=d.compute(sp, m.centroid);
		
			
			double weight=1.0;
			if(dist>0)
				weight=1.0/Math.pow(dist, 3);
			
			sumW+=weight;
			int j=0;
			for(Feature f:schema.getTargetList()){
				ls.set(j,ls.get(j)+weight *m.model.get(j));
				j++;
			}
			
		}
		int j=0;
		for(Feature f:schema.getTargetList())
		{
			ls.set(j,ls.get(j)/sumW);
			j++;
		}
			
		return ls;
		
	}
	
	

public String testKnn(SnapshotData data, SnapshotSchema schema, String outfileName) throws IOException{
		
		List<Double> MSE=new LinkedList<Double>();
		List<Double> SumY2=new LinkedList<Double>();
		List<Double> SumY=new LinkedList<Double>();
		List<Double> MinY=new LinkedList<Double>();
		List<Double> MaxY=new LinkedList<Double>();
	
	//	List<Double> MAE=new LinkedList<Double>();
		FileOutputStream file = new FileOutputStream(outfileName);
      	PrintStream Output = new PrintStream(file);
 //     	Output.println("Learning time:;"+this.getCurrentTimeBegin()+";"+this.getCurrentTimeEnd());
      	String str="";
      	for(Feature f: schema.getTargetList()){
      		if(!str.isEmpty())
      			str+=";";
      		str+=f.getName()+"Pred;"+f.getName();
      		MSE.add(new Double(0.0));
      		SumY.add(new Double(0.0));
      		SumY2.add(new Double(0.0));
    		MinY.add(new Double(Double.POSITIVE_INFINITY));
    		MaxY.add(new Double(Double.NEGATIVE_INFINITY));
    		
 //     		MAE.add(new Double(0.0));
      	}
      	Output.println(str);
      		
      	
      	for(int i=0;i<data.size();i++){
      		//List<Double> out=predict(data.getSensorPoint(i),schema,5);
      		List<Double> out=predict(data.getSensorPoint(i),schema);
      		
      		int index=0;
      		str="";
      		for(Feature f: schema.getTargetList()){
      			f.getIndexMining();
      			Object o=out.get(index);
      			if(!str.isEmpty())
      				str+=";";
      			str+=(o+";"+data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue());
      			int indexFeature=f.getIndexMining()-data.getSpatialFeaturesSize();
      			Value v=data.getSensorPoint(i).getMeasure(f.getIndexMining());
      			if(!v.isNull()){
      				if(o instanceof Double)
      				{
      					double residual=(Double)o-(Double)(data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue());
      					double realValue=(Double)(v.getValue());
      					MSE.set(indexFeature, MSE.get(indexFeature)+Math.pow(residual,2));
      
      					
      					if(MinY.get(indexFeature)>(Double)(data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue()))
      						MinY.set(indexFeature, (Double)(data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue()));
      					
      					

      					if(MaxY.get(indexFeature)<(Double)(data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue()))
      						MaxY.set(indexFeature, (Double)(data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue()));
      					
      					
      					SumY2.set(indexFeature, SumY2.get(indexFeature)+Math.pow(realValue,2));

      					SumY.set(indexFeature, SumY.get(indexFeature)+realValue);
      	             	
      				
             				
      					//					MAE.set(indexFeature, MAE.get(indexFeature)+Math.abs((Double)o-(Double)(data.getSensorPoint(i).getMeasure(f.getIndexMining()).getValue())));
      				}
      				else
      					if(!o.equals((data.getSensorPoint(i).getMeasure(f.getIndexMining()-data.getSpatialFeaturesSize()).getValue())))
      						MSE.set(f.getIndexMining()-data.getSpatialFeaturesSize(), MSE.get(f.getIndexMining()-data.getSpatialFeaturesSize())+1);
      			}
      			index++;
      		}
      		str=str.replace(".", ",");
      		Output.println(str);
      		
      		
      	}
      	String rmseReport="rmse;";
      	String defaultEReport="defaultE;";
      	String rrmseReport="rrmse;";
      	String maxReport="max;";
      	String minReport="min;";
    	String nrmseReport="nrmse;";
    	
     // 	String mae="";
    	
    	
    	double avgRmse=0.0, avgNrmse=0.0, avgRRMSE=0.0;
      	int index=0;
      	int countNoZeroNRMSE=0,countNoZeroRRMSE=0;
    		for(Feature f: schema.getTargetList()){
    		Double mseValue=Math.sqrt(MSE.get(index)/f.getCountTuples());
    		
    		Double RRMSEValue=Math.sqrt(
    				MSE.get(index)/
    				(SumY2.get(index)-(Math.pow(SumY.get(index),2)/f.getCountTuples()))
    				);
    		if(MaxY.get(index)-MinY.get(index)==0)
    			RRMSEValue=Math.sqrt(
        				MSE.get(index)/
        				0
        				);
    			
    		minReport+=MinY.get(index)+";";
    		maxReport+=MaxY.get(index)+";";
    		Double nrmseValue= mseValue/(MaxY.get(index)-MinY.get(index));
    		
    		
    		if(RRMSEValue.equals(Double.NaN))
    			if(!nrmseValue.equals(Double.NaN)) throw new RuntimeException(" Some error occured in the error computation");
    		
    		if(nrmseValue.equals(Double.NaN)) // 0/0
    			nrmseValue=0.0;
    		if(RRMSEValue.equals(Double.NaN))
    			RRMSEValue=0.0;
    		
    		if(Double.isInfinite(nrmseValue)) // x/0
    			nrmseValue=1.0;
    		if(Double.isInfinite(RRMSEValue)) //x/0
    			RRMSEValue=1.0;
    		
    		rmseReport+=mseValue+";";
    		defaultEReport+=Math.sqrt((SumY2.get(index)-(Math.pow(SumY.get(index),2)/f.getCountTuples()))
    				/f.getCountTuples())+";";

    		
    		
    	
    		rrmseReport+=RRMSEValue+";";
    	
    		
    		nrmseReport+=nrmseValue+";";
    		
    		
    		avgRmse+=mseValue;
    		

    		
    		
    		
    		
    	
    		/*
    		if(!nrmseValue.equals(Double.NaN)){
    				avgNrmse+=nrmseValue;
    				countNoZeroNRMSE++;
    		}
    		
    		if(!RRMSEValue.equals(Double.NaN)){
    		    		avgRRMSE+=RRMSEValue;
    		    		countNoZeroRRMSE++;
    		}
    		*/
/*
    		if(!Double.isInfinite(nrmseValue)){
				avgNrmse+=nrmseValue;
				countNoZeroNRMSE++;
    		}
		
    		if(!Double.isInfinite(RRMSEValue)){
		    		avgRRMSE+=RRMSEValue;
		    		countNoZeroRRMSE++;
    		}
    	
    		index++;
       	}
    	
    		avgRmse/=schema.getTargetList().size();
    		avgNrmse/=countNoZeroNRMSE;
    		avgRRMSE/=countNoZeroRRMSE;
    		
    	String avgRmseReport="avgrmse;"+avgRmse;
    	String avgRRMSEReport="avgrrmse;"+avgRRMSE;
    	String avgnrmseReport="avgnrmse;"+avgNrmse;
    	
      	file.close();
     	Output.close();
 
      	return rmseReport+"\n"+defaultEReport+"\n"+	rrmseReport + "\n"+ minReport+ "\n" +maxReport+"\n"+nrmseReport+"\n"+ //rReport+"\n"+
      	avgRmseReport+"\n"+avgRRMSEReport+"\n"+avgnrmseReport+ "\n"; //+avgRReport;
      	

	}

	
	
	public String toString(){
		String str="";
		for(Model m:this.model){
			str+=m+"\n";
		}
		return str;
	}

}*/
