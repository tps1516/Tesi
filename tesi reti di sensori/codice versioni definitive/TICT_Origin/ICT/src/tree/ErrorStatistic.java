package tree;

public class ErrorStatistic{
	double error;
	int countTuples;
	public ErrorStatistic(double error,int countTuples){
		this.error=error;
		this.countTuples=countTuples;
		
	}
	public String toString(){
		return "Error:"+error+" countTuples:"+countTuples;
	}
}