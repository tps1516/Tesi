/*package mbrModel;

import java.io.Serializable;

import snapshot.SnapshotData;

import data.SensorPoint;

public class MBR implements Serializable{
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private int cardinality;
	
	public MBR(double minX, double minY, double maxX, double maxY,int cardinality){
		this.minX=(minX);
		this.minY=(minY);
		this.maxX=(maxX);
		this.maxY=(maxY);
		this.cardinality=cardinality;
		
	}

	

	public double getMinX() {
		return minX;
	}


	public double getMinY() {
		return minY;
	}


	public double getMaxX() {
		return maxX;
	}



	public double getMaxY() {
		return maxY;
	}



	public int getCardinality() {
		return cardinality;
	}

	public double getCentreX(){
		return (minX+maxX)/2;
	}
	public double getCentreY(){
		return (minY+maxY)/2;
	}
	public SensorPoint determineCentre(SnapshotData data, int beginExampleIndex, int endExampleIndex){
		//the sensor point closest to the geometrical centre of the mbr
		
		SensorPoint s=data.getSensorPoint(beginExampleIndex);
		double cX=(minX+maxX)/2;
		double cY=(minY+maxY)/2;
	
		
		double minDist=Math.pow(cX-(Double)s.getMeasure(0).getValue(), 2)+Math.pow(cY-(Double)s.getMeasure(1).getValue(), 2);
	
		for(int i=beginExampleIndex+1;i<=endExampleIndex;i++){
			double d=Math.pow(cX-(Double)data.getSensorPoint(i).getMeasure(0).getValue(), 2)+Math.pow(cY-(Double)data.getSensorPoint(i).getMeasure(1).getValue(), 2);
			if(d<minDist){
				minDist=d;
				s=data.getSensorPoint(i);
			}
			
		}
		
		return s;
	}



}
*/