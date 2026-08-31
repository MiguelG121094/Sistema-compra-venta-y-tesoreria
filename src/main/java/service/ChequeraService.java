package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Chequera;
import modelo.ChequeraDAO;

/**
 * Service de chequeras: ABM del modulo Tesoreria y combo de la linea de cheque en la Orden de Pago.
 * Las escrituras son duenas de la transaccion (setAutoCommit(false) + commit/rollback) y validan
 * dentro de ella, para que el control y el guardado vean el mismo estado. La emision del cheque real
 * (que consume el proximo numero del rango) la hace OrdenPagoService en su propia transaccion.
 *
 * @author Miguel
 */
public class ChequeraService {

    public List<Chequera> listarChequeras() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ChequeraDAO(conn).listarChequeras();
        } catch (SQLException e) {
            System.out.println("Error en ChequeraService: " + e);
            return null;
        }
    }

    public Chequera getChequera(Long idChequera) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ChequeraDAO(conn).getChequera(idChequera);
        } catch (SQLException e) {
            System.out.println("Error en ChequeraService: " + e);
            return null;
        }
    }

    public void insertarChequera(Chequera chequera) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            ChequeraDAO dao = new ChequeraDAO(conn);
            validarSolapamiento(dao, chequera, null);
            dao.insertarChequera(chequera);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ChequeraService.insertarChequera: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    public void actualizarChequera(Chequera chequera) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            ChequeraDAO dao = new ChequeraDAO(conn);
            validarSolapamiento(dao, chequera, chequera.getIdChequera());
            validarRangoContraEmitidos(dao, chequera);
            dao.actualizarChequera(chequera);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ChequeraService.actualizarChequera: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    /**
     * Borra la chequera. Se corta antes si ya emitio cheques: id_chequera es FK ON DELETE NO ACTION,
     * asi que el DELETE fallaria igual, pero con el error crudo de PostgreSQL.
     */
    public void eliminarChequera(Long idChequera) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            ChequeraDAO dao = new ChequeraDAO(conn);
            long emitidos = dao.contarCheques(idChequera);
            if (emitidos > 0) {
                throw new SQLException("No se puede eliminar la chequera: tiene " + emitidos
                        + " cheque(s) emitido(s). Los cheques quedarian sin chequera.");
            }
            dao.eliminarChequera(idChequera);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ChequeraService.eliminarChequera: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * Dos chequeras de la misma cuenta con rangos que se pisan emiten el mismo numero de cheque dos
     * veces, porque el proximo numero se calcula por chequera y no por cuenta.
     */
    private void validarSolapamiento(ChequeraDAO dao, Chequera chequera, Long idExcluir) throws SQLException {
        if (dao.haySolapamiento(chequera.getCuenta().getIdCuenta(),
                chequera.getDesdeNumero(), chequera.getHastaNumero(), idExcluir)) {
            throw new SQLException("El rango " + chequera.getDesdeNumero() + "-" + chequera.getHastaNumero()
                    + " se pisa con otra chequera de la misma cuenta.");
        }
    }

    /**
     * El rango no puede achicarse por debajo de lo que ya se emitio: esos cheques quedarian fuera de
     * su propia chequera y el proximo numero saldria mal.
     */
    private void validarRangoContraEmitidos(ChequeraDAO dao, Chequera chequera) throws SQLException {
        long[] emitido = dao.rangoEmitido(chequera.getIdChequera());
        if (emitido == null) {
            return;
        }
        if (chequera.getDesdeNumero() > emitido[0] || chequera.getHastaNumero() < emitido[1]) {
            throw new SQLException("La chequera ya emitio cheques del " + emitido[0] + " al " + emitido[1]
                    + ": el rango nuevo tiene que contenerlos.");
        }
    }

    private void cerrar(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
