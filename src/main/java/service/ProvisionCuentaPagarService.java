package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.CuentaPagarDAO;
import modelo.FondoFijoRendicionDAO;
import modelo.ProvisionCuentaPagar;
import modelo.ProvisionCuentaPagarDAO;
import modelo.ProvisionCuentaPagarDetalle;

/**
 * Service de provisión de cuenta a pagar. Dueño de la transacción (setAutoCommit(false)).
 * La provisión solo AGRUPA/reserva: al guardarla marca las cuentas como 'En provision'
 * (sin tocar el saldo, que se descuenta en la Orden de Pago). Ver MODULO_TESORERIA_PLAN.md.
 *
 * @author Miguel
 */
public class ProvisionCuentaPagarService {

    public List<ProvisionCuentaPagar> listarProvisiones() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ProvisionCuentaPagarDAO(conn).listarProvisiones();
        } catch (SQLException e) {
            System.out.println("Error en ProvisionCuentaPagarService: " + e);
            return null;
        }
    }

    /**
     * Provisiones disponibles para generar una Orden de Pago (solo las 'Pendiente'; las
     * 'Procesada' ya fueron pagadas por otra OP y las 'Anulado' no sirven).
     */
    public List<ProvisionCuentaPagar> listarProvisionesPendientes() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ProvisionCuentaPagarDAO(conn).listarProvisionesPorEstado("Pendiente");
        } catch (SQLException e) {
            System.out.println("Error en ProvisionCuentaPagarService: " + e);
            return null;
        }
    }

    public ProvisionCuentaPagar getProvision(Long idProvision) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ProvisionCuentaPagarDAO(conn).getProvision(idProvision);
        } catch (SQLException e) {
            System.out.println("Error en ProvisionCuentaPagarService: " + e);
            return null;
        }
    }

    public List<ProvisionCuentaPagarDetalle> listarDetallesPorProvision(Long idProvision) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ProvisionCuentaPagarDAO(conn).listarDetallesPorProvision(idProvision);
        } catch (SQLException e) {
            System.out.println("Error en ProvisionCuentaPagarService: " + e);
            return null;
        }
    }

    /**
     * Guarda la provisión completa en una sola transacción:
     *   1. INSERT cabecera de provisión.
     *   2. INSERT cada detalle (cuenta a pagar + importe a pagar).
     *   3. Reserva cada cuenta a pagar (estado 'En provision', sin tocar el saldo).
     */
    public Long guardarProvisionCompleta(ProvisionCuentaPagar provision,
            List<ProvisionCuentaPagarDetalle> detalles) throws SQLException {

        Connection conn = null;
        Long idProvision = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            ProvisionCuentaPagarDAO dao = new ProvisionCuentaPagarDAO(conn);
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);

            idProvision = dao.insertarProvision(provision);
            for (ProvisionCuentaPagarDetalle det : detalles) {
                dao.insertarDetalle(det, idProvision);
                cuentaPagarDAO.marcarEnProvision(
                    det.getCuentaPagar().getIdCuentaPagar(),
                    det.getCuentaPagar().getFacturaCompra().getIdFacturaCompra());
            }

            // Si la provisión se armó desde una rendición, la rendición queda tomada: el modal de
            // la provisión solo ofrece las 'Generada'. Ver MODULO_TESORERIA_PLAN.md §E.1.
            Long idRendicion = idRendicionDe(provision);
            if (idRendicion != null) {
                FondoFijoRendicionDAO rendicionDAO = new FondoFijoRendicionDAO(conn);
                String estado = rendicionDAO.getEstadoBloqueado(idRendicion);
                if (!FondoFijoRendicionDAO.ESTADO_GENERADA.equals(estado)) {
                    throw new SQLException("La rendición ya no está disponible para provisionar");
                }
                rendicionDAO.actualizarEstado(idRendicion, FondoFijoRendicionDAO.ESTADO_PROVISIONADA);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en guardarProvisionCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return idProvision;
    }

    /**
     * Anula la provisión y revierte la reserva de sus cuentas a pagar (recalcula el estado
     * desde el saldo). Una sola transacción.
     */
    public void anularProvisionCompleta(Long idProvision) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            ProvisionCuentaPagarDAO dao = new ProvisionCuentaPagarDAO(conn);
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);

            ProvisionCuentaPagar provision = dao.getProvision(idProvision);
            Long idRendicion = idRendicionDe(provision);

            List<ProvisionCuentaPagarDetalle> detalles = dao.listarDetallesPorProvision(idProvision);
            dao.anularProvision(idProvision);
            for (ProvisionCuentaPagarDetalle det : detalles) {
                // Si venía de una rendición, la cuenta vuelve a 'Rendida': la factura sigue rendida,
                // lo que se deshizo es la provisión.
                cuentaPagarDAO.revertirProvision(
                    det.getCuentaPagar().getIdCuentaPagar(),
                    det.getCuentaPagar().getFacturaCompra().getIdFacturaCompra(),
                    idRendicion != null);
            }
            if (idRendicion != null) {
                new FondoFijoRendicionDAO(conn).actualizarEstado(
                        idRendicion, FondoFijoRendicionDAO.ESTADO_GENERADA);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en anularProvisionCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /** Id de la rendición que originó la provisión, o null si es una provisión normal. */
    private Long idRendicionDe(ProvisionCuentaPagar provision) {
        if (provision == null || provision.getFondoFijoRendicion() == null) {
            return null;
        }
        return provision.getFondoFijoRendicion().getIdFondoFijoRendicion();
    }
}
