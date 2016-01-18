package data.feature;

import data.datavalue.Value;
import snapshot.SnapshotData;
import snapshot.SnapshotWeigth;

public class GetisOrdGeneralG //implements AutocorrelationI 
{


	public boolean isMax(double v) {
		// TODO Auto-generated method stub
		return v==Double.NaN || v==Double.POSITIVE_INFINITY || v==Double.MAX_VALUE;
	}

	public Double compute(SnapshotData data, Feature feature, SnapshotWeigth W,int beginIndex, int endIndex){		
		NumericFeature f=(NumericFeature)feature;
		int featureIndex=f.getIndexMining();
		double sumWXX=0.0;
		double sumXX=0.0;		
		int n=0;
		double sumW=0.0;
		double S1=0,S2=0;
		
		double sumX=0.0,sumX2=0.0,sumX3=0.0,sumX4=0.0;
		for(int i=beginIndex;i<=endIndex;i++)
		{
			int id1=data.getSensorPoint(i).getId();
			Value Xi=data.getSensorPoint(i).getMeasure(featureIndex);
			double S2iLeft=0.0;
			double S2iRight=0.0;
			if(!Xi.isNull()){
				n++;
			//	System.out.print(Xi+ " ");
				double xi=(Double)Xi.getValue();
				sumX+=xi;
				sumX2+=Math.pow(xi,2);
				sumX3+=Math.pow(xi,3);
				sumX4+=Math.pow(xi,4);
				for(int j=beginIndex;j<=endIndex;j++)
				{
					int id2=data.getSensorPoint(j).getId();
					Value Xj=data.getSensorPoint(j).getMeasure(featureIndex);
					if(!Xj.isNull()&& id1!=id2)
					{						
						double wij=W.getWeight(id1, id2);
						sumXX+=(Double)Xi.getValue()*(Double)Xj.getValue();
						sumWXX+=wij*(Double)Xi.getValue()*(Double)Xj.getValue();
						sumW+=wij;
						
						double wji=W.getWeight(id2, id1);
						S1+=Math.pow(wij+wji, 2);
						S2iLeft+=wij;
						S2iRight+=wji;
						
					}
				}// end for j
				
			}
			S2+= (Math.pow (S2iLeft+S2iRight,2));
		}
		//System.out.println(" ");
		double G=sumWXX/sumXX;
		double EG=sumW/(n*(n-1));
		S1=0.5*S1;
		
		double D0=(Math.pow(n,2)-3*n+3)*S1-n*S2+3*Math.pow(sumW,2);
		double D1= -( (Math.pow(n, 2)-n)*S1-2*n*S2+6*Math.pow(sumW,2) );
		double D2=-(2*n*S1-(n+3)*S2+6*Math.pow(sumW,2));
		double D3=4*(n-1)*S1-2*(n+1)*S2+8*Math.pow(sumW,2);
		double D4=S1-S2+Math.pow(sumW,2);
		
		
		double A=D0*Math.pow(sumX2, 2)+D1*sumX4+D2*Math.pow(sumX, 2)*sumX2;
		double B=D3*sumX*sumX3+D4*Math.pow(sumX, 4);
		double C= Math.pow((Math.pow(sumX,2)-sumX2), 2)*n*(n-1)*(n-2)*(n-3);
		
		double EG2=(A+B)/C;
		double VG=EG2-Math.pow(EG, 2);
		double globalG= Double.MAX_VALUE;
		if(VG>0)
			globalG=Math.abs((G-EG)/Math.sqrt(VG));
	//	System.out.println(globalG);
		return globalG;
	}
	

}
