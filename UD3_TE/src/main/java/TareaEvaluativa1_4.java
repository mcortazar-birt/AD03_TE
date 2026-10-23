import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Scanner;

public class TareaEvaluativa1_4 {

	private static final String URL = "jdbc:mysql://localhost:3306/dbformacion";
    private static final String USER = "ADTE";
    private static final String PASS = "ADTE";
	
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
           con.setAutoCommit(false);
			//ejercicio1(con);
			//ejercicio2(con, sc);

        } catch (SQLException e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
        }
		// Ejercicio 3
		try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
	           con.setAutoCommit(false);
				//ejercicio3(con, sc);
        } catch (SQLException e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
        }
		// Ejercicio 4
		try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
	           con.setAutoCommit(false);
				ejercicio4(con, sc);
     } catch (SQLException e) {
         System.err.println("No se pudo conectar: " + e.getMessage());
     }
		sc.close();
	}
	
	public static void ejercicio1(Connection c){

		String query = "SELECT c.id_curso as id, c.titulo as curso, c.fecha, COUNT(m.dni) as matriculas, a.nombre as aula, a.edificio as edificio FROM `dbformacion`.`cursos` c JOIN `dbformacion`.`aulas` a ON c.id_aula = a.id_aula LEFT JOIN `dbformacion`.`matriculas` m ON c.id_curso = m.id_curso GROUP BY (c.id_curso) ORDER BY c.fecha";
		try (Statement statement = c.createStatement();
			ResultSet rs = statement.executeQuery(query)) {
			String formato = "%-4s %-25s %-12s    %-12s %-25s %-12s ";
			String stringCabecera = String.format(
					formato, 
					rs.getMetaData().getColumnLabel(1), rs.getMetaData().getColumnLabel(2),
					rs.getMetaData().getColumnLabel(3), rs.getMetaData().getColumnLabel(4),
					rs.getMetaData().getColumnLabel(5), rs.getMetaData().getColumnLabel(6));
			System.out.println(stringCabecera);
			System.out.println(new String(new char[98]).replace("\0", "-"));
			while(rs.next()) {
				
				String linea = String.format(
						formato, 
						rs.getString(1), rs.getString(2),
						rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6));
				System.out.println(linea);
				
			}
			
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void ejercicio2(Connection c, Scanner sc) {
		
		String dni = getValidDNI(sc);
		if (!isExistingDNI(c, dni)) {
			System.out.println("No existe un alumno con el dni " + dni);
			return;
		}
		
		String query = "SELECT a.nombre FROM alumnos a WHERE a.dni = ?";
		String updateStatementSQL = "UPDATE alumnos SET nombre = ? WHERE dni = ?";
		
		try (PreparedStatement statement = c.prepareStatement(query)) {
			statement.setString(1, dni);
			String nombreNuevo = "";
			boolean actualizar = true;
			// Obtener nombre
			try (ResultSet rs = statement.executeQuery()) {
				
				// En caso de no aparecer, salir
				if (!rs.next()) return;
				
				String nombreActual =  rs.getString(1);
				
				System.out.println("Nombre actual: " + nombreActual);
				System.out.println("Nuevo nombre (deja en blanco para no modificar): ");
				nombreNuevo = sc.nextLine().trim();
				
			}
			
			// Si el nombre nuevo es vacio salir
			if (nombreNuevo.isBlank()|| nombreNuevo.isEmpty()) {
				System.out.println("No se actualiza el nombre.");
				return;
			} 				

			// Actualizar Nombre
			try (PreparedStatement updateStatement = c.prepareStatement(updateStatementSQL)) {
				updateStatement.setString(1, nombreNuevo);
				updateStatement.setString(2, dni);
				int lineas = updateStatement.executeUpdate();
				if (lineas != 1) throw new SQLException("No se ha podido modificar el alumno");
				else System.out.println("Nombre actualizado correctamente.");
			}
			c.commit();
		}catch(SQLException e){
			if (c != null) {
		        try {
		            c.rollback();
		        } catch (SQLException rollbackError) {
		            e.addSuppressed(rollbackError);
		        }
		    }
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void ejercicio3(Connection c, Scanner sc) {
		String dni = getValidDNI(sc);
		// SI el dni no existe, registrar alumno
		if (!isExistingDNI(c, dni)) {
			System.out.println("No existe un alumno con el dni " + dni);
			// Si la matrícula no funciona, rollback en caso de datos 
			// residuales u operaciones no completadas
			if (!registrarAlumno(c, sc, dni)) {
				if (c != null) {
			        try {
			            c.rollback();
			        } catch (SQLException rollbackError) {
			        	System.out.println(rollbackError.getMessage());
			        	return;
			        }
			    }
			}
		}
		
		// Mostrar Listado de Cursos
		mostrarCursos(c);
		
		System.out.println("introduce el ID del curso: ");
		int idCurso = sc.nextInt();
		boolean existeCurso = false, quedanPlazas = false, alumnoMatriculado = false;

		try {			
			// Si no se cumplen las condiciones
			if (!cursoTienePlazas(c, idCurso) || alumnoMatriculado(c, dni, idCurso)) {
				// Rollback
				throw new SQLException();
			}
		} catch(SQLException e) {
			if (c != null) {
		        try {
		            c.rollback();
		        } catch (SQLException rollbackError) {
		        	System.out.println(rollbackError.getMessage());
		        	return;
		        }
		    }
			System.out.println("No se hja podido matricular " + !existeCurso + " " +  !quedanPlazas + " " + alumnoMatriculado);
			return;
		}
		// realizar matricula
		matricularAlumno(c, dni, idCurso);
		
		// Commit
		try {
			c.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Matricula realizada");
		
	}

	public static void ejercicio4(Connection c, Scanner sc) {
		mostrarCursos(c);
		System.out.println("Introduce el ID del curso: ");
		int respuesta = sc.nextInt();
		
		String call = "{ ? = call obtener_numero_matriculados(?)}";
		try (CallableStatement s = c.prepareCall(call)) {
			
			s.registerOutParameter(1, Types.INTEGER);
			s.setInt(2, respuesta);
			s.execute();
			
			int total = s.getInt(1);
			
			System.out.println("Número de alumnos matriculados: " + total);
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		
	}
	
	/*Utilidades*/
	private static boolean cursoTienePlazas(Connection c, int idCurso) throws SQLException {
		String queryExistePlazas = "SELECT c.*, COUNT(m.dni) as matriculas, a.capacidad\r\n"
				+ "FROM cursos c \r\n"
				+ "LEFT JOIN aulas a ON c.id_aula = a.id_aula \r\n"
				+ "LEFT JOIN `dbformacion`.`matriculas` m ON c.id_curso = m.id_curso \r\n"
				+ "WHERE c.id_curso = ?\r\n"
				+ "GROUP BY (c.id_curso)";
		
		try (PreparedStatement ps = c.prepareStatement(queryExistePlazas)) {
			ps.setInt(1, idCurso);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.getInt("capacidad") > rs.getInt("matriculas")) return true;
				else return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("No se ha podido comprobar el estado del curso", e);
		}
	}

	private static boolean alumnoMatriculado(Connection c, String dni, int idCurso) throws SQLException {
		String queryAlumnoMatriculado = "SELECT c.id_curso FROM cursos c \r\n"
				+ "LEFT JOIN matriculas m ON c.id_curso = m.id_curso \r\n"
				+ "WHERE m.dni = ? GROUP BY (c.id_curso)";
		
		try (PreparedStatement ps = c.prepareStatement(queryAlumnoMatriculado)) {
			ps.setString(1, dni);
			try (ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					if (rs.getInt(1) == idCurso) return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("No se ha podido comprobar el estado del alumno", e);
		}
		return false;
	}

	private static boolean matricularAlumno(Connection c, String dni, int idCurso) {
		// Realizar matricula
		String queryMatricular = "INSERT INTO matriculas (dni, id_curso, fecha_matricula) VALUES (?, ?, ?)";
		try (PreparedStatement ps = c.prepareStatement(queryMatricular)) {
			ps.setString(1, dni);
			ps.setInt(2, idCurso);
			ps.setDate(3, new Date(System.currentTimeMillis()));
			
			ps.executeUpdate();
			
			// Si falla hacer rollback
		} catch (SQLException e) {
			e.printStackTrace();
			// Rollback
			if (c != null) {
				try {
					c.rollback();
				} catch (SQLException rollbackError) {
					System.out.println(rollbackError.getMessage());
					return false;
				}
			}
			return false;
		}
		return true;
	}
	
	public static void mostrarCursos(Connection c) {
		
		String query = "SELECT c.*, COUNT(m.dni), a.capacidad \r\n"
				+ "FROM cursos c \r\n"
				+ "LEFT JOIN aulas a ON c.id_aula = a.id_aula \r\n"
				+ "LEFT JOIN `dbformacion`.`matriculas` m ON c.id_curso = m.id_curso \r\n"
				+ "GROUP BY (c.id_curso) ";
		String format = "%d. %-40s plazas libres: %-2d";
		System.out.println("Cursos disponibles:");
		
		try(Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(query)) {
			
			while (rs.next()) {
				System.out.println(
					String.format(format, 
						rs.getInt(1), rs.getString(2), rs.getInt(6) - rs.getInt(5) 
					)
				);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean registrarAlumno(Connection c, Scanner sc, String dni) {
		System.out.println("Por favor, introduce el nombre del alumno, para realizar la matrícula");
		String nombre = sc.nextLine().trim();
		if (nombre.isBlank() || nombre.isEmpty()) return false;
		
		String insertString = "INSERT INTO alumnos (dni, nombre) VALUES (?, ?);";
		try (PreparedStatement ps = c.prepareStatement(insertString)) {
			ps.setString(1, dni);
			ps.setString(2, nombre);
			int result = ps.executeUpdate();
			return result == 1;
		} catch(SQLException e){
			if (c != null) {
		        try {
		            c.rollback();
		        } catch (SQLException rollbackError) {
		            e.addSuppressed(rollbackError);
		        }
		    }
			System.out.println(e.getMessage());
		}
		
		return true;
	}
	
	public static boolean isExistingDNI(Connection c, String dni) {
		String query = "SELECT a.dni FROM alumnos a WHERE a.dni = ?";
		boolean result = false;
		try (PreparedStatement statement = c.prepareStatement(query)) {
			statement.setString(1, dni);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) result = true;			
			}
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
		return result;
	}

	public static String getValidDNI(Scanner sc) {
		String resultado;
		while(true) {
			System.out.println("Introduce el DNI del alumno: ");
			resultado = sc.nextLine().trim();
			if (isValidDNI(resultado)) break;
			System.out.println("El DNI proporcionado no es válido");
		}

		return resultado;
	}
	
	public static boolean isValidDNI(String dni) {
		return dni.matches("\\d{8}[a-zA-Z]");
	}
	
}
