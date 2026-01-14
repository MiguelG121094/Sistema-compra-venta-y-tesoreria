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
public class FacturaCompraDAO {

    private Connection conn;
    private ProveedorDAO proveedorDAO;
    private SucursalDAO sucursalDAO;
    private UsuarioDAO usuarioDAO;
    private OrdenCompraDAO ordenCompraDAO;
    private static final Logger LOGGER = Logger.getLogger(FacturaCompraDAO.class.getName());

    public FacturaCompraDAO(Connection conn) {
        this.conn = conn;
    }

    public FacturaCompra getFacturaCompra(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: el parámetro idFacturaCompra es nulo");
            return null;
        }
        FacturaCompra facturaCompra = null;
        String sql = "SELECT id_fact_comp_cab, fact_comp_numero, fact_comp_timbrado, fact_comp_fecha_venci_timb, " +
                    "fact_comp_fecha_emision, fact_comp_fecha_carga, fact_comp_condicion, fact_comp_plazo, " +
                    "fact_comp_fecha_venci, fact_comp_observacion, fact_comp_estado, fact_comp_tipo_factura, " +
                    "id_proveedor, id_sucursal, id_usuario, id_orden_compra_cab " +
                    "FROM factura_compra_cabecera WHERE id_fact_comp_cab = ?";

        proveedorDAO = new ProveedorDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);
        ordenCompraDAO = new OrdenCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long idOrdenCompra = rs.getLong("id_orden_compra_cab");
                    OrdenCompra ordenCompra = idOrdenCompra != 0 ? ordenCompraDAO.getOrdenCompra(idOrdenCompra) : null;

                    facturaCompra = new FacturaCompra(
                        rs.getLong("id_fact_comp_cab"),
                        rs.getInt("fact_comp_numero"),
                        rs.getInt("fact_comp_timbrado"),
                        rs.getDate("fact_comp_fecha_venci_timb"),
                        rs.getDate("fact_comp_fecha_emision"),
                        rs.getDate("fact_comp_fecha_carga"),
                        rs.getString("fact_comp_condicion"),
                        rs.getInt("fact_comp_plazo"),
                        rs.getDate("fact_comp_fecha_venci"),
                        rs.getString("fact_comp_observacion"),
                        rs.getString("fact_comp_estado"),
                        rs.getString("fact_comp_tipo_factura"),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        ordenCompra
                    );
                }
            }
        }
        return facturaCompra;
    }

    public List<FacturaCompra> listarFacturasCompra() throws SQLException {
        List<FacturaCompra> facturas = new ArrayList<>();
        String sql = "SELECT id_fact_comp_cab, fact_comp_numero, fact_comp_timbrado, fact_comp_fecha_venci_timb, " +
                    "fact_comp_fecha_emision, fact_comp_fecha_carga, fact_comp_condicion, fact_comp_plazo, " +
                    "fact_comp_fecha_venci, fact_comp_observacion, fact_comp_estado, fact_comp_tipo_factura, " +
                    "id_proveedor, id_sucursal, id_usuario, id_orden_compra_cab " +
                    "FROM factura_compra_cabecera";

        proveedorDAO = new ProveedorDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);
        ordenCompraDAO = new OrdenCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Long idOrdenCompra = rs.getLong("id_orden_compra_cab");
                OrdenCompra ordenCompra = idOrdenCompra != 0 ? ordenCompraDAO.getOrdenCompra(idOrdenCompra) : null;

                FacturaCompra facturaCompra = new FacturaCompra(
                    rs.getLong("id_fact_comp_cab"),
                    rs.getInt("fact_comp_numero"),
                    rs.getInt("fact_comp_timbrado"),
                    rs.getDate("fact_comp_fecha_venci_timb"),
                    rs.getDate("fact_comp_fecha_emision"),
                    rs.getDate("fact_comp_fecha_carga"),
                    rs.getString("fact_comp_condicion"),
                    rs.getInt("fact_comp_plazo"),
                    rs.getDate("fact_comp_fecha_venci"),
                    rs.getString("fact_comp_observacion"),
                    rs.getString("fact_comp_estado"),
                    rs.getString("fact_comp_tipo_factura"),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    ordenCompra
                );
                facturas.add(facturaCompra);
            }
        }
        return facturas;
    }

    public Long obtenerProximoIdFacturaCompra() throws SQLException {
        Long proximoId = null;
        String sqlSecuencia = "SELECT " +
                "CASE " +
                "   WHEN is_called THEN last_value + 1 " +
                "   ELSE start_value " +
                "END AS proximo_id " +
                "FROM factura_compra_cabecera_id_fact_comp_cab_seq;";

        try (PreparedStatement stmt = conn.prepareStatement(sqlSecuencia);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                proximoId = rs.getLong("proximo_id");
            }
        }

        String sqlVerificar = "SELECT COUNT(*) FROM factura_compra_cabecera WHERE id_fact_comp_cab = ?";
        boolean existe = true;
        while (existe) {
            try (PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar)) {
                stmtVerificar.setLong(1, proximoId);
                try (ResultSet rsVerificar = stmtVerificar.executeQuery()) {
                    if (rsVerificar.next()) {
                        existe = rsVerificar.getInt(1) > 0;
                    }
                }
            }
            if (existe) {
                proximoId++;
            }
        }
        return proximoId;
    }

    public Long insertarFacturaCompra(FacturaCompra facturaCompra) throws SQLException {
        if (facturaCompra == null) {
            LOGGER.log(Level.SEVERE, "Error: La factura de compra es nula");
            return null;
        }

        String sql = "INSERT INTO factura_compra_cabecera (fact_comp_numero, fact_comp_timbrado, " +
                    "fact_comp_fecha_venci_timb, fact_comp_fecha_emision, fact_comp_fecha_carga, " +
                    "fact_comp_condicion, fact_comp_plazo, fact_comp_fecha_venci, fact_comp_observacion, " +
                    "fact_comp_estado, fact_comp_tipo_factura, id_proveedor, id_sucursal, id_usuario, " +
                    "id_orden_compra_cab) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, facturaCompra.getNumero());
            stmt.setInt(2, facturaCompra.getTimbrado());
            stmt.setDate(3, new java.sql.Date(facturaCompra.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(facturaCompra.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(facturaCompra.getFechaCarga().getTime()));
            stmt.setString(6, facturaCompra.getCondicion());
            stmt.setInt(7, facturaCompra.getPlazo());
            if (facturaCompra.getFechaVencimiento() != null) {
                stmt.setDate(8, new java.sql.Date(facturaCompra.getFechaVencimiento().getTime()));
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            stmt.setString(9, facturaCompra.getObservacion());
            stmt.setString(10, facturaCompra.getEstado());
            stmt.setString(11, facturaCompra.getTipoFactura());
            stmt.setLong(12, facturaCompra.getProveedor().getIdProveedor());
            stmt.setLong(13, facturaCompra.getSucursal().getIdSucursal());
            stmt.setLong(14, facturaCompra.getUsuario().getIdUsuario());
            if (facturaCompra.getOrdenCompra() != null) {
                stmt.setLong(15, facturaCompra.getOrdenCompra().getIdOrdenCompra());
            } else {
                stmt.setNull(15, java.sql.Types.INTEGER);
            }

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la factura de compra, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    facturaCompra.setIdFacturaCompra(idGenerado);
                    LOGGER.log(Level.INFO, "Factura de compra insertada correctamente con ID: {0}", idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la factura de compra.");
                }
            }
        }
    }

    public void actualizarFacturaCompra(FacturaCompra facturaCompra) throws SQLException {
        if (facturaCompra == null || facturaCompra.getIdFacturaCompra() == null) {
            LOGGER.log(Level.WARNING, "Error: factura de compra es nula");
            return;
        }

        String sql = "UPDATE factura_compra_cabecera SET fact_comp_numero = ?, fact_comp_timbrado = ?, " +
                    "fact_comp_fecha_venci_timb = ?, fact_comp_fecha_emision = ?, fact_comp_fecha_carga = ?, " +
                    "fact_comp_condicion = ?, fact_comp_plazo = ?, fact_comp_fecha_venci = ?, " +
                    "fact_comp_observacion = ?, fact_comp_estado = ?, fact_comp_tipo_factura = ?, " +
                    "id_proveedor = ?, id_sucursal = ?, id_usuario = ?, id_orden_compra_cab = ? " +
                    "WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facturaCompra.getNumero());
            stmt.setInt(2, facturaCompra.getTimbrado());
            stmt.setDate(3, new java.sql.Date(facturaCompra.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(facturaCompra.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(facturaCompra.getFechaCarga().getTime()));
            stmt.setString(6, facturaCompra.getCondicion());
            stmt.setInt(7, facturaCompra.getPlazo());
            if (facturaCompra.getFechaVencimiento() != null) {
                stmt.setDate(8, new java.sql.Date(facturaCompra.getFechaVencimiento().getTime()));
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            stmt.setString(9, facturaCompra.getObservacion());
            stmt.setString(10, facturaCompra.getEstado());
            stmt.setString(11, facturaCompra.getTipoFactura());
            stmt.setLong(12, facturaCompra.getProveedor().getIdProveedor());
            stmt.setLong(13, facturaCompra.getSucursal().getIdSucursal());
            stmt.setLong(14, facturaCompra.getUsuario().getIdUsuario());
            if (facturaCompra.getOrdenCompra() != null) {
                stmt.setLong(15, facturaCompra.getOrdenCompra().getIdOrdenCompra());
            } else {
                stmt.setNull(15, java.sql.Types.INTEGER);
            }
            stmt.setLong(16, facturaCompra.getIdFacturaCompra());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Factura de compra actualizada correctamente");
            } else {
                LOGGER.log(Level.WARNING, "No se encontró la factura de compra con ID: {0}", facturaCompra.getIdFacturaCompra());
            }
        }
    }

    public void eliminarFacturaCompra(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            LOGGER.log(Level.WARNING, "Error: id de la factura de compra es nulo");
            return;
        }

        String sql = "DELETE FROM factura_compra_cabecera WHERE id_fact_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                LOGGER.log(Level.INFO, "Factura de compra eliminada correctamente");
            } else {
                LOGGER.log(Level.WARNING, "No se encontró la factura de compra con ID: {0}", idFacturaCompra);
            }
        }
    }
}
