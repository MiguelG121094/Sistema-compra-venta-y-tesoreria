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
public class CuentaCobrarDAO {

    private Connection conn;
    private FacturaVentaDAO facturaVentaDAO;
    private static final Logger LOGGER = Logger.getLogger(CuentaCobrarDAO.class.getName());

    public CuentaCobrarDAO(Connection conn) {
        this.conn = conn;
    }

    public CuentaCobrar getCuentaCobrar(Long idCuentaCobrar, Long idFacturaVenta) throws SQLException {
        if (idCuentaCobrar == null || idFacturaVenta == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return null;
        }
        CuentaCobrar cuentaCobrar = null;
        String sql = "SELECT id_cta_cobrar, id_fact_venta_cab, cta_cob_monto, cta_cob_fecha, " +
                    "cta_cob_saldo, cta_cob_fecha_venci, cta_cob_estado, cta_cob_cantidad_cuota " +
                    "FROM cuenta_cobrar WHERE id_cta_cobrar = ? AND id_fact_venta_cab = ?";

        facturaVentaDAO = new FacturaVentaDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuentaCobrar);
            stmt.setLong(2, idFacturaVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cuentaCobrar = new CuentaCobrar(
                        rs.getLong("id_cta_cobrar"),
                        facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                        rs.getLong("cta_cob_monto"),
                        rs.getDate("cta_cob_fecha"),
                        rs.getLong("cta_cob_saldo"),
                        rs.getDate("cta_cob_fecha_venci"),
                        rs.getString("cta_cob_estado"),
                        rs.getInt("cta_cob_cantidad_cuota")
                    );
                }
            }
        }
        return cuentaCobrar;
    }

    public List<CuentaCobrar> listarCuentasCobrar() throws SQLException {
        List<CuentaCobrar> cuentas = new ArrayList<>();
        String sql = "SELECT id_cta_cobrar, id_fact_venta_cab, cta_cob_monto, cta_cob_fecha, " +
                    "cta_cob_saldo, cta_cob_fecha_venci, cta_cob_estado, cta_cob_cantidad_cuota " +
                    "FROM cuenta_cobrar";

        facturaVentaDAO = new FacturaVentaDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CuentaCobrar cuentaCobrar = new CuentaCobrar(
                    rs.getLong("id_cta_cobrar"),
                    facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                    rs.getLong("cta_cob_monto"),
                    rs.getDate("cta_cob_fecha"),
                    rs.getLong("cta_cob_saldo"),
                    rs.getDate("cta_cob_fecha_venci"),
                    rs.getString("cta_cob_estado"),
                    rs.getInt("cta_cob_cantidad_cuota")
                );
                cuentas.add(cuentaCobrar);
            }
        }
        return cuentas;
    }

    public List<CuentaCobrar> listarCuentasCobrarPendientes() throws SQLException {
        List<CuentaCobrar> cuentas = new ArrayList<>();
        String sql = "SELECT id_cta_cobrar, id_fact_venta_cab, cta_cob_monto, cta_cob_fecha, " +
                    "cta_cob_saldo, cta_cob_fecha_venci, cta_cob_estado, cta_cob_cantidad_cuota " +
                    "FROM cuenta_cobrar WHERE cta_cob_saldo > 0";

        facturaVentaDAO = new FacturaVentaDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CuentaCobrar cuentaCobrar = new CuentaCobrar(
                    rs.getLong("id_cta_cobrar"),
                    facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                    rs.getLong("cta_cob_monto"),
                    rs.getDate("cta_cob_fecha"),
                    rs.getLong("cta_cob_saldo"),
                    rs.getDate("cta_cob_fecha_venci"),
                    rs.getString("cta_cob_estado"),
                    rs.getInt("cta_cob_cantidad_cuota")
                );
                cuentas.add(cuentaCobrar);
            }
        }
        return cuentas;
    }

    public Long insertarCuentaCobrar(CuentaCobrar cuentaCobrar) throws SQLException {
        if (cuentaCobrar == null) {
            LOGGER.log(Level.SEVERE, "Error: La cuenta a cobrar es nula");
            return null;
        }

        String sql = "INSERT INTO cuenta_cobrar (id_fact_venta_cab, cta_cob_monto, cta_cob_fecha, " +
                    "cta_cob_saldo, cta_cob_fecha_venci, cta_cob_estado, cta_cob_cantidad_cuota) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cuentaCobrar.getFacturaVenta().getIdFacturaVenta());
            stmt.setLong(2, cuentaCobrar.getMonto());
            stmt.setDate(3, new java.sql.Date(cuentaCobrar.getFecha().getTime()));
            stmt.setLong(4, cuentaCobrar.getSaldo());
            stmt.setDate(5, new java.sql.Date(cuentaCobrar.getFechaVencimiento().getTime()));
            stmt.setString(6, cuentaCobrar.getEstado());
            stmt.setInt(7, cuentaCobrar.getCantidadCuota());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la cuenta a cobrar, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    cuentaCobrar.setIdCuentaCobrar(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la cuenta a cobrar.");
                }
            }
        }
    }

    public void actualizarCuentaCobrar(CuentaCobrar cuentaCobrar) throws SQLException {
        if (cuentaCobrar == null || cuentaCobrar.getIdCuentaCobrar() == null) {
            LOGGER.log(Level.WARNING, "Error: cuenta a cobrar es nula");
            return;
        }

        String sql = "UPDATE cuenta_cobrar SET cta_cob_monto = ?, cta_cob_fecha = ?, " +
                    "cta_cob_saldo = ?, cta_cob_fecha_venci = ?, cta_cob_estado = ?, " +
                    "cta_cob_cantidad_cuota = ? WHERE id_cta_cobrar = ? AND id_fact_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cuentaCobrar.getMonto());
            stmt.setDate(2, new java.sql.Date(cuentaCobrar.getFecha().getTime()));
            stmt.setLong(3, cuentaCobrar.getSaldo());
            stmt.setDate(4, new java.sql.Date(cuentaCobrar.getFechaVencimiento().getTime()));
            stmt.setString(5, cuentaCobrar.getEstado());
            stmt.setInt(6, cuentaCobrar.getCantidadCuota());
            stmt.setLong(7, cuentaCobrar.getIdCuentaCobrar());
            stmt.setLong(8, cuentaCobrar.getFacturaVenta().getIdFacturaVenta());

            stmt.executeUpdate();
        }
    }

    public void eliminarCuentaCobrar(Long idCuentaCobrar, Long idFacturaVenta) throws SQLException {
        if (idCuentaCobrar == null || idFacturaVenta == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return;
        }

        String sql = "DELETE FROM cuenta_cobrar WHERE id_cta_cobrar = ? AND id_fact_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCuentaCobrar);
            stmt.setLong(2, idFacturaVenta);
            stmt.executeUpdate();
        }
    }
}
