package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.EntidadFinanciera;
import modelo.EntidadFinancieraDAO;

/**
 *
 * @author Miguel
 */
public class EntidadFinancieraService {

    public List<EntidadFinanciera> listarEntidadFinanciera() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new EntidadFinancieraDAO(conn).listarEntidadFinanciera();
        } catch (SQLException e) {
            System.out.println("Error en EntidadFinancieraService: " + e);
            return null;
        }
    }

    public EntidadFinanciera getEntidadFinanciera(Long idEntidadFinanciera) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new EntidadFinancieraDAO(conn).getEntidadFinanciera(idEntidadFinanciera);
        } catch (SQLException e) {
            System.out.println("Error en EntidadFinancieraService: " + e);
            return null;
        }
    }
}
