package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.FondoFijo;
import modelo.FondoFijoDAO;

/**
 * Service de fondo fijo (requerimiento 3.5). Las escrituras son duenas de la transaccion
 * (setAutoCommit(false) + commit/rollback), igual que el resto del proyecto.
 *
 * @author Miguel
 */
public class FondoFijoService {

    public List<FondoFijo> listarFondosFijos() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoDAO(conn).listarFondosFijos();
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoService: " + e);
            return null;
        }
    }

    public FondoFijo getFondoFijo(Long idFondoFijo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoDAO(conn).getFondoFijo(idFondoFijo);
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoService: " + e);
            return null;
        }
    }

    public boolean esResponsableDeFondoFijo(Long idProveedor) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoDAO(conn).esResponsableDeFondoFijo(idProveedor);
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoService: " + e);
            return false;
        }
    }

    public void insertarFondoFijo(FondoFijo fondoFijo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new FondoFijoDAO(conn).insertarFondoFijo(fondoFijo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FondoFijoService.insertarFondoFijo: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    public void actualizarFondoFijo(FondoFijo fondoFijo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new FondoFijoDAO(conn).actualizarFondoFijo(fondoFijo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FondoFijoService.actualizarFondoFijo: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    /**
     * Borra el fondo fijo. Se corta antes si ya tiene rendiciones: id_fondo_fijo es FK
     * ON DELETE NO ACTION, asi que el DELETE fallaria igual pero con el error crudo de PostgreSQL.
     */
    public void eliminarFondoFijo(Long idFondoFijo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FondoFijoDAO dao = new FondoFijoDAO(conn);
            long rendiciones = dao.contarRendiciones(idFondoFijo);
            if (rendiciones > 0) {
                throw new SQLException("No se puede eliminar el fondo fijo: tiene " + rendiciones
                        + " rendición(es) cargada(s).");
            }
            dao.eliminarFondoFijo(idFondoFijo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FondoFijoService.eliminarFondoFijo: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    private void cerrar(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
