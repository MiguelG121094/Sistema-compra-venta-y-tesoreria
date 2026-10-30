/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class CuentaPagarDAO {

    private Connection conn;
    private FacturaCompraDAO facturaCompraDAO;
    private static final Logger LOGGER = Logger.getLogger(CuentaPagarDAO.class.getName());

    public CuentaPagarDAO(Connection conn) {
        this.conn = conn;
    }

    // ==================== ORIGEN DE LA FILA ====================
    // Una fila por comprobante: la factura y cada nota de crédito/débito tienen la suya, todas con
    // el id_fact_comp_cab de la factura. Ver NOTA_CREDITO_DEBITO_PLAN.md §4.2.

    public static final String ORIGEN_FACTURA = "FACTURA";
    public static final String ORIGEN_NOTA_CREDITO = "NOTA_CRED";
    public static final String ORIGEN_NOTA_DEBITO = "NOTA_DEBI";

    /** Columnas del comprobante; van con {@link #JOIN_COMPROBANTE} y la tabla con alias cp. */
    static final String COLUMNAS_COMPROBANTE =
            "cp.cta_pag_origen, cp.id_nota_cred_comp_cab, cp.id_nota_debi_comp_cab, "
          + "ncc.nota_cred_comp_numero, ndc.nota_debi_comp_numero ";

    static final String JOIN_COMPROBANTE =
            "LEFT JOIN nota_credito_compra_cabecera ncc ON cp.id_nota_cred_comp_cab = ncc.id_nota_cred_comp_cab "
          + "LEFT JOIN nota_debito_compra_cabecera ndc ON cp.id_nota_debi_comp_cab = ndc.id_nota_debi_comp_cab ";

    /** Completa el origen y la nota de la fila (solo id y número, lo que muestran los listados). */
    static void leerComprobante(ResultSet rs, CuentaPagar cp) throws SQLException {
        cp.setOrigen(rs.getString("cta_pag_origen"));
        long idNotaCredito = rs.getLong("id_nota_cred_comp_cab");
        if (!rs.wasNull()) {
            NotaCreditoCompra nc = new NotaCreditoCompra(idNotaCredito);
            nc.setNumero(rs.getString("nota_cred_comp_numero"));
            cp.setNotaCredito(nc);
        }
        long idNotaDebito = rs.getLong("id_nota_debi_comp_cab");
        if (!rs.wasNull()) {
            NotaDebitoCompra nd = new NotaDebitoCompra(idNotaDebito);
            nd.setNumero(rs.getString("nota_debi_comp_numero"));
            cp.setNotaDebito(nd);
        }
    }

    public CuentaPagar getCuentaPagar(Long idCuentaPagar, Long idFacturaCompra) throws SQLException {
        if (idCuentaPagar == null || idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return null;
        }
        CuentaPagar cuentaPagar = null;
        String sql = "SELECT cp.id_cta_pagar, cp.id_fact_comp_cab, cp.cta_pag_monto, cp.cta_pag_estado, " +
                    "cp.cta_pag_fecha_venci, cp.cta_pag_saldo, " + COLUMNAS_COMPROBANTE +
                    "FROM cuenta_pagar cp " + JOIN_COMPROBANTE +
                    "WHERE cp.id_cta_pagar = ? AND cp.id_fact_comp_cab = ?";

        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuentaPagar);
            stmt.setLong(2, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cuentaPagar = new CuentaPagar(
                        rs.getLong("id_cta_pagar"),
                        facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                        rs.getLong("cta_pag_monto"),
                        rs.getString("cta_pag_estado"),
                        rs.getDate("cta_pag_fecha_venci"),
                        rs.getLong("cta_pag_saldo")
                    );
                    leerComprobante(rs, cuentaPagar);
                }
            }
        }
        return cuentaPagar;
    }

    public List<CuentaPagar> listarCuentasPagar() throws SQLException {
        List<CuentaPagar> cuentas = new ArrayList<>();
        String sql = "SELECT id_cta_pagar, id_fact_comp_cab, cta_pag_monto, cta_pag_estado, " +
                    "cta_pag_fecha_venci, cta_pag_saldo FROM cuenta_pagar";

        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CuentaPagar cuentaPagar = new CuentaPagar(
                    rs.getLong("id_cta_pagar"),
                    facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                    rs.getLong("cta_pag_monto"),
                    rs.getString("cta_pag_estado"),
                    rs.getDate("cta_pag_fecha_venci"),
                    rs.getLong("cta_pag_saldo")
                );
                cuentas.add(cuentaPagar);
            }
        }
        return cuentas;
    }

    public List<CuentaPagar> listarCuentasPagarPendientes() throws SQLException {
        List<CuentaPagar> cuentas = new ArrayList<>();
        String sql = "SELECT id_cta_pagar, id_fact_comp_cab, cta_pag_monto, cta_pag_estado, " +
                    "cta_pag_fecha_venci, cta_pag_saldo FROM cuenta_pagar WHERE cta_pag_saldo > 0";

        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CuentaPagar cuentaPagar = new CuentaPagar(
                    rs.getLong("id_cta_pagar"),
                    facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                    rs.getLong("cta_pag_monto"),
                    rs.getString("cta_pag_estado"),
                    rs.getDate("cta_pag_fecha_venci"),
                    rs.getLong("cta_pag_saldo")
                );
                cuentas.add(cuentaPagar);
            }
        }
        return cuentas;
    }

    public Long insertarCuentaPagar(CuentaPagar cuentaPagar) throws SQLException {
        if (cuentaPagar == null) {
            LOGGER.log(Level.SEVERE, "Error: La cuenta a pagar es nula");
            return null;
        }

        String sql = "INSERT INTO cuenta_pagar (id_fact_comp_cab, cta_pag_monto, cta_pag_estado, " +
                    "cta_pag_fecha_venci, cta_pag_saldo, cta_pag_plazo, cta_pag_origen) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cuentaPagar.getFacturaCompra().getIdFacturaCompra());
            stmt.setLong(2, cuentaPagar.getMonto());
            stmt.setString(3, cuentaPagar.getEstado());
            stmt.setDate(4, new java.sql.Date(cuentaPagar.getFechaVencimiento().getTime()));
            stmt.setLong(5, cuentaPagar.getSaldo());
            if (cuentaPagar.getPlazo() != null) {
                stmt.setLong(6, cuentaPagar.getPlazo());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            // Las filas de nota van por insertarCuentaPagarNota: acá siempre es la de la factura.
            stmt.setString(7, cuentaPagar.getOrigen() != null ? cuentaPagar.getOrigen() : ORIGEN_FACTURA);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la cuenta a pagar, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    cuentaPagar.setIdCuentaPagar(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la cuenta a pagar.");
                }
            }
        }
    }

    public void actualizarCuentaPagar(CuentaPagar cuentaPagar) throws SQLException {
        if (cuentaPagar == null || cuentaPagar.getIdCuentaPagar() == null) {
            LOGGER.log(Level.WARNING, "Error: cuenta a pagar es nula");
            return;
        }

        String sql = "UPDATE cuenta_pagar SET cta_pag_monto = ?, cta_pag_estado = ?, " +
                    "cta_pag_fecha_venci = ?, cta_pag_saldo = ?, cta_pag_plazo = ? " +
                    "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cuentaPagar.getMonto());
            stmt.setString(2, cuentaPagar.getEstado());
            stmt.setDate(3, new java.sql.Date(cuentaPagar.getFechaVencimiento().getTime()));
            stmt.setLong(4, cuentaPagar.getSaldo());
            if (cuentaPagar.getPlazo() != null) {
                stmt.setLong(5, cuentaPagar.getPlazo());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setLong(6, cuentaPagar.getIdCuentaPagar());
            stmt.setLong(7, cuentaPagar.getFacturaCompra().getIdFacturaCompra());

            stmt.executeUpdate();
        }
    }

    public void eliminarCuentaPagar(Long idCuentaPagar, Long idFacturaCompra) throws SQLException {
        if (idCuentaPagar == null || idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return;
        }

        String sql = "DELETE FROM cuenta_pagar WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuentaPagar);
            stmt.setLong(2, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina las cuentas a pagar asociadas a una factura de compra.
     *
     * @param idFacturaCompra ID de la factura de compra
     * @throws SQLException si ocurre un error de base de datos
     */
    public void eliminarPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return;
        }

        String sql = "DELETE FROM cuenta_pagar WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    /**
     * Obtiene la cuenta a pagar de la propia factura (origen FACTURA). Las de sus notas tienen el
     * mismo id_fact_comp_cab y quedan afuera.
     */
    public CuentaPagar getByFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return null;
        }

        String sql = "SELECT id_cta_pagar, id_fact_comp_cab, cta_pag_monto, cta_pag_estado, " +
                    "cta_pag_fecha_venci, cta_pag_saldo FROM cuenta_pagar " +
                    "WHERE id_fact_comp_cab = ? AND cta_pag_origen = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.setString(2, ORIGEN_FACTURA);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CuentaPagar cp = new CuentaPagar();
                    cp.setIdCuentaPagar(rs.getLong("id_cta_pagar"));
                    cp.setFacturaCompra(new FacturaCompra(rs.getLong("id_fact_comp_cab")));
                    cp.setMonto(rs.getLong("cta_pag_monto"));
                    cp.setEstado(rs.getString("cta_pag_estado"));
                    cp.setFechaVencimiento(rs.getDate("cta_pag_fecha_venci"));
                    cp.setSaldo(rs.getLong("cta_pag_saldo"));
                    cp.setOrigen(ORIGEN_FACTURA);
                    return cp;
                }
            }
        }
        return null;
    }

    /**
     * Cambia el estado a 'Anulado' de la cuenta a pagar de la propia factura (no las de sus notas,
     * que se anulan con la nota). No elimina el registro, para preservar trazabilidad.
     */
    public void anularPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return;
        }

        String sql = "UPDATE cuenta_pagar SET cta_pag_estado = 'Anulado' "
                   + "WHERE id_fact_comp_cab = ? AND cta_pag_origen = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.setString(2, ORIGEN_FACTURA);
            stmt.executeUpdate();
        }
    }

    // ==================== FILA PROPIA DE LA NOTA DE CRÉDITO / DÉBITO ====================
    // Cada nota tiene su fila; la de la factura no se toca. Ver NOTA_CREDITO_DEBITO_PLAN.md §4.

    private static final String ESTADO_PENDIENTE   = "Pendiente";
    private static final String ESTADO_CANCELADO   = "Cancelado";
    private static final String ESTADO_SALDO_FAVOR = "Saldo a favor";
    private static final String ESTADO_ANULADO     = "Anulado";
    private static final String ESTADO_EN_PROVISION = "En provision";
    private static final String ESTADO_RENDIDA     = "Rendida";

    /**
     * Inserta la cuenta a pagar de una nota. Lleva el id de la factura que corrige (así entra en
     * la provisión del mismo proveedor), monto y saldo con el signo de la nota y vence en su fecha
     * de emisión. El estado sale del saldo: la NC nace 'Saldo a favor' y la ND 'Pendiente'.
     *
     * <p>Corre sobre la Connection compartida; la transacción la controla el Service de la nota.
     *
     * @param monto total de la nota con signo, el mismo de su fila de libro IVA (NC &lt; 0, ND &gt; 0)
     */
    public Long insertarCuentaPagarNota(Long idFacturaCompra, String origen, Long idNota,
            long monto, java.util.Date fechaEmision) throws SQLException {
        if (idFacturaCompra == null || idNota == null || fechaEmision == null) {
            throw new SQLException("insertarCuentaPagarNota: parámetros nulos");
        }
        boolean esCredito = ORIGEN_NOTA_CREDITO.equals(origen);
        if (!esCredito && !ORIGEN_NOTA_DEBITO.equals(origen)) {
            throw new SQLException("insertarCuentaPagarNota: origen inválido " + origen);
        }
        if (esCredito ? monto >= 0 : monto <= 0) {
            throw new SQLException("El total de la nota no tiene el signo esperado (" + monto + ")");
        }

        String sql = "INSERT INTO cuenta_pagar (id_fact_comp_cab, cta_pag_monto, cta_pag_estado, "
                   + "cta_pag_fecha_venci, cta_pag_saldo, cta_pag_plazo, cta_pag_origen, "
                   + "id_nota_cred_comp_cab, id_nota_debi_comp_cab) "
                   + "VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.setLong(2, monto);
            stmt.setString(3, calcularEstadoPorSaldo(monto));
            stmt.setDate(4, new java.sql.Date(fechaEmision.getTime()));
            stmt.setLong(5, monto);
            stmt.setString(6, origen);
            if (esCredito) {
                stmt.setLong(7, idNota);
                stmt.setNull(8, java.sql.Types.INTEGER);
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
                stmt.setLong(8, idNota);
            }
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("No se generó el id de la cuenta a pagar de la nota");
    }

    /**
     * Anula la cuenta a pagar de una nota, siempre que no haya entrado al circuito de pago: en
     * provisión, en una rendición o con el saldo ya aplicado en una OP se rechaza con un mensaje
     * para el usuario. Bloquea la fila con FOR UPDATE para que una provisión no la tome mientras
     * tanto. Ver NOTA_CREDITO_DEBITO_PLAN.md §4.4.
     */
    public void anularCuentaPagarNota(String origen, Long idNota) throws SQLException {
        String columnaNota;
        if (ORIGEN_NOTA_CREDITO.equals(origen)) {
            columnaNota = "id_nota_cred_comp_cab";
        } else if (ORIGEN_NOTA_DEBITO.equals(origen)) {
            columnaNota = "id_nota_debi_comp_cab";
        } else {
            throw new SQLException("anularCuentaPagarNota: origen inválido " + origen);
        }

        Long idCtaPagar = null;
        Long idFactura = null;
        long monto = 0;
        long saldo = 0;
        String estado = null;
        String sqlSelect = "SELECT id_cta_pagar, id_fact_comp_cab, cta_pag_monto, cta_pag_saldo, "
                         + "cta_pag_estado FROM cuenta_pagar WHERE " + columnaNota + " = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setLong(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    idCtaPagar = rs.getLong("id_cta_pagar");
                    idFactura = rs.getLong("id_fact_comp_cab");
                    monto = rs.getLong("cta_pag_monto");
                    saldo = rs.getLong("cta_pag_saldo");
                    estado = rs.getString("cta_pag_estado");
                }
            }
        }

        if (idCtaPagar == null) {
            throw new SQLException("La nota no tiene cuenta a pagar. Verifique que se haya corrido "
                    + "la migración de cuenta a pagar por comprobante.");
        }
        if (ESTADO_ANULADO.equals(estado)) {
            return;
        }
        if (ESTADO_EN_PROVISION.equals(estado)) {
            throw new SQLException("No se puede anular: la nota está en una provisión. "
                    + "Anule primero la provisión.");
        }
        if (ESTADO_RENDIDA.equals(estado)) {
            throw new SQLException("No se puede anular: la nota está en una rendición de fondo fijo. "
                    + "Anule primero la rendición.");
        }
        if (saldo != monto) {
            throw new SQLException("No se puede anular: la nota ya se aplicó en una orden de pago. "
                    + "Anule primero la orden de pago y su provisión.");
        }

        String sqlUpdate = "UPDATE cuenta_pagar SET cta_pag_estado = ? "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, ESTADO_ANULADO);
            stmt.setLong(2, idCtaPagar);
            stmt.setLong(3, idFactura);
            stmt.executeUpdate();
        }
    }

    /**
     * Indica si la cuenta a pagar de la propia factura está en alguna provisión u orden de pago.
     * Las filas de sus notas no cuentan: tienen su propio circuito. Reemplaza la heurística
     * saldo &lt; monto. Ver NOTA_CREDITO_DEBITO_PLAN.md §8.4.
     */
    public boolean tienePagosAplicados(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            return false;
        }
        String sql =
            "SELECT (EXISTS (SELECT 1 FROM orden_pago_detalle d " +
            "                JOIN cuenta_pagar cp ON d.id_cta_pagar = cp.id_cta_pagar " +
            "                 AND d.id_fact_comp_cab = cp.id_fact_comp_cab " +
            "                WHERE d.id_fact_comp_cab = ? AND cp.cta_pag_origen = ?) " +
            "     OR EXISTS (SELECT 1 FROM provision_cuenta_pagar_detalle d " +
            "                JOIN cuenta_pagar cp ON d.id_cta_pagar = cp.id_cta_pagar " +
            "                 AND d.id_fact_comp_cab = cp.id_fact_comp_cab " +
            "                WHERE d.id_fact_comp_cab = ? AND cp.cta_pag_origen = ?))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.setString(2, ORIGEN_FACTURA);
            stmt.setLong(3, idFacturaCompra);
            stmt.setString(4, ORIGEN_FACTURA);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    // ==================== PROVISIÓN DE CUENTA A PAGAR ====================

    /**
     * Cuentas a pagar de un proveedor disponibles para provisionar: saldo != 0 (incluye
     * saldos NEGATIVOS = saldo a favor de NC, para el neteo), y estado provisionable
     * (excluye 'En provision' y 'Anulado'). Trae la factura (numero) y el plazo.
     * Ver MODULO_TESORERIA_PLAN.md §B y NOTA_CREDITO_DEBITO_PLAN.md §8.3.
     */
    public List<CuentaPagar> listarCuentasPagarPorProveedor(Long idProveedor) throws SQLException {
        List<CuentaPagar> cuentas = new ArrayList<>();
        if (idProveedor == null) {
            return cuentas;
        }
        String sql = "SELECT cp.id_cta_pagar, cp.id_fact_comp_cab, cp.cta_pag_monto, cp.cta_pag_estado, "
                   + "cp.cta_pag_fecha_venci, cp.cta_pag_saldo, cp.cta_pag_plazo, "
                   + "f.fact_comp_numero, f.fact_comp_fecha_emision, " + COLUMNAS_COMPROBANTE
                   + "FROM cuenta_pagar cp "
                   + "JOIN factura_compra_cabecera f ON cp.id_fact_comp_cab = f.id_fact_comp_cab "
                   + JOIN_COMPROBANTE
                   + "WHERE f.id_proveedor = ? AND cp.cta_pag_saldo <> 0 "
                   // Las rendidas se provisionan desde la rendicion, a nombre del responsable: si
                   // tambien aparecieran aca se podrian pagar dos veces, una a cada uno.
                   + "AND cp.cta_pag_estado NOT IN ('En provision', 'Anulado', 'Rendida') "
                   // Las de fondo fijo se le reponen al responsable y entran a la provision por la
                   // rendicion, nunca por el proveedor: al comercio ya le pago el responsable.
                   + "AND f.fact_comp_tipo_factura <> 'fondoFijo' "
                   + "ORDER BY cp.id_cta_pagar";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProveedor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FacturaCompra fc = new FacturaCompra(rs.getLong("id_fact_comp_cab"));
                    fc.setNumero(rs.getString("fact_comp_numero"));
                    fc.setFechaEmision(rs.getDate("fact_comp_fecha_emision"));

                    CuentaPagar cp = new CuentaPagar();
                    cp.setIdCuentaPagar(rs.getLong("id_cta_pagar"));
                    cp.setFacturaCompra(fc);
                    cp.setMonto(rs.getLong("cta_pag_monto"));
                    cp.setEstado(rs.getString("cta_pag_estado"));
                    cp.setFechaVencimiento(rs.getDate("cta_pag_fecha_venci"));
                    cp.setSaldo(rs.getLong("cta_pag_saldo"));
                    long plazo = rs.getLong("cta_pag_plazo");
                    cp.setPlazo(rs.wasNull() ? null : plazo);
                    leerComprobante(rs, cp);
                    cuentas.add(cp);
                }
            }
        }
        return cuentas;
    }

    /**
     * Reserva la cuenta a pagar al provisionarla (estado 'En provision'), sin tocar el saldo.
     * El saldo se descuenta recién en la Orden de Pago.
     */
    public void marcarEnProvision(Long idCtaPagar, Long idFacturaCompra) throws SQLException {
        String sql = "UPDATE cuenta_pagar SET cta_pag_estado = 'En provision' "
                   + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCtaPagar);
            stmt.setLong(2, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    /**
     * Revierte la reserva (al anular la provisión): recalcula el estado desde el saldo,
     * solo si estaba 'En provision'. La lógica del estado se resuelve en Java, no en la consulta.
     */
    public void revertirProvision(Long idCtaPagar, Long idFacturaCompra) throws SQLException {
        revertirProvision(idCtaPagar, idFacturaCompra, false);
    }

    /**
     * Igual que la anterior, pero si la provisión venía de una rendición de fondo fijo la cuenta
     * vuelve a 'Rendida' y no al estado por saldo: la factura sigue rendida, lo que se deshizo es
     * la provisión. Ver MODULO_TESORERIA_PLAN.md §E.1.3.
     */
    public void revertirProvision(Long idCtaPagar, Long idFacturaCompra, boolean veniaDeRendicion)
            throws SQLException {
        // 1. Leer saldo y estado actual
        Long saldo = null;
        String estadoActual = null;
        String sqlSelect = "SELECT cta_pag_saldo, cta_pag_estado FROM cuenta_pagar "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setLong(1, idCtaPagar);
            stmt.setLong(2, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    saldo = rs.getLong("cta_pag_saldo");
                    estadoActual = rs.getString("cta_pag_estado");
                }
            }
        }

        // 2. Solo revierte si la cuenta estaba reservada por la provisión
        if (saldo == null || !ESTADO_EN_PROVISION.equals(estadoActual)) {
            return;
        }

        // 3. Calcular el estado en Java y actualizar
        String nuevoEstado = veniaDeRendicion ? ESTADO_RENDIDA : calcularEstadoPorSaldo(saldo);
        String sqlUpdate = "UPDATE cuenta_pagar SET cta_pag_estado = ? "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, nuevoEstado);
            stmt.setLong(2, idCtaPagar);
            stmt.setLong(3, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    // ==================== RENDICIÓN DE FONDO FIJO ====================

    /**
     * Cuentas a pagar de facturas de fondo fijo que todavia se pueden rendir. Se excluyen las ya
     * rendidas para que la misma factura no entre en dos rendiciones, y se reusa el criterio de la
     * provision para las provisionadas y anuladas. Ver MODULO_TESORERIA_PLAN.md §E.
     */
    public List<CuentaPagar> listarCuentasPagarFondoFijo() throws SQLException {
        List<CuentaPagar> cuentas = new ArrayList<>();
        String sql = "SELECT cp.id_cta_pagar, cp.id_fact_comp_cab, cp.cta_pag_monto, cp.cta_pag_estado, "
                   + "cp.cta_pag_fecha_venci, cp.cta_pag_saldo, cp.cta_pag_plazo, "
                   + "f.fact_comp_numero, f.fact_comp_fecha_emision, f.id_proveedor, " + COLUMNAS_COMPROBANTE
                   + "FROM cuenta_pagar cp "
                   + "JOIN factura_compra_cabecera f ON cp.id_fact_comp_cab = f.id_fact_comp_cab "
                   + JOIN_COMPROBANTE
                   + "WHERE f.fact_comp_tipo_factura = 'fondoFijo' AND cp.cta_pag_saldo <> 0 "
                   + "AND cp.cta_pag_estado NOT IN ('En provision', 'Anulado', 'Rendida') "
                   + "ORDER BY cp.id_cta_pagar";

        ProveedorDAO proveedorDAO = new ProveedorDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                FacturaCompra fc = new FacturaCompra(rs.getLong("id_fact_comp_cab"));
                fc.setNumero(rs.getString("fact_comp_numero"));
                fc.setFechaEmision(rs.getDate("fact_comp_fecha_emision"));
                fc.setProveedor(proveedorDAO.getProveedor(rs.getLong("id_proveedor")));

                CuentaPagar cp = new CuentaPagar();
                cp.setIdCuentaPagar(rs.getLong("id_cta_pagar"));
                cp.setFacturaCompra(fc);
                cp.setMonto(rs.getLong("cta_pag_monto"));
                cp.setEstado(rs.getString("cta_pag_estado"));
                cp.setFechaVencimiento(rs.getDate("cta_pag_fecha_venci"));
                cp.setSaldo(rs.getLong("cta_pag_saldo"));
                long plazo = rs.getLong("cta_pag_plazo");
                cp.setPlazo(rs.wasNull() ? null : plazo);
                leerComprobante(rs, cp);
                cuentas.add(cp);
            }
        }
        return cuentas;
    }

    /**
     * Marca la cuenta como rendida, sin tocar el saldo: la rendicion solo agrupa las facturas del
     * fondo fijo, el pago lo sigue haciendo la orden de pago. Bloquea la fila con FOR UPDATE y exige
     * que este disponible, de modo que dos rendiciones simultaneas no puedan tomar la misma factura.
     */
    public void marcarRendida(Long idCtaPagar, Long idFacturaCompra) throws SQLException {
        String estadoActual = null;
        String sqlSelect = "SELECT cta_pag_estado FROM cuenta_pagar "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setLong(1, idCtaPagar);
            stmt.setLong(2, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    estadoActual = rs.getString("cta_pag_estado");
                }
            }
        }
        if (estadoActual == null) {
            throw new SQLException("La cuenta a pagar " + idCtaPagar + " no existe");
        }
        if (ESTADO_RENDIDA.equals(estadoActual)) {
            throw new SQLException("La factura ya fue rendida en otra rendición");
        }
        if (ESTADO_EN_PROVISION.equals(estadoActual) || ESTADO_ANULADO.equals(estadoActual)) {
            throw new SQLException("La cuenta a pagar está '" + estadoActual + "' y no se puede rendir");
        }

        String sqlUpdate = "UPDATE cuenta_pagar SET cta_pag_estado = ? "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, ESTADO_RENDIDA);
            stmt.setLong(2, idCtaPagar);
            stmt.setLong(3, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    /**
     * Revierte la marca al anular la rendicion: recalcula el estado desde el saldo, solo si la
     * cuenta seguia 'Rendida'. Mismo criterio que revertirProvision.
     */
    public void revertirRendicion(Long idCtaPagar, Long idFacturaCompra) throws SQLException {
        Long saldo = null;
        String estadoActual = null;
        String sqlSelect = "SELECT cta_pag_saldo, cta_pag_estado FROM cuenta_pagar "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setLong(1, idCtaPagar);
            stmt.setLong(2, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    saldo = rs.getLong("cta_pag_saldo");
                    estadoActual = rs.getString("cta_pag_estado");
                }
            }
        }
        if (saldo == null || !ESTADO_RENDIDA.equals(estadoActual)) {
            return;
        }
        String nuevoEstado = calcularEstadoPorSaldo(saldo);
        String sqlUpdate = "UPDATE cuenta_pagar SET cta_pag_estado = ? "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, nuevoEstado);
            stmt.setLong(2, idCtaPagar);
            stmt.setLong(3, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    // ==================== ORDEN DE PAGO ====================

    /**
     * Descuenta el importe pagado del saldo de una cuenta a pagar al generar la Orden de Pago:
     * read-modify-write con FOR UPDATE (evita lost-updates; siempre corre dentro de la tx de la OP)
     * y recalcula el estado en Java. La cuenta que estaba 'En provision' pasa a
     * Cancelado/Pendiente/Saldo a favor según el saldo resultante. Ver MODULO_TESORERIA_PLAN.md §C.
     */
    public long descontarSaldo(Long idCtaPagar, Long idFacturaCompra, long importe) throws SQLException {
        if (idCtaPagar == null || idFacturaCompra == null) {
            throw new SQLException("descontarSaldo: parámetros nulos");
        }

        // 1. Leer el saldo actual bloqueando la fila hasta el commit.
        Long saldoActual = null;
        String sqlSelect = "SELECT cta_pag_saldo FROM cuenta_pagar "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setLong(1, idCtaPagar);
            stmt.setLong(2, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    saldoActual = rs.getLong("cta_pag_saldo");
                }
            }
        }
        if (saldoActual == null) {
            throw new SQLException("descontarSaldo: la cuenta a pagar (" + idCtaPagar + ", "
                    + idFacturaCompra + ") no existe.");
        }

        // 2. Calcular el nuevo saldo y el estado en Java.
        long nuevoSaldo = saldoActual - importe;
        String nuevoEstado = calcularEstadoPorSaldo(nuevoSaldo);

        // 3. Actualizar con los valores ya resueltos.
        String sqlUpdate = "UPDATE cuenta_pagar SET cta_pag_saldo = ?, cta_pag_estado = ? "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setLong(1, nuevoSaldo);
            stmt.setString(2, nuevoEstado);
            stmt.setLong(3, idCtaPagar);
            stmt.setLong(4, idFacturaCompra);
            stmt.executeUpdate();
        }

        LOGGER.log(Level.INFO,
            "Cuenta a pagar ({0},{1}) descontada por OP (importe={2}) -> saldo={3}",
            new Object[]{idCtaPagar, idFacturaCompra, importe, nuevoSaldo});
        return nuevoSaldo;
    }

    /**
     * Reversa exacta de {@link #descontarSaldo}: al anular la Orden de Pago devuelve el importe
     * pagado al saldo de la cuenta a pagar (saldo += importe) y la deja nuevamente reservada por
     * la provisión ('En provision'), ya que al anular la OP su provisión vuelve a 'Pendiente'.
     * Read-modify-write con FOR UPDATE (siempre corre dentro de la tx de la anulación).
     * Ver MODULO_TESORERIA_PLAN.md §C (anulación de la OP).
     */
    public long restaurarSaldoPorAnulacionOP(Long idCtaPagar, Long idFacturaCompra, long importe)
            throws SQLException {
        if (idCtaPagar == null || idFacturaCompra == null) {
            throw new SQLException("restaurarSaldoPorAnulacionOP: parámetros nulos");
        }

        // 1. Leer el saldo actual bloqueando la fila hasta el commit.
        Long saldoActual = null;
        String sqlSelect = "SELECT cta_pag_saldo FROM cuenta_pagar "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setLong(1, idCtaPagar);
            stmt.setLong(2, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    saldoActual = rs.getLong("cta_pag_saldo");
                }
            }
        }
        if (saldoActual == null) {
            throw new SQLException("restaurarSaldoPorAnulacionOP: la cuenta a pagar (" + idCtaPagar
                    + ", " + idFacturaCompra + ") no existe.");
        }

        // 2. Devolver el importe y volver a reservarla por la provisión (que se reactiva).
        long nuevoSaldo = saldoActual + importe;
        String sqlUpdate = "UPDATE cuenta_pagar SET cta_pag_saldo = ?, cta_pag_estado = ? "
                         + "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setLong(1, nuevoSaldo);
            stmt.setString(2, ESTADO_EN_PROVISION);
            stmt.setLong(3, idCtaPagar);
            stmt.setLong(4, idFacturaCompra);
            stmt.executeUpdate();
        }

        LOGGER.log(Level.INFO,
            "Cuenta a pagar ({0},{1}) restaurada por anulación de OP (importe={2}) -> saldo={3}, estado={4}",
            new Object[]{idCtaPagar, idFacturaCompra, importe, nuevoSaldo, ESTADO_EN_PROVISION});
        return nuevoSaldo;
    }

    /**
     * Estado de la cuenta a pagar según el saldo (lógica de negocio en Java):
     * saldo &gt; 0 -&gt; Pendiente | = 0 -&gt; Cancelado | &lt; 0 -&gt; Saldo a favor.
     */
    private String calcularEstadoPorSaldo(long saldo) {
        if (saldo > 0) {
            return ESTADO_PENDIENTE;
        }
        if (saldo == 0) {
            return ESTADO_CANCELADO;
        }
        return ESTADO_SALDO_FAVOR;
    }
}
