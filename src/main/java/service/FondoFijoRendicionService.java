package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.CuentaPagar;
import modelo.CuentaPagarDAO;
import modelo.FondoFijoRendicion;
import modelo.FondoFijoRendicionDAO;
import modelo.FondoFijoRendicionDetalle;

/**
 * Service de la rendicion de fondo fijo (requerimiento 3.6, §E del MODULO_TESORERIA_PLAN.md).
 *
 * <p>Es dueno de la transaccion: guarda la cabecera, su detalle y marca las cuentas a pagar en una
 * sola unidad. La rendicion <b>no toca el saldo</b> de las cuentas: solo agrupa las facturas del
 * fondo fijo y las marca como 'Rendida' para que no entren en dos rendiciones. El pago lo sigue
 * haciendo el circuito provision → orden de pago, que ve las rendidas porque filtra por exclusion.
 *
 * @author Miguel
 */
public class FondoFijoRendicionService {

    public List<FondoFijoRendicion> listarRendiciones() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoRendicionDAO(conn).listarRendiciones();
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoRendicionService: " + e);
            return null;
        }
    }

    public FondoFijoRendicion getRendicion(Long idRendicion) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoRendicionDAO(conn).getRendicion(idRendicion);
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoRendicionService: " + e);
            return null;
        }
    }

    public List<FondoFijoRendicionDetalle> listarDetalles(Long idRendicion) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoRendicionDAO(conn).listarDetallesPorRendicion(idRendicion);
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoRendicionService: " + e);
            return null;
        }
    }

    public long obtenerProximoNumero() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FondoFijoRendicionDAO(conn).obtenerProximoNumero();
        }
    }

    /** Cuentas a pagar de facturas de fondo fijo disponibles para rendir. */
    public List<CuentaPagar> listarCuentasPagarFondoFijo() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new CuentaPagarDAO(conn).listarCuentasPagarFondoFijo();
        } catch (SQLException e) {
            System.out.println("Error en FondoFijoRendicionService: " + e);
            return null;
        }
    }

    /**
     * Guarda la rendicion completa: cabecera, detalle y la marca de cada cuenta a pagar, todo en
     * una transaccion. Si una factura ya fue rendida por otra rendicion, marcarRendida corta y se
     * deshace todo.
     */
    public Long guardarRendicionCompleta(FondoFijoRendicion rendicion,
            List<FondoFijoRendicionDetalle> detalles) throws SQLException {

        if (rendicion == null || rendicion.getFondoFijo() == null
                || rendicion.getFondoFijo().getIdFondoFijo() == null) {
            throw new SQLException("Debe seleccionar el responsable del fondo fijo");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new SQLException("La rendición no tiene facturas cargadas");
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            FondoFijoRendicionDAO rendicionDAO = new FondoFijoRendicionDAO(conn);
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);

            Long idRendicion = rendicionDAO.insertarRendicion(rendicion);

            for (FondoFijoRendicionDetalle detalle : detalles) {
                CuentaPagar cuenta = detalle.getCuentaPagar();
                if (cuenta == null || cuenta.getIdCuentaPagar() == null
                        || cuenta.getFacturaCompra() == null) {
                    throw new SQLException("Hay una línea sin cuenta a pagar válida");
                }
                rendicionDAO.insertarDetalle(detalle, idRendicion);
                cuentaPagarDAO.marcarRendida(cuenta.getIdCuentaPagar(),
                        cuenta.getFacturaCompra().getIdFacturaCompra());
            }

            conn.commit();
            return idRendicion;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FondoFijoRendicionService.guardarRendicionCompleta: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    /**
     * Anula la rendicion y devuelve sus cuentas a pagar al estado que les corresponde por saldo,
     * para que puedan volver a rendirse. La fila no se borra: el detalle queda como trazabilidad.
     */
    public void anularRendicionCompleta(Long idRendicion) throws SQLException {
        if (idRendicion == null) {
            throw new SQLException("Debe indicar la rendición a anular");
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            FondoFijoRendicionDAO rendicionDAO = new FondoFijoRendicionDAO(conn);
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);

            String estado = rendicionDAO.getEstadoBloqueado(idRendicion);
            if (estado == null) {
                throw new SQLException("La rendición " + idRendicion + " no existe");
            }
            if (FondoFijoRendicionDAO.ESTADO_ANULADO.equals(estado)) {
                throw new SQLException("La rendición ya estaba anulada");
            }

            for (FondoFijoRendicionDetalle detalle : rendicionDAO.listarDetallesPorRendicion(idRendicion)) {
                CuentaPagar cuenta = detalle.getCuentaPagar();
                if (cuenta != null && cuenta.getFacturaCompra() != null) {
                    cuentaPagarDAO.revertirRendicion(cuenta.getIdCuentaPagar(),
                            cuenta.getFacturaCompra().getIdFacturaCompra());
                }
            }
            rendicionDAO.actualizarEstado(idRendicion, FondoFijoRendicionDAO.ESTADO_ANULADO);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FondoFijoRendicionService.anularRendicionCompleta: " + e);
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
