package stream.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



/**
 * Gestisce l'accesso al DB per la lettura dei dati di training
 * @author Map Tutor
 *
 */
public class DbAccess {

	private  final String DRIVER_CLASS_NAME = "org.gjt.mm.mysql.Driver";
	private  final String DBMS = "jdbc:mysql";
	private  final String SERVER = "localhost";
	private  final int PORT = 3306;
	private  final String DATABASE = "MapDB";
	private  final String USER_ID = "MapUser";
	private  final String PASSWORD = "map";

	private  Connection conn;

	/**
	 * Inizializza una connessione al DB
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 */
	public  void initConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String connectionString = DBMS+"://" + SERVER + ":" + PORT + "/" + DATABASE;
		
			
				Class.forName(DRIVER_CLASS_NAME).newInstance();
		
			conn = DriverManager.getConnection(connectionString, USER_ID, PASSWORD);
			
		
	}
	public Connection getConnection(){
		return conn;
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println("Impossibile chiudere la connessione");
		}
	}

}
