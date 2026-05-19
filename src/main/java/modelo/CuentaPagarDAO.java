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
}
