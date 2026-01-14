/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Miguel
 */
public class Conexion {

    private static final String URL = "jdbc:postgresql://localhost:5432/taller_3ro_compras_tesoreria";
    private static final String USUARIO = "postgres";
    private static final String CONTRASEÑA = "adolfo94";

    //manejo manual de la conexion a la base de dtos
//    public static Connection getConnection() {
//        Connection conn = null;
//        try {
//            Class.forName("org.postgresql.Driver");
//            conn = DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
//            if (conn != null) {
//                System.out.println("Conexión exitosa");
//            } else {
//                System.out.println("Fallo en la conexion");
//            }
//            return conn;
//        }
//        catch (ClassNotFoundException e) {
//            System.out.println("Error: No se encontró el driver de PostgreSQL.");
//            e.printStackTrace();
//            return null;
//        } catch (SQLException e) {
//            System.out.println("Error de SQL: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
    
    // manejo de la conexion a la base de datos con pool de conexiones que se administra desde la consola del glassfish en http://localhost:4848/
    private static DataSource dataSource;

    static {
        try {
            Context initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup("jdbc/MiDataSource"); //nombre del datasorse creado en la consola del srvidor glassfish
        } catch (NamingException e) {
            throw new RuntimeException("Error al inicializar el pool de conexiones y Error al obtener el DataSource", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
