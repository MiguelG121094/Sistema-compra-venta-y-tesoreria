package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Cuenta;
import modelo.CuentaDAO;

/**
 * Service de cuentas bancarias. Las escrituras son dueñas de la transacción
 * (setAutoCommit(false) + commit/rollback), igual que el resto del proyecto.
 *
 * @author Miguel
 */
public class CuentaService {

    public List<Cuenta> listarCuenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new CuentaDAO(conn).listarCuenta();
        } catch (SQLException e) {
            System.out.println("Error en CuentaService: " + e);
            return null;
        }
    }

    public Cuenta getCuenta(Long idCuenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new CuentaDAO(conn).getCuenta(idCuenta);
        } catch (SQLException e) {
            System.out.println("Error en CuentaService: " + e);
            return null;
        }
    }

    public void insertarCuenta(Cuenta cuenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new CuentaDAO(conn).insertarCuenta(cuenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaService.insertarCuenta: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void actualizarCuenta(Cuenta cuenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new CuentaDAO(conn).actualizarCuenta(cuenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaService.actualizarCuenta: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void eliminarCuenta(Long idCuenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new CuentaDAO(conn).eliminarCuenta(idCuenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaService.eliminarCuenta: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
