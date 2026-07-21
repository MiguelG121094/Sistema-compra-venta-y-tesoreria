package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Cheque;
import modelo.CuentaPagarDAO;
import modelo.FormaPagoDetalle;
import modelo.FormaPagoDetalleDAO;
import modelo.OrdenPago;
import modelo.OrdenPagoDAO;
import modelo.OrdenPagoDetalle;
import modelo.OrdenPagoDetalleDAO;
import modelo.ChequeDAO;
import modelo.ChequeraDAO;

/**
 * Service de Orden de Pago. Dueño de la transacción (setAutoCommit(false)), calcado del patrón
 * de ProvisionCuentaPagarService. Genera la OP en UNA transacción con 5 pasos:
 *   1. INSERT orden_pago_cabecera.
 *   2. INSERT orden_pago_detalle (N facturas de la provisión, SOLO LECTURA).
 *   3. INSERT forma_pago_detalle (N formas: transferencia + cheque(s)); por cada línea de
 *      cheque se emite un cheque real de la chequera (nro dentro del rango).
 *   4. Descuenta cta_pag_saldo de cada factura + recalcula el estado.
 * Ver MODULO_TESORERIA_PLAN.md §C.
 *
 * @author Miguel
 */
public class OrdenPagoService {

    private static final String ESTADO_CHEQUE_EMITIDO = "Emitido";

    public List<OrdenPago> listarOrdenesPago() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new OrdenPagoDAO(conn).listarOrdenesPago();
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService.listarOrdenesPago: " + e);
            return null;
        }
    }

    public OrdenPago getOrdenPago(Long idOrdenPago) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new OrdenPagoDAO(conn).getOrdenPago(idOrdenPago);
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService.getOrdenPago: " + e);
            return null;
        }
    }

    public List<OrdenPagoDetalle> listarDetallePorOrden(Long idOrdenPago) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new OrdenPagoDetalleDAO(conn).listarPorOrden(idOrdenPago);
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService.listarDetallePorOrden: " + e);
            return null;
        }
    }

    public List<FormaPagoDetalle> listarFormasPagoPorOrden(Long idOrdenPago) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new FormaPagoDetalleDAO(conn).listarPorOrden(idOrdenPago);
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService.listarFormasPagoPorOrden: " + e);
            return null;
        }
    }

    /**
     * Genera la Orden de Pago completa en una sola transacción (los 5 pasos). Valida antes de
     * abrir la transacción; si cualquier paso falla se hace rollback total.
     */
    public Long guardarOrdenPagoCompleta(OrdenPago orden, List<OrdenPagoDetalle> detalles,
            List<FormaPagoDetalle> formasPago) throws SQLException {

        validar(orden, detalles, formasPago);

        Connection conn = null;
        Long idOrden = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            OrdenPagoDAO ordenDAO = new OrdenPagoDAO(conn);
            OrdenPagoDetalleDAO detalleDAO = new OrdenPagoDetalleDAO(conn);
            FormaPagoDetalleDAO formaDAO = new FormaPagoDetalleDAO(conn);
            ChequeDAO chequeDAO = new ChequeDAO(conn);
            ChequeraDAO chequeraDAO = new ChequeraDAO(conn);
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);

            // 1. Cabecera
            idOrden = ordenDAO.insertarOrdenPago(orden);

            // 2. Detalle (facturas de la provisión, solo lectura)
            for (OrdenPagoDetalle det : detalles) {
                detalleDAO.insertarDetalle(det, idOrden);
            }

            // 3. Formas de pago (+ emisión de cheque real por cada línea de cheque)
            for (FormaPagoDetalle fp : formasPago) {
                fp.setOrdenPago(orden);
                Cheque cheque = fp.getCheque();
                if (cheque != null) {
                    long proximoNro = chequeraDAO.proximoNumeroCheque(cheque.getChequera().getIdChequera());
                    cheque.setNumero(proximoNro);
                    if (cheque.getEstado() == null) {
                        cheque.setEstado(ESTADO_CHEQUE_EMITIDO);
                    }
                    if (cheque.getFechaEmision() == null) {
                        cheque.setFechaEmision(orden.getFechaEmision());
                    }
                    if (cheque.getaLaOrden() == null) {
                        cheque.setaLaOrden(orden.getProveedor().getRazonSocial());
                    }
                    if (cheque.getObservacion() == null) {
                        cheque.setObservacion("Orden de pago Nro " + orden.getNumero());
                    }
                    if (cheque.getFechaPago() == null) {
                        cheque.setFechaPago(orden.getFechaEmision());
                    }
                    if (cheque.getFechaVencimiento() == null) {
                        cheque.setFechaVencimiento(orden.getFechaEmision());
                    }
                    chequeDAO.insertarCheque(cheque); // asigna idCheque
                    if (fp.getReferencia() == null) {
                        fp.setReferencia(String.valueOf(proximoNro));
                    }
                }
                if (fp.getFecha() == null) {
                    fp.setFecha(orden.getFechaEmision());
                }
                formaDAO.insertarFormaPago(fp, idOrden);
            }

            // 4. Descontar el saldo de cada factura de la provisión + recalcular estado
            for (OrdenPagoDetalle det : detalles) {
                cuentaPagarDAO.descontarSaldo(
                    det.getCuentaPagar().getIdCuentaPagar(),
                    det.getCuentaPagar().getFacturaCompra().getIdFacturaCompra(),
                    det.getMonto());
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en guardarOrdenPagoCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return idOrden;
    }

    /**
     * Validaciones de negocio antes de generar (fuera de la transacción):
     * provisión previa obligatoria, hay facturas y formas de pago, cada forma con monto &gt; 0,
     * y Σ de las formas de pago == monto de la OP. El "sin efectivo" lo garantiza el combo de
     * forma_pago_cabecera (solo cheque/transferencia).
     */
    private void validar(OrdenPago orden, List<OrdenPagoDetalle> detalles,
            List<FormaPagoDetalle> formasPago) throws SQLException {
        if (orden == null) {
            throw new SQLException("La orden de pago es nula");
        }
        if (orden.getIdProvisionCtaPagar() == null) {
            throw new SQLException("No se puede generar una orden de pago sin una provisión previa");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new SQLException("La orden de pago no tiene facturas (detalle vacío)");
        }
        if (formasPago == null || formasPago.isEmpty()) {
            throw new SQLException("La orden de pago no tiene formas de pago");
        }
        long sumaFormas = 0;
        for (FormaPagoDetalle fp : formasPago) {
            if (fp.getMonto() == null || fp.getMonto() <= 0) {
                throw new SQLException("Cada forma de pago debe tener un monto mayor a 0");
            }
            sumaFormas += fp.getMonto();
        }
        if (orden.getMonto() == null || sumaFormas != orden.getMonto()) {
            throw new SQLException("La suma de las formas de pago (" + sumaFormas
                    + ") no coincide con el monto de la orden de pago (" + orden.getMonto() + ")");
        }
    }
}
