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
import modelo.ProvisionCuentaPagarDAO;

/**
 * Service de Orden de Pago. Dueño de la transacción (setAutoCommit(false)), calcado del patrón
 * de ProvisionCuentaPagarService. Genera la OP en UNA transacción:
 *   0. Bloquea la provisión (FOR UPDATE) y valida que esté 'Pendiente' (guard anti doble-pago).
 *   1. INSERT orden_pago_cabecera.
 *   2. INSERT orden_pago_detalle (N facturas de la provisión, SOLO LECTURA).
 *   3. INSERT forma_pago_detalle (N formas: transferencia + cheque(s)); por cada línea de
 *      cheque se emite un cheque real de la chequera (nro dentro del rango).
 *   4. Descuenta cta_pag_saldo de cada factura + recalcula el estado.
 *   5. Marca la provisión como 'Procesada' (no puede volver a pagarse).
 * La anulación (anularOrdenPagoCompleta) revierte todo: devuelve el saldo, anula los cheques y
 * reactiva la provisión a 'Pendiente'. Ver MODULO_TESORERIA_PLAN.md §C.
 *
 * @author Miguel
 */
public class OrdenPagoService {

    private static final String ESTADO_CHEQUE_EMITIDO = "Emitido";
    private static final String ESTADO_OP_ANULADO     = "Anulado";

    // Estados de la provisión: 'Pendiente' = activa/lista para pagar; 'Procesada' = ya consumida
    // por una OP (no se puede volver a pagar); 'Anulado' = anulada. Ver MODULO_TESORERIA_PLAN.md §C.
    private static final String ESTADO_PROV_PENDIENTE = "Pendiente";
    private static final String ESTADO_PROV_PROCESADA = "Procesada";

    public List<OrdenPago> listarOrdenesPago() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new OrdenPagoDAO(conn).listarOrdenesPago();
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService.listarOrdenesPago: " + e);
            return null;
        }
    }

    /**
     * Guard: la provisión ya fue consumida por una orden de pago vigente (no anulada).
     *
     * <p>A diferencia de los listados, este método <b>propaga</b> la SQLException en vez de
     * devolver un valor por defecto. Es el mismo criterio que los guards de Factura de Compra
     * (ver NOTA_CREDITO_DEBITO_PLAN.md §8.4): si la consulta falla no podemos contestar "no hay
     * orden de pago", porque eso habilitaría por descuido una anulación destructiva.
     */
    public boolean tieneOrdenPagoActivaPorProvision(Long idProvision) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new OrdenPagoDAO(conn).tieneOrdenPagoActivaPorProvision(idProvision);
        }
    }

    /** Próximo correlativo de OP (ord_pag_numero), para mostrarlo al abrir una orden nueva. */
    public Integer obtenerProximoNumero() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new OrdenPagoDAO(conn).obtenerProximoNumero();
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService.obtenerProximoNumero: " + e);
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
            ProvisionCuentaPagarDAO provisionDAO = new ProvisionCuentaPagarDAO(conn);

            // 0. Consumir la provisión (guard anti doble-pago): se bloquea la fila de la provisión
            //    (FOR UPDATE) y se exige que esté 'Pendiente'. Si otra OP ya la procesó (o está
            //    anulada), se aborta. Dos OPs concurrentes se serializan por el lock y solo la
            //    primera la ve 'Pendiente'. Ver MODULO_TESORERIA_PLAN.md §C.
            Long idProvision = orden.getIdProvisionCtaPagar();
            String estadoProvision = provisionDAO.getEstadoBloqueado(idProvision);
            if (estadoProvision == null) {
                throw new SQLException("La provisión " + idProvision + " no existe");
            }
            if (!ESTADO_PROV_PENDIENTE.equals(estadoProvision)) {
                throw new SQLException("La provisión " + idProvision + " no está disponible para pagar "
                        + "(estado actual: '" + estadoProvision + "'). Ya fue procesada o anulada.");
            }

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

            // 5. Marcar la provisión como procesada (ya no puede volver a pagarse).
            provisionDAO.actualizarEstado(idProvision, ESTADO_PROV_PROCESADA);

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
     * Anula una Orden de Pago revirtiendo TODOS sus efectos en una sola transacción (reversa
     * simétrica de {@link #guardarOrdenPagoCompleta}):
     *   1. Valida que la OP exista y no esté ya anulada.
     *   2. Por cada factura del detalle: devuelve el importe al cta_pag_saldo y la deja 'En provision'.
     *   3. Por cada forma de pago con cheque: anula el cheque emitido.
     *   4. Marca la OP como 'Anulado'.
     *   5. Reactiva la provisión a 'Pendiente' (vuelve a estar disponible para pagar).
     * Rollback total si cualquier paso falla. Ver MODULO_TESORERIA_PLAN.md §C.
     */
    public void anularOrdenPagoCompleta(Long idOrdenPago) throws SQLException {
        if (idOrdenPago == null) {
            throw new SQLException("anularOrdenPagoCompleta: idOrdenPago es nulo");
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            OrdenPagoDAO ordenDAO = new OrdenPagoDAO(conn);
            OrdenPagoDetalleDAO detalleDAO = new OrdenPagoDetalleDAO(conn);
            FormaPagoDetalleDAO formaDAO = new FormaPagoDetalleDAO(conn);
            ChequeDAO chequeDAO = new ChequeDAO(conn);
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            ProvisionCuentaPagarDAO provisionDAO = new ProvisionCuentaPagarDAO(conn);

            // 1. Cargar la OP y validar que se pueda anular.
            OrdenPago orden = ordenDAO.getOrdenPago(idOrdenPago);
            if (orden == null) {
                throw new SQLException("La orden de pago " + idOrdenPago + " no existe");
            }
            if (ESTADO_OP_ANULADO.equals(orden.getEstado())) {
                throw new SQLException("La orden de pago " + idOrdenPago + " ya está anulada");
            }

            // 2. Devolver el saldo de cada factura pagada (queda 'En provision' de nuevo).
            List<OrdenPagoDetalle> detalles = detalleDAO.listarPorOrden(idOrdenPago);
            for (OrdenPagoDetalle det : detalles) {
                cuentaPagarDAO.restaurarSaldoPorAnulacionOP(
                    det.getCuentaPagar().getIdCuentaPagar(),
                    det.getCuentaPagar().getFacturaCompra().getIdFacturaCompra(),
                    det.getMonto());
            }

            // 3. Anular los cheques emitidos por esta OP.
            List<FormaPagoDetalle> formas = formaDAO.listarPorOrden(idOrdenPago);
            for (FormaPagoDetalle fp : formas) {
                if (fp.getCheque() != null && fp.getCheque().getIdCheque() != null) {
                    chequeDAO.anularCheque(fp.getCheque().getIdCheque());
                }
            }

            // 4. Anular la cabecera de la OP.
            ordenDAO.anularOrdenPago(idOrdenPago);

            // 5. Reactivar la provisión (vuelve a estar disponible para generar otra OP).
            provisionDAO.actualizarEstado(orden.getIdProvisionCtaPagar(), ESTADO_PROV_PENDIENTE);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en anularOrdenPagoCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
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
            if (fp.getCuenta() == null || fp.getCuenta().getIdCuenta() == null) {
                throw new SQLException("Cada forma de pago debe indicar la cuenta bancaria de origen");
            }
            // Fix 3: las líneas de cheque requieren los datos mínimos para emitir el cheque real;
            // sin esto, la emisión fallaría con un NullPointerException poco claro (el Service
            // defaultea el resto de los campos del cheque, pero NO estos tres).
            Cheque cheque = fp.getCheque();
            if (cheque != null) {
                if (cheque.getChequera() == null || cheque.getChequera().getIdChequera() == null) {
                    throw new SQLException("La forma de pago con cheque debe indicar la chequera");
                }
                if (cheque.getTipoCheque() == null || cheque.getTipoCheque().getIdTipoCheque() == null) {
                    throw new SQLException("La forma de pago con cheque debe indicar el tipo de cheque");
                }
                if (cheque.getUsuario() == null || cheque.getUsuario().getIdUsuario() == null) {
                    throw new SQLException("La forma de pago con cheque debe tener el usuario que lo emite");
                }
            }
            sumaFormas += fp.getMonto();
        }
        if (orden.getMonto() == null || sumaFormas != orden.getMonto()) {
            throw new SQLException("La suma de las formas de pago (" + sumaFormas
                    + ") no coincide con el monto de la orden de pago (" + orden.getMonto() + ")");
        }
    }
}
