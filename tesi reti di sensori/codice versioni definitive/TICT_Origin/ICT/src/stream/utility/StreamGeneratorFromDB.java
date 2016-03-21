package stream.utility;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StreamGeneratorFromDB {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		//2-65535
		
		//voltage 0-3.5
		//light
		DbAccess db=new DbAccess();
		db.initConnection();
		

		FileOutputStream fileTest = new FileOutputStream("berkeleyC.arff");
      	PrintStream OutputTest = new PrintStream(fileTest);
      	
      	Statement st=db.getConnection().createStatement();
      	ResultSet rs=st.executeQuery("select epoch,moteid,x,y,temperature,humidity,light,voltage from berkeley where epoch>=2  order by epoch,moteid;" );

      	OutputTest.println("@");
      	int epochLast=2;
      	while(rs.next()){
      		int epoch=rs.getInt("epoch");
      		int moteid=rs.getInt("moteid");
      		float x=rs.getFloat("x");
      		float y=rs.getFloat("y");
      		
      		Object temperature=rs.getObject("temperature");
      		Object humidity=rs.getObject("humidity");
      		Object light=rs.getObject("light");
      		Object voltage=rs.getObject("voltage");
      		if(epoch!=epochLast){
      			OutputTest.println("@");
      			epochLast=epoch;
      		}
      		if(temperature!=null)
      			if((Double)temperature<=0 ||(Double)temperature>=100)
      				temperature=null;
      		if(humidity!=null)
      			if((Double)humidity<=0 ||(Double)humidity>=100)
      				humidity=null;
      				
      		String str=moteid+","+x+","+y+","+temperature+","+humidity+","+light+","+voltage;
      		str=str.replace("null", "?");
      		OutputTest.println(str);
      		
      	}
      	OutputTest.println("@");
      	rs.close();
      	st.close();
      	
      	
      	OutputTest.close();
      	fileTest.close();
      	
		
		db.closeConnection();

	}

}
