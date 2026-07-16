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

    public CuentaPagar getCuentaPagar(Long idCuentaPagar, Long idFacturaCompra) throws SQLException {
        if (idCuentaPagar == null || idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return null;
        }
        CuentaPagar cuentaPagar = null;
        String sql = "SELECT id_cta_pagar, id_fact_comp_cab, cta_pag_monto, cta_pag_estado, " +
                    "cta_pag_fecha_venci, cta_pag_saldo FROM cuenta_pagar " +
                    "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";

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
                    "cta_pag_fecha_venci, cta_pag_saldo) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cuentaPagar.getFacturaCompra().getIdFacturaCompra());
            stmt.setLong(2, cuentaPagar.getMonto());
            stmt.setString(3, cuentaPagar.getEstado());
            stmt.setDate(4, new java.sql.Date(cuentaPagar.getFechaVencimiento().getTime()));
            stmt.setLong(5, cuentaPagar.getSaldo());

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
                    "cta_pag_fecha_venci = ?, cta_pag_saldo = ? " +
                    "WHERE id_cta_pagar = ? AND id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cuentaPagar.getMonto());
            stmt.setString(2, cuentaPagar.getEstado());
            stmt.setDate(3, new java.sql.Date(cuentaPagar.getFechaVencimiento().getTime()));
            stmt.setLong(4, cuentaPagar.getSaldo());
            stmt.setLong(5, cuentaPagar.getIdCuentaPagar());
            stmt.setLong(6, cuentaPagar.getFacturaCompra().getIdFacturaCompra());

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
     * Obtiene la cuenta a pagar asociada a una factura de compra.
     * Asume una única cuenta a pagar por factura.
     */
    public CuentaPagar getByFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return null;
        }

        String sql = "SELECT id_cta_pagar, id_fact_comp_cab, cta_pag_monto, cta_pag_estado, " +
                    "cta_pag_fecha_venci, cta_pag_saldo FROM cuenta_pagar " +
                    "WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CuentaPagar cp = new CuentaPagar();
                    cp.setIdCuentaPagar(rs.getLong("id_cta_pagar"));
                    cp.setFacturaCompra(new FacturaCompra(rs.getLong("id_fact_comp_cab")));
                    cp.setMonto(rs.getLong("cta_pag_monto"));
                    cp.setEstado(rs.getString("cta_pag_estado"));
                    cp.setFechaVencimiento(rs.getDate("cta_pag_fecha_venci"));
                    cp.setSaldo(rs.getLong("cta_pag_saldo"));
                    return cp;
                }
            }
        }
        return null;
    }

    /**
     * Cambia el estado a 'Anulado' de la cuenta a pagar asociada a una factura.
     * No elimina el registro, para preservar trazabilidad.
     */
    public void anularPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaCompra es nulo");
            return;
        }

        String sql = "UPDATE cuenta_pagar SET cta_pag_estado = 'Anulado' WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            stmt.executeUpdate();
        }
    }

    // ==================== AJUSTE POR NOTA DE CRÉDITO / DÉBITO ====================
    // Enfoque 1 con neteo en la provisión — ver NOTA_CREDITO_DEBITO_PLAN.md §4/§8.

    private static final String ESTADO_PENDIENTE   = "Pendiente";
    private static final String ESTADO_CANCELADO   = "Cancelado";
    private static final String ESTADO_SALDO_FAVOR = "Saldo a favor";
    private static final String ESTADO_ANULADO     = "Anulado";

    public enum TipoNota { CREDITO, DEBITO }

    /**
     * Aplica (o reversa) el efecto de una Nota de Crédito/Débito sobre el saldo de la
     * cuenta a pagar de una factura.
     * Enfoque 1 con neteo en la provisión — ver NOTA_CREDITO_DEBITO_PLAN.md §4/§8.
     *
     * El saldo PUEDE quedar negativo (= saldo a favor); NO se bloquea NC &gt; saldo:
     * ese negativo se netea después en la provisión del proveedor.
     * Estado recalculado: saldo &gt; 0 -&gt; Pendiente | = 0 -&gt; Cancelado | &lt; 0 -&gt; Saldo a favor.
     *
     * Corre sobre la Connection compartida; NO hace commit/rollback (lo controla el
     * Service/Servlet que abrió la transacción, igual que la sincronización de Factura de Compra).
     *
     * @param idFacturaCompra factura referenciada por la nota (1 cuenta a pagar por factura)
     * @param montoNota       monto de la nota, SIEMPRE positivo
     * @param tipo            CREDITO (resta al saldo) o DEBITO (suma al saldo)
     * @param reversa         false = aplicar la nota | true = revertir (anulación de la nota)
     * @return nuevo saldo de la cuenta a pagar tras el ajuste
     * @throws SQLException si la factura no tiene cuenta a pagar gestionable o hay error de BD
     */
    public long ajustarSaldoPorNota(Long idFacturaCompra, long montoNota,
                                    TipoNota tipo, boolean reversa) throws SQLException {

        if (idFacturaCompra == null) {
            throw new SQLException("ajustarSaldoPorNota: idFacturaCompra es nulo");
        }
        if (montoNota <= 0) {
            throw new SQLException("ajustarSaldoPorNota: el monto de la nota debe ser > 0 "
                    + "(recibido: " + montoNota + ")");
        }

        // Signo: NC resta, ND suma. La reversa (anulación) invierte el signo.
        long delta = (tipo == TipoNota.CREDITO) ? -montoNota : montoNota;
        if (reversa) {
            delta = -delta;
        }

        // UPDATE atómico + recálculo de estado en un solo statement (sin read-modify-write).
        // En SQL, la cláusula SET y el CASE leen el valor VIEJO de la columna -> correcto.
        // WHERE excluye cuentas anuladas: una factura anulada no ajusta saldo.
        // RETURNING (PostgreSQL) devuelve el saldo ya actualizado en el mismo round-trip.
        String sql =
            "UPDATE cuenta_pagar " +
            "   SET cta_pag_saldo  = cta_pag_saldo + ?, " +
            "       cta_pag_estado = CASE " +
            "                           WHEN cta_pag_saldo + ? > 0 THEN ? " +
            "                           WHEN cta_pag_saldo + ? = 0 THEN ? " +
            "                           ELSE ? " +
            "                        END " +
            " WHERE id_fact_comp_cab = ? " +
            "   AND cta_pag_estado <> ? " +
            "RETURNING cta_pag_saldo";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, delta);
            stmt.setLong(2, delta);
            stmt.setString(3, ESTADO_PENDIENTE);
            stmt.setLong(4, delta);
            stmt.setString(5, ESTADO_CANCELADO);
            stmt.setString(6, ESTADO_SALDO_FAVOR);
            stmt.setLong(7, idFacturaCompra);
            stmt.setString(8, ESTADO_ANULADO);

            try (ResultSet rs = stmt.executeQuery()) {   // RETURNING -> executeQuery()
                if (rs.next()) {
                    long nuevoSaldo = rs.getLong("cta_pag_saldo");
                    LOGGER.log(Level.INFO,
                        "Cuenta a pagar de factura {0} ajustada por nota (delta={1}) -> saldo={2}",
                        new Object[]{idFacturaCompra, delta, nuevoSaldo});
                    return nuevoSaldo;
                }
                // 0 filas: la factura no tiene cuenta a pagar, o está Anulada.
                throw new SQLException("No se pudo ajustar: la factura " + idFacturaCompra
                        + " no tiene cuenta a pagar gestionable (inexistente o anulada).");
            }
        }
    }
}
