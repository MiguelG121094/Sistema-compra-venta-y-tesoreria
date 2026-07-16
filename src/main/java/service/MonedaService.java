package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Moneda;
import modelo.MonedaDAO;

/**
 *
 * @author Miguel
 */
public class MonedaService {

    public List<Moneda> listarMoneda() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new MonedaDAO(conn).listarMoneda();
        } catch (SQLException e) {
            System.out.println("Error en MonedaService: " + e);
            return null;
        }
    }

    public Moneda getMoneda(Long idMoneda) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new MonedaDAO(conn).getMoneda(idMoneda);
        } catch (SQLException e) {
            System.out.println("Error en MonedaService: " + e);
            return null;
        }
    }
}
