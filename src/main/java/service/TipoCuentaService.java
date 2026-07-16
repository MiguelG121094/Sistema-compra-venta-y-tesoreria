package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.TipoCuenta;
import modelo.TipoCuentaDAO;

/**
 *
 * @author Miguel
 */
public class TipoCuentaService {

    public List<TipoCuenta> listarTipoCuenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new TipoCuentaDAO(conn).listarTipoCuenta();
        } catch (SQLException e) {
            System.out.println("Error en TipoCuentaService: " + e);
            return null;
        }
    }

    public TipoCuenta getTipoCuenta(Long idTipoCuenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new TipoCuentaDAO(conn).getTipoCuenta(idTipoCuenta);
        } catch (SQLException e) {
            System.out.println("Error en TipoCuentaService: " + e);
            return null;
        }
    }
}
