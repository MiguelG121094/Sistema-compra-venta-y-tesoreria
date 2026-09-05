package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * DAO de la conciliacion bancaria, cabecera y detalle en el mismo DAO (igual que
 * ProvisionCuentaPagarDAO). §F del MODULO_TESORERIA_PLAN.md y CONCILIACION_BANCARIA_PLAN.md.
 *
 * La conciliacion es una foto que se arma al conciliar: la orden de pago nunca escribe aca, porque
 * la cabecera es por cuenta y periodo y no existe todavia cuando se genera la OP. Lo que se concilia
 * no es la orden de pago sino la forma de pago, que es el movimiento que aparece en el extracto: una
 * OP puede pagarse con una transferencia de un banco y dos cheques de otro.
 *
 * Corre sobre la Connection compartida; la transaccion la controla el Service.
 *
 * @author Miguel
 */
public class ConciliacionBancariaDAO {

    public static final String ESTADO_VIGENTE = "Vigente";
    public static final String ESTADO_ANULADO = "Anulado";

    /** Tipos de item, segun el comentario de conc_bancaria_tipo. */
    public static final String TIPO_CREDITO = "Cred";
    public static final String TIPO_DEBITO = "Deb";
    public static final String TIPO_CHEQUE = "Ch";

    private static final String COLUMNAS =
            "id_conc_bancaria, id_cuenta, conc_bancaria_fecha_desde, conc_bancaria_fecha, "
            + "conc_bancaria_fecha_hasta, conc_bancaria_saldo_inicial, conc_bancaria_saldo_final, "
            + "conc_banc_saldo_banco, conc_bancaria_estado";

    /**
     * Un movimiento sigue pendiente mientras no este en el detalle de ninguna conciliacion que no
     * haya sido anulada. Se pregunta contra el detalle y no contra forma_pag_estado / chq_estado
     * porque debitos y creditos no tienen estado de conciliacion, y porque asi el arrastre no
     * depende de que esos dos campos se hayan actualizado bien. El COALESCE cubre las filas
     * anteriores al alta de conc_bancaria_estado, que valen como vigentes.
     */
    private static final String PENDIENTE =
            "NOT EXISTS (SELECT 1 FROM conciliacion_bancaria_detalle cd "
            + "JOIN conciliacion_bancaria cc ON cd.id_conc_bancaria = cc.id_conc_bancaria "
            + "WHERE COALESCE(cc.conc_bancaria_estado, '" + ESTADO_VIGENTE + "') <> '" + ESTADO_ANULADO + "' "
            + "AND cd.%s = %s)";

    private Connection conn;

    public ConciliacionBancariaDAO(Connection conn) {
        this.conn = conn;
    }

    // ==================== CABECERA ====================

    private ConciliacionBancaria mapear(ResultSet rs) throws SQLException {
        ConciliacionBancaria conciliacion = new ConciliacionBancaria();
        conciliacion.setIdConciliacionBancaria(rs.getLong("id_conc_bancaria"));
        conciliacion.setCuenta(new CuentaDAO(conn).getCuenta(rs.getLong("id_cuenta")));
        conciliacion.setFechaDesde(rs.getDate("conc_bancaria_fecha_desde"));
        conciliacion.setFecha(rs.getDate("conc_bancaria_fecha"));
        conciliacion.setFechaHasta(rs.getDate("conc_bancaria_fecha_hasta"));
        conciliacion.setSaldoInicial(rs.getLong("conc_bancaria_saldo_inicial"));
        conciliacion.setSaldoFinal(rs.getLong("conc_bancaria_saldo_final"));
        conciliacion.setSaldoBanco(rs.getLong("conc_banc_saldo_banco"));
        conciliacion.setEstado(rs.getString("conc_bancaria_estado"));
        return conciliacion;
    }

    public ConciliacionBancaria getConciliacion(Long idConciliacion) throws SQLException {
        if (idConciliacion == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM conciliacion_bancaria WHERE id_conc_bancaria = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConciliacion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Listado del modal de busqueda: las mas nuevas primero, anuladas incluidas. */
    public List<ConciliacionBancaria> listarConciliaciones() throws SQLException {
        List<ConciliacionBancaria> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM conciliacion_bancaria ORDER BY id_conc_bancaria DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Ultima conciliacion vigente de una cuenta. De ella salen el saldo inicial encadenado y el
     * periodo anterior, y es la unica que se puede anular: si se anulara una del medio, las
     * posteriores quedarian partiendo de un saldo que ya no existe.
     */
    public ConciliacionBancaria getUltimaVigente(Long idCuenta) throws SQLException {
        if (idCuenta == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM conciliacion_bancaria "
                   + "WHERE id_cuenta = ? AND COALESCE(conc_bancaria_estado, ?) <> ? "
                   + "ORDER BY conc_bancaria_fecha_hasta DESC, id_conc_bancaria DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            stmt.setString(2, ESTADO_VIGENTE);
            stmt.setString(3, ESTADO_ANULADO);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Saldo inicial encadenado: el saldo final de la ultima conciliacion vigente de la cuenta, o
     * cero si es la primera. La empresa que ya venia conciliando carga su saldo de arranque como un
     * credito, no aca.
     */
    public long obtenerSaldoInicial(Long idCuenta) throws SQLException {
        ConciliacionBancaria anterior = getUltimaVigente(idCuenta);
        if (anterior == null || anterior.getSaldoFinal() == null) {
            return 0L;
        }
        return anterior.getSaldoFinal();
    }

    public Long insertarConciliacion(ConciliacionBancaria conciliacion) throws SQLException {
        String sql = "INSERT INTO conciliacion_bancaria (id_cuenta, conc_bancaria_fecha_desde, "
                   + "conc_bancaria_fecha, conc_bancaria_fecha_hasta, conc_bancaria_saldo_inicial, "
                   + "conc_bancaria_saldo_final, conc_banc_saldo_banco, conc_bancaria_estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, conciliacion.getCuenta().getIdCuenta());
            stmt.setDate(2, new java.sql.Date(conciliacion.getFechaDesde().getTime()));
            stmt.setDate(3, new java.sql.Date(conciliacion.getFecha().getTime()));
            stmt.setDate(4, new java.sql.Date(conciliacion.getFechaHasta().getTime()));
            stmt.setLong(5, conciliacion.getSaldoInicial());
            stmt.setLong(6, conciliacion.getSaldoFinal());
            stmt.setLong(7, conciliacion.getSaldoBanco());
            stmt.setString(8, ESTADO_VIGENTE);

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó la conciliación, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    conciliacion.setIdConciliacionBancaria(id);
                    conciliacion.setEstado(ESTADO_VIGENTE);
                    return id;
                }
            }
            throw new SQLException("No se generó id de conciliación");
        }
    }

    /** Estado de la conciliacion bloqueando la fila, para que no se anule dos veces a la vez. */
    public String getEstadoBloqueado(Long idConciliacion) throws SQLException {
        String sql = "SELECT conc_bancaria_estado FROM conciliacion_bancaria "
                   + "WHERE id_conc_bancaria = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConciliacion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String estado = rs.getString("conc_bancaria_estado");
                    return estado == null ? ESTADO_VIGENTE : estado;
                }
            }
        }
        return null;
    }

    public void actualizarEstado(Long idConciliacion, String estado) throws SQLException {
        String sql = "UPDATE conciliacion_bancaria SET conc_bancaria_estado = ? "
                   + "WHERE id_conc_bancaria = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setLong(2, idConciliacion);
            stmt.executeUpdate();
        }
    }

    // ==================== DETALLE ====================

    public void insertarDetalle(ConciliacionBancariaDetalle detalle, Long idConciliacion) throws SQLException {
        String sql = "INSERT INTO conciliacion_bancaria_detalle (id_conc_bancaria, "
                   + "conc_bancaria_nro_item, id_creditos, id_debitos, id_orden_pago, "
                   + "id_forma_pago_det, conc_bancaria_descripcion, conc_bancaria_monto, "
                   + "conc_bancaria_tipo, conc_bancaria_conciliado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConciliacion);
            stmt.setLong(2, detalle.getNumeroItem());
            setIdONulo(stmt, 3, detalle.getCredito() == null ? null : detalle.getCredito().getIdCredito());
            setIdONulo(stmt, 4, detalle.getDebito() == null ? null : detalle.getDebito().getIdDebito());
            setIdONulo(stmt, 5, detalle.getOrdenPago() == null ? null : detalle.getOrdenPago().getIdOrdenPago());
            setIdONulo(stmt, 6, detalle.getFormaPagoDetalle() == null
                    ? null : detalle.getFormaPagoDetalle().getIdFormaPagoDetalle());
            stmt.setString(7, detalle.getDescripcion());
            stmt.setLong(8, detalle.getMonto());
            stmt.setString(9, detalle.getTipo());
            stmt.setBoolean(10, Boolean.TRUE.equals(detalle.getConciliado()));
            stmt.executeUpdate();
        }
    }

    private void setIdONulo(PreparedStatement stmt, int posicion, Long id) throws SQLException {
        if (id != null) {
            stmt.setLong(posicion, id);
        } else {
            stmt.setNull(posicion, Types.INTEGER);
        }
    }

    /**
     * Items grabados de una conciliacion, con lo que la grilla necesita de cada origen: la fecha de
     * emision, la del movimiento, el numero de documento y el de la orden de pago. La descripcion y
     * el monto salen de la fila del detalle, que es la foto del momento en que se concilio.
     */
    public List<ConciliacionBancariaDetalle> listarDetallesPorConciliacion(Long idConciliacion) throws SQLException {
        List<ConciliacionBancariaDetalle> lista = new ArrayList<>();
        if (idConciliacion == null) {
            return lista;
        }
        String sql = "SELECT d.conc_bancaria_nro_item, d.id_creditos, d.id_debitos, d.id_orden_pago, "
                   + "d.id_forma_pago_det, d.conc_bancaria_descripcion, d.conc_bancaria_monto, "
                   + "d.conc_bancaria_tipo, d.conc_bancaria_conciliado, "
                   + "deb.debitos_nro_comprobante, deb.debitos_fecha, deb.debitos_detalle, deb.debito_monto, "
                   + "cre.creditos_nro_comprobante, cre.creditos_fecha, cre.creditos_detalle, cre.credito_monto, "
                   + "fp.forma_pag_monto, fp.forma_pag_estado, fp.forma_pag_referencia, fp.forma_pag_fecha, "
                   + "ch.id_cheque, ch.chq_numero, ch.chq_fecha_emision, ch.chq_estado, ch.chq_a_la_orden, "
                   + "op.ord_pag_numero, pr.id_proveedor, pr.prov_razon_social "
                   + "FROM conciliacion_bancaria_detalle d "
                   + "LEFT JOIN debitos deb ON d.id_debitos = deb.id_debitos "
                   + "LEFT JOIN creditos cre ON d.id_creditos = cre.id_creditos "
                   + "LEFT JOIN forma_pago_detalle fp ON d.id_forma_pago_det = fp.id_forma_pago_det "
                   + "LEFT JOIN cheque ch ON fp.id_cheque = ch.id_cheque "
                   + "LEFT JOIN orden_pago_cabecera op ON d.id_orden_pago = op.id_orden_pago "
                   + "LEFT JOIN proveedor pr ON op.id_proveedor = pr.id_proveedor "
                   + "WHERE d.id_conc_bancaria = ? ORDER BY d.conc_bancaria_nro_item";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idConciliacion);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ConciliacionBancariaDetalle detalle = new ConciliacionBancariaDetalle();
                    detalle.setNumeroItem(rs.getLong("conc_bancaria_nro_item"));
                    detalle.setDescripcion(rs.getString("conc_bancaria_descripcion"));
                    detalle.setMonto(rs.getLong("conc_bancaria_monto"));
                    detalle.setTipo(rs.getString("conc_bancaria_tipo"));
                    detalle.setConciliado(rs.getBoolean("conc_bancaria_conciliado"));

                    long idDebito = rs.getLong("id_debitos");
                    if (!rs.wasNull()) {
                        detalle.setDebito(mapearDebitoLiviano(rs, idDebito));
                    }
                    long idCredito = rs.getLong("id_creditos");
                    if (!rs.wasNull()) {
                        detalle.setCredito(mapearCreditoLiviano(rs, idCredito));
                    }
                    long idFormaPago = rs.getLong("id_forma_pago_det");
                    if (!rs.wasNull()) {
                        detalle.setFormaPagoDetalle(mapearFormaPagoLiviana(rs, idFormaPago));
                    }
                    long idOrdenPago = rs.getLong("id_orden_pago");
                    if (!rs.wasNull()) {
                        detalle.setOrdenPago(mapearOrdenPagoLiviana(rs, idOrdenPago));
                    }
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }

    // ==================== MOVIMIENTOS A CONCILIAR ====================

    /**
     * La consulta central del modulo: los movimientos que le tocan a una conciliacion de esta
     * cuenta y este periodo. No son solo los del periodo, son
     *
     *     los del periodo + los anteriores que quedaron sin conciliar
     *
     * porque un cheque tiene alrededor de un mes para cobrarse y tiene que volver a aparecer mes a
     * mes hasta que se cobre.
     *
     * Por eso el unico corte es la fecha hasta y la fecha desde de la cabecera no entra en la
     * consulta: cualquier movimiento mas viejo que siga pendiente tiene que aparecer igual.
     *
     * Los items vuelven sin numerar y sin conciliacion asignada — eso lo hace el Service al grabar.
     * Los debitos y creditos nacen tildados porque se cargan cuando ya ocurrieron en el banco; los
     * cheques nacen destildados.
     */
    public List<ConciliacionBancariaDetalle> listarMovimientosAConciliar(Long idCuenta, Date fechaHasta)
            throws SQLException {
        List<ConciliacionBancariaDetalle> movimientos = new ArrayList<>();
        if (idCuenta == null || fechaHasta == null) {
            return movimientos;
        }
        movimientos.addAll(listarMovimientosDeFormasDePago(idCuenta, fechaHasta));
        movimientos.addAll(listarMovimientosDeDebitos(idCuenta, fechaHasta));
        movimientos.addAll(listarMovimientosDeCreditos(idCuenta, fechaHasta));
        Collections.sort(movimientos, new Comparator<ConciliacionBancariaDetalle>() {
            @Override
            public int compare(ConciliacionBancariaDetalle uno, ConciliacionBancariaDetalle otro) {
                Date fechaUno = fechaDelMovimiento(uno);
                Date fechaOtro = fechaDelMovimiento(otro);
                if (fechaUno == null || fechaOtro == null) {
                    return fechaUno == fechaOtro ? 0 : (fechaUno == null ? 1 : -1);
                }
                return fechaUno.compareTo(fechaOtro);
            }
        });
        return movimientos;
    }

    /**
     * Fecha del movimiento, que es la que ordena la grilla. En un cheque no coincide con la de
     * emision: se emite en un mes y se cobra en otro, que es justamente lo que hace visible el
     * arrastre.
     */
    public static Date fechaDelMovimiento(ConciliacionBancariaDetalle detalle) {
        if (detalle.getFormaPagoDetalle() != null) {
            return detalle.getFormaPagoDetalle().getFecha();
        }
        if (detalle.getDebito() != null) {
            return detalle.getDebito().getFecha();
        }
        if (detalle.getCredito() != null) {
            return detalle.getCredito().getFecha();
        }
        return null;
    }

    /**
     * Formas de pago de ordenes de pago no anuladas. El tipo lo da el cheque: con id_cheque es 'Ch'
     * y sin el es 'Deb', una transferencia. La descripcion es el proveedor, que es lo que se lee en
     * la columna DETALLE de la grilla.
     */
    private List<ConciliacionBancariaDetalle> listarMovimientosDeFormasDePago(Long idCuenta, Date hasta)
            throws SQLException {
        List<ConciliacionBancariaDetalle> lista = new ArrayList<>();
        String sql = "SELECT fp.id_forma_pago_det, fp.forma_pag_monto, fp.forma_pag_estado, "
                   + "fp.forma_pag_referencia, fp.forma_pag_fecha, "
                   + "ch.id_cheque, ch.chq_numero, ch.chq_fecha_emision, ch.chq_estado, ch.chq_a_la_orden, "
                   + "op.id_orden_pago, op.ord_pag_numero, pr.id_proveedor, pr.prov_razon_social "
                   + "FROM forma_pago_detalle fp "
                   + "JOIN orden_pago_cabecera op ON fp.id_orden_pago = op.id_orden_pago "
                   + "JOIN proveedor pr ON op.id_proveedor = pr.id_proveedor "
                   + "LEFT JOIN cheque ch ON fp.id_cheque = ch.id_cheque "
                   + "WHERE fp.id_cuenta = ? AND fp.forma_pag_fecha IS NOT NULL "
                   + "AND fp.forma_pag_fecha <= ? "
                   + "AND op.ord_pag_estado <> '" + OrdenPagoDAO.ESTADO_ANULADO + "' "
                   + "AND (ch.id_cheque IS NULL OR ch.chq_estado <> '" + ChequeDAO.ESTADO_ANULADO + "') "
                   + "AND " + String.format(PENDIENTE, "id_forma_pago_det", "fp.id_forma_pago_det") + " "
                   + "ORDER BY fp.forma_pag_fecha, fp.id_forma_pago_det";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            stmt.setDate(2, new java.sql.Date(hasta.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ConciliacionBancariaDetalle detalle = new ConciliacionBancariaDetalle();
                    FormaPagoDetalle formaPago = mapearFormaPagoLiviana(rs, rs.getLong("id_forma_pago_det"));
                    detalle.setFormaPagoDetalle(formaPago);
                    detalle.setOrdenPago(mapearOrdenPagoLiviana(rs, rs.getLong("id_orden_pago")));
                    detalle.setMonto(rs.getLong("forma_pag_monto"));
                    detalle.setDescripcion(rs.getString("prov_razon_social"));
                    boolean esCheque = formaPago.getCheque() != null;
                    detalle.setTipo(esCheque ? TIPO_CHEQUE : TIPO_DEBITO);
                    // El cheque nace destildado: puede tardar un mes en presentarse al banco.
                    detalle.setConciliado(!esCheque);
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }

    private List<ConciliacionBancariaDetalle> listarMovimientosDeDebitos(Long idCuenta, Date hasta)
            throws SQLException {
        List<ConciliacionBancariaDetalle> lista = new ArrayList<>();
        String sql = "SELECT d.id_debitos, d.debitos_nro_comprobante, d.debitos_fecha, "
                   + "d.debitos_detalle, d.debito_monto "
                   + "FROM debitos d "
                   + "WHERE d.id_cuenta = ? AND d.debitos_fecha <= ? "
                   + "AND COALESCE(d.debitos_estado, '" + DebitoDAO.ESTADO_VIGENTE + "') = '"
                   + DebitoDAO.ESTADO_VIGENTE + "' "
                   + "AND " + String.format(PENDIENTE, "id_debitos", "d.id_debitos") + " "
                   + "ORDER BY d.debitos_fecha, d.id_debitos";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            stmt.setDate(2, new java.sql.Date(hasta.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ConciliacionBancariaDetalle detalle = new ConciliacionBancariaDetalle();
                    detalle.setDebito(mapearDebitoLiviano(rs, rs.getLong("id_debitos")));
                    detalle.setMonto(rs.getLong("debito_monto"));
                    detalle.setDescripcion(rs.getString("debitos_detalle"));
                    detalle.setTipo(TIPO_DEBITO);
                    // Debitos y creditos nacen tildados: se cargan cuando ya ocurrieron en el banco.
                    detalle.setConciliado(Boolean.TRUE);
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }

    private List<ConciliacionBancariaDetalle> listarMovimientosDeCreditos(Long idCuenta, Date hasta)
            throws SQLException {
        List<ConciliacionBancariaDetalle> lista = new ArrayList<>();
        String sql = "SELECT c.id_creditos, c.creditos_nro_comprobante, c.creditos_fecha, "
                   + "c.creditos_detalle, c.credito_monto "
                   + "FROM creditos c "
                   + "WHERE c.id_cuenta = ? AND c.creditos_fecha <= ? "
                   + "AND COALESCE(c.creditos_estado, '" + CreditoDAO.ESTADO_VIGENTE + "') = '"
                   + CreditoDAO.ESTADO_VIGENTE + "' "
                   + "AND " + String.format(PENDIENTE, "id_creditos", "c.id_creditos") + " "
                   + "ORDER BY c.creditos_fecha, c.id_creditos";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuenta);
            stmt.setDate(2, new java.sql.Date(hasta.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ConciliacionBancariaDetalle detalle = new ConciliacionBancariaDetalle();
                    detalle.setCredito(mapearCreditoLiviano(rs, rs.getLong("id_creditos")));
                    detalle.setMonto(rs.getLong("credito_monto"));
                    detalle.setDescripcion(rs.getString("creditos_detalle"));
                    detalle.setTipo(TIPO_CREDITO);
                    detalle.setConciliado(Boolean.TRUE);
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }

    // ==================== ESTADOS DE LO CONCILIADO ====================

    /**
     * Cierra o reabre la forma de pago. El estado no gobierna el arrastre —eso lo decide el detalle
     * de las conciliaciones vigentes— pero es lo que muestra la orden de pago, asi que se mantiene
     * al dia en las dos direcciones: 'Conciliado' al grabar, 'Pendiente' al anular.
     */
    public void actualizarEstadoFormaPago(Long idFormaPagoDetalle, String estado) throws SQLException {
        String sql = "UPDATE forma_pago_detalle SET forma_pag_estado = ? WHERE id_forma_pago_det = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setLong(2, idFormaPagoDetalle);
            stmt.executeUpdate();
        }
    }

    /**
     * Marca el cheque como cobrado. Es el unico lugar del sistema donde ese estado se usa: hasta que
     * se tilda en una conciliacion, el cheque queda emitido o entregado.
     */
    public void marcarChequeCobrado(Long idCheque) throws SQLException {
        String sql = "UPDATE cheque SET chq_estado = '" + ChequeDAO.ESTADO_COBRADO + "' "
                   + "WHERE id_cheque = ? AND chq_estado <> '" + ChequeDAO.ESTADO_ANULADO + "'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCheque);
            stmt.executeUpdate();
        }
    }

    /**
     * Devuelve el cheque al estado que tenia antes de conciliarse, al anular la conciliacion. El
     * estado anterior no se guarda en ningun lado pero se deduce: si tiene fecha de entrega es
     * porque llego a entregarse. Un cheque anulado no se toca, nunca estuvo conciliado.
     *
     * <p>Se lee primero y se decide en Java, en vez de resolverlo con un CASE en el UPDATE.
     */
    public void revertirChequeCobrado(Long idCheque) throws SQLException {
        String estadoActual = null;
        java.sql.Date fechaEntrega = null;
        String consulta = "SELECT chq_estado, chq_fecha_entrega FROM cheque WHERE id_cheque = ?";
        try (PreparedStatement stmt = conn.prepareStatement(consulta)) {
            stmt.setLong(1, idCheque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    estadoActual = rs.getString("chq_estado");
                    fechaEntrega = rs.getDate("chq_fecha_entrega");
                }
            }
        }
        if (!ChequeDAO.ESTADO_COBRADO.equals(estadoActual)) {
            return;
        }
        String estadoAnterior = fechaEntrega != null
                ? ChequeDAO.ESTADO_ENTREGADO : ChequeDAO.ESTADO_EMITIDO;

        String sql = "UPDATE cheque SET chq_estado = ? WHERE id_cheque = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estadoAnterior);
            stmt.setLong(2, idCheque);
            stmt.executeUpdate();
        }
    }

    // ==================== MAPEOS LIVIANOS ====================
    // Traen solo lo que la grilla muestra de cada origen. La cuenta bancaria no se hidrata: la
    // conciliacion es por cuenta y esa cuenta ya esta en la cabecera.

    private Debito mapearDebitoLiviano(ResultSet rs, long idDebito) throws SQLException {
        Debito debito = new Debito();
        debito.setIdDebito(idDebito);
        debito.setNumeroComprobante(rs.getLong("debitos_nro_comprobante"));
        debito.setFecha(rs.getDate("debitos_fecha"));
        debito.setDetalle(rs.getString("debitos_detalle"));
        debito.setMonto(rs.getLong("debito_monto"));
        return debito;
    }

    private Credito mapearCreditoLiviano(ResultSet rs, long idCredito) throws SQLException {
        Credito credito = new Credito();
        credito.setIdCredito(idCredito);
        credito.setNumeroComprobante(rs.getLong("creditos_nro_comprobante"));
        credito.setFecha(rs.getDate("creditos_fecha"));
        credito.setDetalle(rs.getString("creditos_detalle"));
        credito.setMonto(rs.getLong("credito_monto"));
        return credito;
    }

    private FormaPagoDetalle mapearFormaPagoLiviana(ResultSet rs, long idFormaPagoDetalle) throws SQLException {
        FormaPagoDetalle formaPago = new FormaPagoDetalle();
        formaPago.setIdFormaPagoDetalle(idFormaPagoDetalle);
        formaPago.setMonto(rs.getLong("forma_pag_monto"));
        formaPago.setEstado(rs.getString("forma_pag_estado"));
        formaPago.setReferencia(rs.getString("forma_pag_referencia"));
        formaPago.setFecha(rs.getDate("forma_pag_fecha"));
        long idCheque = rs.getLong("id_cheque");
        if (!rs.wasNull()) {
            Cheque cheque = new Cheque();
            cheque.setIdCheque(idCheque);
            cheque.setNumero(rs.getLong("chq_numero"));
            cheque.setFechaEmision(rs.getDate("chq_fecha_emision"));
            cheque.setEstado(rs.getString("chq_estado"));
            cheque.setaLaOrden(rs.getString("chq_a_la_orden"));
            formaPago.setCheque(cheque);
        }
        return formaPago;
    }

    private OrdenPago mapearOrdenPagoLiviana(ResultSet rs, long idOrdenPago) throws SQLException {
        OrdenPago ordenPago = new OrdenPago();
        ordenPago.setIdOrdenPago(idOrdenPago);
        ordenPago.setNumero(rs.getInt("ord_pag_numero"));
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(rs.getLong("id_proveedor"));
        proveedor.setRazonSocial(rs.getString("prov_razon_social"));
        ordenPago.setProveedor(proveedor);
        return ordenPago;
    }
}
