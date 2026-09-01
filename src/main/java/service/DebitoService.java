package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Debito;
import modelo.DebitoDAO;

/**
 * Service de debitos bancarios. Las escrituras son duenas de la transaccion
 * (setAutoCommit(false) + commit/rollback), igual que el resto del proyecto. El debito no arrastra
 * ningun efecto colateral: la cuenta bancaria no tiene saldo, el movimiento se cruza recien en la
 * conciliacion (§F del MODULO_TESORERIA_PLAN.md).
 *
 * @author Miguel
 */
public class DebitoService {

    public List<Debito> listarDebitos() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new DebitoDAO(conn).listarDebitos();
        } catch (SQLException e) {
            System.out.println("Error en DebitoService: " + e);
            return null;
        }
    }

    public Debito getDebito(Long idDebito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new DebitoDAO(conn).getDebito(idDebito);
        } catch (SQLException e) {
            System.out.println("Error en DebitoService: " + e);
            return null;
        }
    }

    public long proximoNumero() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new DebitoDAO(conn).proximoNumero();
        }
    }

    public Long insertarDebito(Debito debito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            Long id = new DebitoDAO(conn).insertarDebito(debito);
            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en DebitoService.insertarDebito: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    public void anularDebito(Long idDebito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            DebitoDAO dao = new DebitoDAO(conn);
            Debito debito = dao.getDebito(idDebito);
            if (debito == null) {
                throw new SQLException("El débito " + idDebito + " no existe");
            }
            if (!dao.anularDebito(idDebito)) {
                throw new SQLException("El débito ya estaba anulado");
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en DebitoService.anularDebito: " + e);
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
