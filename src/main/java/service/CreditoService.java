package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Credito;
import modelo.CreditoDAO;

/**
 * Service de creditos bancarios, depositos incluidos: la boleta de deposito es una fila de creditos,
 * asi que el mismo circuito cierra 3.8 y 3.9. Las escrituras son duenas de la transaccion
 * (setAutoCommit(false) + commit/rollback), igual que el resto del proyecto. El credito no arrastra
 * ningun efecto colateral: la cuenta bancaria no tiene saldo, el movimiento se cruza recien en la
 * conciliacion (§F del MODULO_TESORERIA_PLAN.md).
 *
 * @author Miguel
 */
public class CreditoService {

    public List<Credito> listarCreditos() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new CreditoDAO(conn).listarCreditos();
        } catch (SQLException e) {
            System.out.println("Error en CreditoService: " + e);
            return null;
        }
    }

    public Credito getCredito(Long idCredito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new CreditoDAO(conn).getCredito(idCredito);
        } catch (SQLException e) {
            System.out.println("Error en CreditoService: " + e);
            return null;
        }
    }

    public Long insertarCredito(Credito credito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            Long id = new CreditoDAO(conn).insertarCredito(credito);
            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CreditoService.insertarCredito: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    public void anularCredito(Long idCredito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CreditoDAO dao = new CreditoDAO(conn);
            Credito credito = dao.getCredito(idCredito);
            if (credito == null) {
                throw new SQLException("El crédito " + idCredito + " no existe");
            }
            if (!dao.anularCredito(idCredito)) {
                throw new SQLException("El crédito ya estaba anulado");
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CreditoService.anularCredito: " + e);
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
