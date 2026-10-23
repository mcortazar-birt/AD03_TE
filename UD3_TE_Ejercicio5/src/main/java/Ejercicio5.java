import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.vibur.dbcp.ViburDBCPDataSource;


public class Ejercicio5 {

	public static void main(String[] args) {
		
		ViburDBCPDataSource ds = new ViburDBCPDataSource();
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setJdbcUrl("jdbc:mysql://localhost:3306/dbformacion");
		ds.setUsername("ADTE");
		ds.setPassword("ADTE");
		ds.setPoolInitialSize(2);
		ds.setPoolMaxSize(8);
		ds.start();
		
		
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(false);
			System.out.println(c.isValid(2));
			
			try (Statement s = c.createStatement()) {
				s.execute("CREATE TABLE comprobacion( "
						+ " id INT PRIMARY KEY,"
						+ " mensaje VARCHAR(40));");
				s.executeUpdate("INSERT INTO comprobacion (id, mensaje) VALUES (1, 'Mensaje de prueba')");
			}catch(SQLException e) {
				e.printStackTrace();
				System.out.println("Probablemente ya existe la tabla");
			}
			
			try (Statement s = c.createStatement();
				ResultSet result = s.executeQuery("SELECT * FROM comprobacion;")) {
				while(result.next()) {
					System.out.println("Resultado: " + result.getInt(1) + ", " + result.getString(2));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
