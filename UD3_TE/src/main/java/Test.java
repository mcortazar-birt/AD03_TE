import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class Test {

	private static final String URL =
            "jdbc:mysql://localhost:3306/testDB";
    private static final String USER = "testUSer";
    private static final String PASS = "testPW";
    private static String[] edificios = {"A", "B", "C", "Principal"};
    private static String[] nombres = {"Rojo" ,"Verde", "Azul", "Amarillo", "Naranja", "Morado", "Negro", "Blanco"
    									, "Árbol", "Perro", "Gato", "Flor"};
    private static int id_aula = 0;
    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Conexión válida: " + con.isValid(2));
            
            Statement s = con.createStatement();
            /*StringBuilder insert = new StringBuilder("INSERT INTO aulas (id_aula,nombre,edificio,capacidad) VALUES ");
            Random rnd = new Random();
            for (int i = 0; i < 50; i++) {
            	insert.append(String.format("('%d', '%s', '%s', '%d'),", id_aula++, 
        					  nombres[rnd.nextInt(nombres.length)] + nombres[rnd.nextInt(nombres.length)],
        					  edificios[rnd.nextInt(edificios.length)], rnd.nextInt(45) + 20)
    			);
            }
            insert.replace(insert.length()-1, insert.length(), ";");
            System.out.println(insert.toString());
            s.executeUpdate(insert.toString());
            */
            
            ResultSet rs = s.executeQuery("SELECT * FROM `testdb`.`aulas` WHERE edificio = 'Principal' AND capacidad >=40");
            
            while(rs.next()) {
            	String result = String.format("%s %s %s %s", rs.getString("id_aula"),rs.getString("nombre"),rs.getString("edificio"),
            			rs.getString("capacidad"));
            	System.out.println(result);
            }
            
            
            rs.close();
            s.close();
            
        } catch (SQLException e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
        }
    }

}
