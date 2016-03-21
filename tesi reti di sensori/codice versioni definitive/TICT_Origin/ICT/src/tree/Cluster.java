package tree;

public class Cluster {
	int begin;
	int end;
	double x;
	double y;
	Cluster(int begin,int end){
		this.begin=begin;
			this.end=end;
				
	}
	Cluster(int begin,int end,double x,double y){
		this(begin,end);
		this.x=x;
			this.y=y;
				
	}
	public String toString(){
		return "["+begin+","+end+"]\n";
	}

}
