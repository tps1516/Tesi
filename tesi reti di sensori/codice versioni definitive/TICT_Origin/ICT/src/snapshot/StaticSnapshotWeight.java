package snapshot;

import java.util.GregorianCalendar;

import data.DistanceI;

public class StaticSnapshotWeight extends SnapshotWeigth {
	/**
	 * 
	 */

	private int size;
	
	private Double D[][]; //triangular Superior matrix <idI-idJ, w>
	
	public StaticSnapshotWeight(double b, int size) {
		super(b);
		this.size=size;
		 D=new Double[size][];
		 for(int i=0;i<size;i++) // matrice simmetrica
			 D[i]=new Double[size];
	}

	
	public void updateSnapshotWeigth(SnapshotData data, DistanceI distance) {
		
		

		
		for (int i=0;i<data.size();i++){
	
		
			//System.out.println(i);
			for(int j=i;j<data.size();j++)
			{
				/*if(data.getSensorPoint(i).getId()==758 && data.getSensorPoint(j).getId()==872)
					System.out.print("");*/
				if(data.getSensorPoint(i).getId()!=data.getSensorPoint(j).getId())
				{ 		
						double d=distance.compute(data.getSensorPoint(i), data.getSensorPoint(j));
						if(d<=b)
						{
						
							D[i][j]=new Double(d);
							
						
						}
					
					}
			}
		}
		end=new GregorianCalendar();
		
	}

	
	
	public Double getWeight (Integer id1, Integer id2){
		
		Double w=null;
		if(id1.intValue()==id2.intValue())
			return 0.0;
		else{
			
			try{
			if(id1<id2)
				
				w=(double)D[id1-1][id2-1];
			else
				w=(double)D[id2-1][id1-1];
			}
			catch(NullPointerException e)
			{
				return 0.0;
			}
			if(w==null) w=b; // approximate the distance with the boundary (out of boundary)
			if(w.equals(0.0))
				return 1.0;
			else
				return 1.0/Math.pow(w, 3);
			
		
		}
			
	}
	
	public Double getDistance (Integer id1, Integer id2){
		Double d;
		if(id1.intValue()==id2.intValue())
			return 0.0;
		else{
			
			if(id1<id2)
				d=D[id1-1][id2-1];
			else
				d=D[id2-1][id1-1];
				
			if(d==null) return b;
			else 
				return d;
		}
		
			
	}
	

	public String toString(){
		String str="Weight Matrix\n";
		for(int i=0;i<D.length;i++){
			str+=(i+":"+D[i].length+",");
			for(int k=0;k<D[i].length;k++)
				
				str+=(getWeight(i, k)+",");
				
			
			str+="\n";
		}
		
		return str;
		}
					
	
}
