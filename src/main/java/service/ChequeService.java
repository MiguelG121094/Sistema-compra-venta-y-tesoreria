/*
 * Service de gestión de cheques (Tesorería, §G3 del plan).
 * Dueño de la transacción (setAutoCommit(false)), como el resto del módulo.
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import modelo.Cheque;
import modelo.ChequeDAO;
import modelo.FormaPagoDetalleDAO;

/**
 * Gestión de cheques ya emitidos: anulación individual y registro de la entrega al proveedor.
 *
 * <p>El cheque nace dentro de la Orden de Pago; acá sólo se lo administra después.
 *
 * @author Miguel
 */
public class ChequeService {

    private final OrdenPagoService ordenPagoService = new OrdenPagoService();

    /** Todos los cheques con su chequera, su cuenta y la orden de pago que los emitió. */
    public List<Cheque> listarParaGestion() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ChequeDAO(conn).listarParaGestion();
        }
    }

    public Cheque getCheque(Long idCheque) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ChequeDAO(conn).getCheque(idCheque);
        }
    }

    /**
     * Anula un cheque emitido: el mal impreso, el extraviado y el rechazado.
     *
     * <p><b>Sólo si es la única forma de pago de su orden.</b> Anular el cheque devuelve el saldo
     * de las facturas que pagó, y con dos o más formas de pago no hay forma de saber qué factura
     * pagó este cheque: el detalle de la OP es por factura y el de las formas de pago es por medio,
     * y nada los une. Con una sola forma de pago el monto coincide con el total y no hay ambigüedad.
     * Con varias, el camino es anular la orden de pago entera.
     *
     * <p>La reversa es la de la OP ({@code anularOrdenPagoCompleta}), que ya hace exactamente lo que
     * hace falta y en una sola transacción: devuelve el saldo de cada factura, anula el cheque,
     * anula la cabecera y reactiva la provisión. Como el cheque queda anulado, deja de contar en la
     * conciliación y su plata vuelve al saldo de la cuenta, que se calcula desde ahí.
     *
     * <p>No se anula un cheque 'Cobrado' —el banco ya lo debitó, eso se arregla con un débito— ni
     * uno ya anulado.
     */
    public void anularChequeIndividual(Long idCheque) throws SQLException {
        if (idCheque == null) {
            throw new SQLException("No se indicó el cheque a anular");
        }

        // Validaciones de pantalla: avisan antes de arrancar la reversa. La palabra final la tiene
        // anularOrdenPagoCompleta, que revalida la OP con la transaccion abierta.
        Long idOrdenPago;
        try (Connection conn = Conexion.getConnection()) {
            Cheque cheque = new ChequeDAO(conn).getCheque(idCheque);
            if (cheque == null) {
                throw new SQLException("El cheque no existe");
            }
            String estado = cheque.getEstado();
            if (ChequeDAO.ESTADO_ANULADO.equals(estado)) {
                throw new SQLException("El cheque ya está anulado");
            }
            if (ChequeDAO.ESTADO_COBRADO.equals(estado)) {
                throw new SQLException("El cheque ya fue cobrado por el banco: no se puede anular");
            }

            FormaPagoDetalleDAO formaDAO = new FormaPagoDetalleDAO(conn);
            idOrdenPago = formaDAO.getIdOrdenPagoPorCheque(idCheque);
            if (idOrdenPago == null) {
                throw new SQLException("El cheque no está asociado a ninguna orden de pago");
            }
            if (formaDAO.contarPorOrden(idOrdenPago) > 1) {
                throw new SQLException("La orden de pago tiene más de una forma de pago: "
                        + "para deshacer este cheque hay que anular la orden de pago completa");
            }
        }

        // La reversa completa corre en su propia transacción y anula el cheque en cascada.
        ordenPagoService.anularOrdenPagoCompleta(idOrdenPago);
    }

    /**
     * Registra que el proveedor retiró el cheque: fecha y quién lo retiró, con estado 'Entregado'.
     *
     * <p>Es la misma operación que el botón de la Orden de Pago, pero de a un cheque. Re-ejecutarla
     * corrige los datos en vez de fallar, que es lo que hace falta cuando se cargó mal una fecha.
     * El número de recibo se sigue cargando desde la OP: es de la orden, no del cheque.
     */
    public void registrarEntrega(Long idCheque, Date fechaEntrega, String entregadoA)
            throws SQLException {

        if (idCheque == null) {
            throw new SQLException("No se indicó el cheque");
        }
        if (fechaEntrega == null) {
            throw new SQLException("Indique la fecha de entrega");
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            ChequeDAO chequeDAO = new ChequeDAO(conn);
            String estado = chequeDAO.getEstadoBloqueado(idCheque);
            if (estado == null) {
                throw new SQLException("El cheque no existe");
            }
            if (ChequeDAO.ESTADO_ANULADO.equals(estado)) {
                throw new SQLException("El cheque está anulado: no se puede entregar");
            }
            if (ChequeDAO.ESTADO_COBRADO.equals(estado)) {
                throw new SQLException("El cheque ya fue cobrado por el banco");
            }

            chequeDAO.registrarEntrega(idCheque, new java.sql.Date(fechaEntrega.getTime()), entregadoA);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en registrarEntrega - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
