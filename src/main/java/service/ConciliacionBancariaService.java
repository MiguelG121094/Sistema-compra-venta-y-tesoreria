package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import modelo.Cheque;
import modelo.ConciliacionBancaria;
import modelo.ConciliacionBancariaDAO;
import modelo.ConciliacionBancariaDetalle;
import modelo.FormaPagoDetalle;

/**
 * Service de la conciliacion bancaria (§F del MODULO_TESORERIA_PLAN.md y
 * CONCILIACION_BANCARIA_PLAN.md). Es dueno de la transaccion: cabecera, detalle y los estados de lo
 * conciliado se graban en una sola unidad.
 *
 * <p>La conciliacion no mueve ningun saldo, porque la cuenta bancaria no tiene: el saldo vive
 * unicamente en las conciliaciones, encadenado de una a la siguiente.
 *
 * @author Miguel
 */
public class ConciliacionBancariaService {

    /** Estados de forma_pag_estado. 'Pendiente' es el que deja OrdenPagoServlet al emitir la OP. */
    public static final String FORMA_PAGO_PENDIENTE = "Pendiente";
    public static final String FORMA_PAGO_CONCILIADO = "Conciliado";

    // ==================== CONSULTAS ====================

    public List<ConciliacionBancaria> listarConciliaciones() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ConciliacionBancariaDAO(conn).listarConciliaciones();
        } catch (SQLException e) {
            System.out.println("Error en ConciliacionBancariaService: " + e);
            return null;
        }
    }

    public ConciliacionBancaria getConciliacion(Long idConciliacion) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ConciliacionBancariaDAO(conn).getConciliacion(idConciliacion);
        } catch (SQLException e) {
            System.out.println("Error en ConciliacionBancariaService: " + e);
            return null;
        }
    }

    public List<ConciliacionBancariaDetalle> listarDetalles(Long idConciliacion) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ConciliacionBancariaDAO(conn).listarDetallesPorConciliacion(idConciliacion);
        } catch (SQLException e) {
            System.out.println("Error en ConciliacionBancariaService: " + e);
            return null;
        }
    }

    /** Ultima conciliacion vigente de la cuenta: de ella salen el saldo y el periodo anterior. */
    public ConciliacionBancaria getUltimaVigente(Long idCuenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ConciliacionBancariaDAO(conn).getUltimaVigente(idCuenta);
        } catch (SQLException e) {
            System.out.println("Error en ConciliacionBancariaService: " + e);
            return null;
        }
    }

    /** Saldo inicial encadenado de la cuenta, para mostrarlo al elegirla (va de solo lectura). */
    public long obtenerSaldoInicial(Long idCuenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ConciliacionBancariaDAO(conn).obtenerSaldoInicial(idCuenta);
        }
    }

    /** Los movimientos que arman la grilla: los del periodo mas los que quedaron arrastrados. */
    public List<ConciliacionBancariaDetalle> listarMovimientosAConciliar(Long idCuenta, Date fechaHasta)
            throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new ConciliacionBancariaDAO(conn).listarMovimientosAConciliar(idCuenta, fechaHasta);
        } catch (SQLException e) {
            System.out.println("Error en ConciliacionBancariaService: " + e);
            return null;
        }
    }

    // ==================== SALDOS ====================

    /**
     * Saldo segun libro al cierre del periodo: el saldo inicial mas los creditos y menos los
     * debitos y cheques <b>del periodo</b>.
     *
     * <p>Los arrastrados quedan afuera a proposito: ya se contaron en el saldo del periodo en que
     * ocurrieron. El libro registra el movimiento cuando pasa, no cuando el banco lo muestra.
     */
    public static long calcularSaldoLibro(long saldoInicial, List<ConciliacionBancariaDetalle> detalles,
            Date desde, Date hasta) {
        long saldo = saldoInicial;
        if (detalles == null) {
            return saldo;
        }
        for (ConciliacionBancariaDetalle detalle : detalles) {
            if (!esDelPeriodo(detalle, desde, hasta)) {
                continue;
            }
            saldo += esCredito(detalle) ? detalle.getMonto() : -detalle.getMonto();
        }
        return saldo;
    }

    /**
     * El saldo del extracto ajustado por las partidas conciliatorias, que son los items que
     * quedaron sin tildar: se le restan los cheques y debitos que el banco todavia no muestra y se
     * le suman los depositos que todavia no acredito. Si la conciliacion cierra, da lo mismo que
     * {@link #calcularSaldoLibro}.
     */
    public static long calcularSaldoAjustado(long saldoBanco, List<ConciliacionBancariaDetalle> detalles) {
        long saldo = saldoBanco;
        if (detalles == null) {
            return saldo;
        }
        for (ConciliacionBancariaDetalle detalle : detalles) {
            if (Boolean.TRUE.equals(detalle.getConciliado())) {
                continue;
            }
            saldo += esCredito(detalle) ? detalle.getMonto() : -detalle.getMonto();
        }
        return saldo;
    }

    /**
     * Lo que no se explica: la diferencia entre el extracto ajustado y el libro. En cero la
     * conciliacion cuadra. No se guarda —no hay columna— pero es el numero que mira el usuario.
     */
    public static long calcularDiferencia(ConciliacionBancaria conciliacion,
            List<ConciliacionBancariaDetalle> detalles) {
        // La pantalla llama a esto mientras se carga, con los saldos todavia en blanco.
        long saldoInicial = conciliacion.getSaldoInicial() == null ? 0L : conciliacion.getSaldoInicial();
        long saldoBanco = conciliacion.getSaldoBanco() == null ? 0L : conciliacion.getSaldoBanco();
        long libro = calcularSaldoLibro(saldoInicial, detalles,
                conciliacion.getFechaDesde(), conciliacion.getFechaHasta());
        return calcularSaldoAjustado(saldoBanco, detalles) - libro;
    }

    private static boolean esCredito(ConciliacionBancariaDetalle detalle) {
        return ConciliacionBancariaDAO.TIPO_CREDITO.equals(detalle.getTipo());
    }

    /** Un item es del periodo si la fecha de su movimiento cae dentro; si no, viene arrastrado. */
    private static boolean esDelPeriodo(ConciliacionBancariaDetalle detalle, Date desde, Date hasta) {
        Date fecha = ConciliacionBancariaDAO.fechaDelMovimiento(detalle);
        if (fecha == null || desde == null || hasta == null) {
            return false;
        }
        return !fecha.before(desde) && !fecha.after(hasta);
    }

    // ==================== GRABAR ====================

    /**
     * Graba la conciliacion completa: cabecera, items numerados y el cierre de los estados de lo
     * tildado, todo en una transaccion.
     *
     * <p>El saldo inicial no se toma de lo que llega de la pantalla sino que se vuelve a leer de la
     * conciliacion anterior, y el saldo final se calcula aca: son los dos numeros que encadenan un
     * periodo con el siguiente y no pueden depender del JavaScript.
     *
     * <p>Se permite grabar con diferencia: los items sin tildar son justamente la explicacion de
     * esa diferencia, y como el saldo final sale del libro y no del extracto, una conciliacion que
     * no cuadra no ensucia el encadenado.
     */
    public Long guardarConciliacionCompleta(ConciliacionBancaria conciliacion,
            List<ConciliacionBancariaDetalle> detalles) throws SQLException {

        if (conciliacion == null || conciliacion.getCuenta() == null
                || conciliacion.getCuenta().getIdCuenta() == null) {
            throw new SQLException("Debe seleccionar la cuenta bancaria a conciliar");
        }
        if (conciliacion.getFechaDesde() == null || conciliacion.getFechaHasta() == null) {
            throw new SQLException("Debe indicar el período a conciliar");
        }
        if (conciliacion.getFechaDesde().after(conciliacion.getFechaHasta())) {
            throw new SQLException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        if (conciliacion.getSaldoBanco() == null) {
            throw new SQLException("Debe cargar el saldo del extracto bancario");
        }
        if (conciliacion.getFecha() == null) {
            conciliacion.setFecha(new Date());
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            ConciliacionBancariaDAO conciliacionDAO = new ConciliacionBancariaDAO(conn);
            Long idCuenta = conciliacion.getCuenta().getIdCuenta();

            validarPeriodo(conciliacionDAO, idCuenta, conciliacion.getFechaDesde());

            long saldoInicial = conciliacionDAO.obtenerSaldoInicial(idCuenta);
            conciliacion.setSaldoInicial(saldoInicial);
            conciliacion.setSaldoFinal(calcularSaldoLibro(saldoInicial, detalles,
                    conciliacion.getFechaDesde(), conciliacion.getFechaHasta()));

            Long idConciliacion = conciliacionDAO.insertarConciliacion(conciliacion);

            long numeroItem = 1;
            if (detalles != null) {
                for (ConciliacionBancariaDetalle detalle : detalles) {
                    validarDetalle(detalle);
                    detalle.setNumeroItem(numeroItem++);
                    detalle.setConciliacionBancaria(conciliacion);
                    conciliacionDAO.insertarDetalle(detalle, idConciliacion);
                    cerrarEstados(conciliacionDAO, detalle);
                }
            }

            conn.commit();
            return idConciliacion;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ConciliacionBancariaService.guardarConciliacionCompleta: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    /**
     * El periodo tiene que arrancar el dia siguiente al cierre de la conciliacion anterior de esa
     * cuenta. Con el saldo encadenado no es una comodidad: un hueco deja movimientos que nadie
     * concilio y un solape los cuenta dos veces.
     */
    private void validarPeriodo(ConciliacionBancariaDAO conciliacionDAO, Long idCuenta, Date desde)
            throws SQLException {
        ConciliacionBancaria anterior = conciliacionDAO.getUltimaVigente(idCuenta);
        if (anterior == null || anterior.getFechaHasta() == null) {
            return;
        }
        Date esperada = diaSiguiente(anterior.getFechaHasta());
        if (!mismoDia(desde, esperada)) {
            throw new SQLException("El período debe arrancar el "
                    + new java.text.SimpleDateFormat("dd/MM/yyyy").format(esperada)
                    + ", que es el día siguiente al cierre de la última conciliación de la cuenta");
        }
    }

    private void validarDetalle(ConciliacionBancariaDetalle detalle) throws SQLException {
        if (detalle.getMonto() == null || detalle.getTipo() == null) {
            throw new SQLException("Hay un movimiento sin monto o sin tipo");
        }
        boolean tieneOrigen = detalle.getFormaPagoDetalle() != null
                || detalle.getDebito() != null || detalle.getCredito() != null;
        if (!tieneOrigen) {
            throw new SQLException("Hay un movimiento que no apunta a ningún origen");
        }
    }

    /**
     * Deja los estados de lo conciliado al dia. No gobiernan el arrastre —eso lo decide el detalle
     * de las conciliaciones vigentes— pero son lo que muestran la orden de pago y el cheque.
     */
    private void cerrarEstados(ConciliacionBancariaDAO conciliacionDAO,
            ConciliacionBancariaDetalle detalle) throws SQLException {
        FormaPagoDetalle formaPago = detalle.getFormaPagoDetalle();
        if (formaPago == null || formaPago.getIdFormaPagoDetalle() == null) {
            return;
        }
        boolean conciliado = Boolean.TRUE.equals(detalle.getConciliado());
        conciliacionDAO.actualizarEstadoFormaPago(formaPago.getIdFormaPagoDetalle(),
                conciliado ? FORMA_PAGO_CONCILIADO : FORMA_PAGO_PENDIENTE);

        Cheque cheque = formaPago.getCheque();
        if (conciliado && cheque != null && cheque.getIdCheque() != null) {
            conciliacionDAO.marcarChequeCobrado(cheque.getIdCheque());
        }
    }

    // ==================== ANULAR ====================

    /**
     * Anula la conciliacion y revierte todo lo que el grabado cerro. La fila no se borra: queda
     * como historia y sus items siguen ahi, pero al estar anulada dejan de contar y los
     * movimientos vuelven a aparecer en la proxima conciliacion.
     *
     * <p>Solo se puede anular la ultima vigente de la cuenta: si se anulara una del medio, las
     * posteriores quedarian partiendo de un saldo inicial que ya no existe.
     */
    public void anularConciliacionCompleta(Long idConciliacion) throws SQLException {
        if (idConciliacion == null) {
            throw new SQLException("Debe indicar la conciliación a anular");
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            ConciliacionBancariaDAO conciliacionDAO = new ConciliacionBancariaDAO(conn);

            String estado = conciliacionDAO.getEstadoBloqueado(idConciliacion);
            if (estado == null) {
                throw new SQLException("La conciliación " + idConciliacion + " no existe");
            }
            if (ConciliacionBancariaDAO.ESTADO_ANULADO.equals(estado)) {
                throw new SQLException("La conciliación ya estaba anulada");
            }

            ConciliacionBancaria conciliacion = conciliacionDAO.getConciliacion(idConciliacion);
            validarQueSeaLaUltima(conciliacionDAO, conciliacion);

            for (ConciliacionBancariaDetalle detalle
                    : conciliacionDAO.listarDetallesPorConciliacion(idConciliacion)) {
                revertirEstados(conciliacionDAO, detalle);
            }
            conciliacionDAO.actualizarEstado(idConciliacion, ConciliacionBancariaDAO.ESTADO_ANULADO);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ConciliacionBancariaService.anularConciliacionCompleta: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    private void validarQueSeaLaUltima(ConciliacionBancariaDAO conciliacionDAO,
            ConciliacionBancaria conciliacion) throws SQLException {
        if (conciliacion == null || conciliacion.getCuenta() == null) {
            throw new SQLException("La conciliación no tiene cuenta asociada");
        }
        ConciliacionBancaria ultima = conciliacionDAO.getUltimaVigente(
                conciliacion.getCuenta().getIdCuenta());
        if (ultima != null
                && !ultima.getIdConciliacionBancaria().equals(conciliacion.getIdConciliacionBancaria())) {
            throw new SQLException("Solo se puede anular la última conciliación de la cuenta: "
                    + "antes hay que anular las posteriores, que arrancan del saldo de ésta");
        }
    }

    /** Lo contrario de {@link #cerrarEstados}: devuelve la forma de pago y el cheque a lo que eran. */
    private void revertirEstados(ConciliacionBancariaDAO conciliacionDAO,
            ConciliacionBancariaDetalle detalle) throws SQLException {
        FormaPagoDetalle formaPago = detalle.getFormaPagoDetalle();
        if (formaPago == null || formaPago.getIdFormaPagoDetalle() == null) {
            return;
        }
        conciliacionDAO.actualizarEstadoFormaPago(formaPago.getIdFormaPagoDetalle(), FORMA_PAGO_PENDIENTE);

        Cheque cheque = formaPago.getCheque();
        if (cheque != null && cheque.getIdCheque() != null) {
            conciliacionDAO.revertirChequeCobrado(cheque.getIdCheque());
        }
    }

    // ==================== AUXILIARES ====================

    private static Date diaSiguiente(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.add(Calendar.DAY_OF_MONTH, 1);
        return calendario.getTime();
    }

    /** Compara solo la fecha: las de la BD vienen sin hora, pero las de la pantalla no siempre. */
    private static boolean mismoDia(Date una, Date otra) {
        if (una == null || otra == null) {
            return false;
        }
        Calendar calendarioUna = Calendar.getInstance();
        calendarioUna.setTime(una);
        Calendar calendarioOtra = Calendar.getInstance();
        calendarioOtra.setTime(otra);
        return calendarioUna.get(Calendar.YEAR) == calendarioOtra.get(Calendar.YEAR)
                && calendarioUna.get(Calendar.DAY_OF_YEAR) == calendarioOtra.get(Calendar.DAY_OF_YEAR);
    }

    private void cerrar(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
