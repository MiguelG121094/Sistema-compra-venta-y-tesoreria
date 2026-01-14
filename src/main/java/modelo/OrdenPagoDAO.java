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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class OrdenPagoDAO {

    private Connection conn;
    private SucursalDAO sucursalDAO;
    private ProveedorDAO proveedorDAO;
    private static final Logger LOGGER = Logger.getLogger(OrdenPagoDAO.class.getName());

    public OrdenPagoDAO(Connection conn) {
        this.conn = conn;
    }

    public OrdenPago getOrdenPago(Long idOrdenPago) throws SQLException {
        if (idOrdenPago == null) {
            LOGGER.log(Level.WARNING, "Error: idOrdenPago es nulo");
            return null;
        }
        OrdenPago ordenPago = null;
        String sql = "SELECT id_orden_pago, ord_pag_numero, ord_pag_fecha_emision, ord_pag_monto, " +
                    "ord_pag_estado, id_provi_cta_pagar_cabecera, ord_pag_nro_recibo, id_moneda, " +
                    "ord_pag_tipo_cambio, id_sucursal, id_cheque, ord_pag_tipo_pago, id_proveedor, id_cuenta " +
                    "FROM orden_pago_cabecera WHERE id_orden_pago = ?";

        sucursalDAO = new SucursalDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenPago);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ordenPago = new OrdenPago(
                        rs.getLong("id_orden_pago"),
                        rs.getInt("ord_pag_numero"),
                        rs.getDate("ord_pag_fecha_emision"),
                        rs.getLong("ord_pag_monto"),
                        rs.getString("ord_pag_estado"),
                        rs.getLong("id_provi_cta_pagar_cabecera"),
                        rs.getInt("ord_pag_nro_recibo"),
                        rs.getLong("id_moneda"),
                        rs.getDouble("ord_pag_tipo_cambio"),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        rs.getLong("id_cheque"),
                        rs.getString("ord_pag_tipo_pago"),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                        rs.getLong("id_cuenta")
                    );
                }
            }
        }
        return ordenPago;
    }

    public List<OrdenPago> listarOrdenesPago() throws SQLException {
        List<OrdenPago> ordenes = new ArrayList<>();
        String sql = "SELECT id_orden_pago, ord_pag_numero, ord_pag_fecha_emision, ord_pag_monto, " +
                    "ord_pag_estado, id_provi_cta_pagar_cabecera, ord_pag_nro_recibo, id_moneda, " +
                    "ord_pag_tipo_cambio, id_sucursal, id_cheque, ord_pag_tipo_pago, id_proveedor, id_cuenta " +
                    "FROM orden_pago_cabecera";

        sucursalDAO = new SucursalDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                OrdenPago ordenPago = new OrdenPago(
                    rs.getLong("id_orden_pago"),
                    rs.getInt("ord_pag_numero"),
                    rs.getDate("ord_pag_fecha_emision"),
                    rs.getLong("ord_pag_monto"),
                    rs.getString("ord_pag_estado"),
                    rs.getLong("id_provi_cta_pagar_cabecera"),
                    rs.getInt("ord_pag_nro_recibo"),
                    rs.getLong("id_moneda"),
                    rs.getDouble("ord_pag_tipo_cambio"),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    rs.getLong("id_cheque"),
                    rs.getString("ord_pag_tipo_pago"),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    rs.getLong("id_cuenta")
                );
                ordenes.add(ordenPago);
            }
        }
        return ordenes;
    }

    public Long insertarOrdenPago(OrdenPago ordenPago) throws SQLException {
        if (ordenPago == null) {
            LOGGER.log(Level.SEVERE, "Error: La orden de pago es nula");
            return null;
        }

        String sql = "INSERT INTO orden_pago_cabecera (ord_pag_numero, ord_pag_fecha_emision, ord_pag_monto, " +
                    "ord_pag_estado, id_provi_cta_pagar_cabecera, ord_pag_nro_recibo, id_moneda, " +
                    "ord_pag_tipo_cambio, id_sucursal, id_cheque, ord_pag_tipo_pago, id_proveedor, id_cuenta) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ordenPago.getNumero());
            stmt.setDate(2, new java.sql.Date(ordenPago.getFechaEmision().getTime()));
            stmt.setLong(3, ordenPago.getMonto());
            stmt.setString(4, ordenPago.getEstado());
            stmt.setLong(5, ordenPago.getIdProvisionCtaPagar());
            stmt.setInt(6, ordenPago.getNumeroRecibo());
            stmt.setLong(7, ordenPago.getIdMoneda());
            if (ordenPago.getTipoCambio() != null) {
                stmt.setDouble(8, ordenPago.getTipoCambio());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            stmt.setLong(9, ordenPago.getSucursal().getIdSucursal());
            if (ordenPago.getIdCheque() != null) {
                stmt.setLong(10, ordenPago.getIdCheque());
            } else {
                stmt.setNull(10, Types.BIGINT);
            }
            stmt.setString(11, ordenPago.getTipoPago());
            stmt.setLong(12, ordenPago.getProveedor().getIdProveedor());
            stmt.setLong(13, ordenPago.getIdCuenta());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la orden de pago, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    ordenPago.setIdOrdenPago(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la orden de pago.");
                }
            }
        }
    }

    public void actualizarOrdenPago(OrdenPago ordenPago) throws SQLException {
        if (ordenPago == null || ordenPago.getIdOrdenPago() == null) {
            LOGGER.log(Level.WARNING, "Error: orden de pago es nula");
            return;
        }

        String sql = "UPDATE orden_pago_cabecera SET ord_pag_numero = ?, ord_pag_fecha_emision = ?, " +
                    "ord_pag_monto = ?, ord_pag_estado = ?, id_provi_cta_pagar_cabecera = ?, " +
                    "ord_pag_nro_recibo = ?, id_moneda = ?, ord_pag_tipo_cambio = ?, id_sucursal = ?, " +
                    "id_cheque = ?, ord_pag_tipo_pago = ?, id_proveedor = ?, id_cuenta = ? " +
                    "WHERE id_orden_pago = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ordenPago.getNumero());
            stmt.setDate(2, new java.sql.Date(ordenPago.getFechaEmision().getTime()));
            stmt.setLong(3, ordenPago.getMonto());
            stmt.setString(4, ordenPago.getEstado());
            stmt.setLong(5, ordenPago.getIdProvisionCtaPagar());
            stmt.setInt(6, ordenPago.getNumeroRecibo());
            stmt.setLong(7, ordenPago.getIdMoneda());
            if (ordenPago.getTipoCambio() != null) {
                stmt.setDouble(8, ordenPago.getTipoCambio());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            stmt.setLong(9, ordenPago.getSucursal().getIdSucursal());
            if (ordenPago.getIdCheque() != null) {
                stmt.setLong(10, ordenPago.getIdCheque());
            } else {
                stmt.setNull(10, Types.BIGINT);
            }
            stmt.setString(11, ordenPago.getTipoPago());
            stmt.setLong(12, ordenPago.getProveedor().getIdProveedor());
            stmt.setLong(13, ordenPago.getIdCuenta());
            stmt.setLong(14, ordenPago.getIdOrdenPago());

            stmt.executeUpdate();
        }
    }

    public void eliminarOrdenPago(Long idOrdenPago) throws SQLException {
        if (idOrdenPago == null) {
            LOGGER.log(Level.WARNING, "Error: idOrdenPago es nulo");
            return;
        }

        String sql = "DELETE FROM orden_pago_cabecera WHERE id_orden_pago = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenPago);
            stmt.executeUpdate();
        }
    }
}
