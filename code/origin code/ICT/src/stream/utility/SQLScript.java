package stream.utility;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLScript {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		// TODO Auto-generated method stub
		
	 	FileReader inputFileReader = null;
		BufferedReader inputStream;
		String fileName=args[0];
			
		
		inputFileReader = new FileReader("dataset/"+fileName+".txt");
		inputStream   = new BufferedReader(inputFileReader);
		
		FileOutputStream fileTest = new FileOutputStream("dataset/"+fileName+".sql");
      	PrintStream OutputTest = new PrintStream(fileTest);
      	
      	String createTable="create table berkeley(epoch integer, moteid integer, temperature real, humidity real, light real, voltage real, primary key (epoch,moteid));";
      	OutputTest.println(createTable);
      	DbAccess db= new DbAccess();
      	db.initConnection();
      
      	Statement stat= db.getConnection().createStatement();
    	
      	try{
      		stat.execute("drop table berkeley");
      	}
      	catch(SQLException e){
      		System.out.println(e);
      	}
    	
      	stat.execute(createTable);
      	String line=inputStream.readLine();
      	while(line!=null){
      		String[] split=line.split(" ");
      		
      		String insertTable="insert into berkeley values(";
      		if(split.length<=4){
      			line=inputStream.readLine();
      			continue; // skip the null tuple;
      		}
      		insertTable+=split[2];
      		for(int i=3;i<split.length;i++){
      			if(split[i].isEmpty())
      				split[i]=split[i].replace("", "null");
      			insertTable+=","+split[i];
      		}
      		//for(int i=split.length;i<8;i++)
      		//	insertTable+=",?";
      		insertTable+=");";
      		
      		
      		OutputTest.println(insertTable);
      		System.out.println(insertTable);
      		try{
      			stat.execute(insertTable);
      		}
      		catch(SQLException e){
      			System.out.println(e);
      		}
      		
      		line=inputStream.readLine();
      	}
      	stat.close();
      	db.closeConnection();
      	inputStream.close();
      	OutputTest.close();
      	}
      	
      	

}
