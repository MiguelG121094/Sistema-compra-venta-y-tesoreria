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
public class OrdenCompraDAO {

    private Connection conn;
    private PresupuestoDAO presupuestoDAO;
    private PedidoCompraDAO pedidoCompraDAO;
    private ProveedorDAO proveedorDAO;
    private SucursalDAO sucursalDAO;
    private UsuarioDAO usuarioDAO;
    private static final Logger LOGGER = Logger.getLogger(OrdenCompraDAO.class.getName());

    public OrdenCompraDAO(Connection conn) {
        this.conn = conn;
    }

    public OrdenCompra getOrdenCompra(Long idOrdenCompra) throws SQLException {
        if (idOrdenCompra == null) {
            LOGGER.log(Level.WARNING, "Error: el parámetro idOrdenCompra es nulo");
            return null;
        }
        OrdenCompra ordenCompra = null;
        String sql = "SELECT id_orden_compra_cab, id_presupuesto_cab, id_pedido_cab, id_proveedor, " +
                    "id_sucursal, id_usuario, ord_comp_fecha, ord_comp_estado, ord_comp_condicion_comp, " +
                    "ord_comp_observacion FROM orden_compra_cabecera WHERE id_orden_compra_cab = ?";
        presupuestoDAO = new PresupuestoDAO(conn);
        pedidoCompraDAO = new PedidoCompraDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long idPresupuesto = rs.getLong("id_presupuesto_cab");
                    Presupuesto presupuesto = idPresupuesto != 0 ? presupuestoDAO.getPresupuesto(idPresupuesto) : null;

                    ordenCompra = new OrdenCompra(
                        rs.getLong("id_orden_compra_cab"),
                        presupuesto,
                        pedidoCompraDAO.getPedidoCompra(rs.getLong("id_pedido_cab")),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        rs.getDate("ord_comp_fecha"),
                        rs.getString("ord_comp_estado"),
                        rs.getString("ord_comp_condicion_comp"),
                        rs.getString("ord_comp_observacion")
                    );
                }
            }
        }
        return ordenCompra;
    }

    public List<OrdenCompra> listarOrdenesCompra() throws SQLException {
        List<OrdenCompra> ordenes = new ArrayList<>();
        String sql = "SELECT id_orden_compra_cab, id_presupuesto_cab, id_pedido_cab, id_proveedor, " +
                    "id_sucursal, id_usuario, ord_comp_fecha, ord_comp_estado, ord_comp_condicion_comp, " +
                    "ord_comp_observacion FROM orden_compra_cabecera";
        presupuestoDAO = new PresupuestoDAO(conn);
        pedidoCompraDAO = new PedidoCompraDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Long idPresupuesto = rs.getLong("id_presupuesto_cab");
                Presupuesto presupuesto = idPresupuesto != 0 ? presupuestoDAO.getPresupuesto(idPresupuesto) : null;

                OrdenCompra ordenCompra = new OrdenCompra(
                    rs.getLong("id_orden_compra_cab"),
                    presupuesto,
                    pedidoCompraDAO.getPedidoCompra(rs.getLong("id_pedido_cab")),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    rs.getDate("ord_comp_fecha"),
                    rs.getString("ord_comp_estado"),
                    rs.getString("ord_comp_condicion_comp"),
                    rs.getString("ord_comp_observacion")
                );
                ordenes.add(ordenCompra);
            }
        }
        return ordenes;
    }

    public List<OrdenCompra> listarOrdenesCompraConDetalles() throws SQLException {
        List<OrdenCompra> ordenes = new ArrayList<>();
        String sql = "SELECT " +
                    "oc.id_orden_compra_cab, oc.id_presupuesto_cab, oc.id_pedido_cab, " +
                    "oc.id_usuario, p.per_nombre || ' ' || p.per_apellido AS usuario_nombre, " +
                    "pr.id_proveedor, pr.prov_razon_social, " +
                    "s.id_sucursal, s.suc_descripcion, " +
                    "oc.ord_comp_fecha, oc.ord_comp_estado, oc.ord_comp_condicion_comp, oc.ord_comp_observacion, " +
                    "COALESCE(STRING_AGG(a.art_descripcion || ' (Cant: ' || od.ord_comp_det_cantidad || ' Precio: ' || od.orden_compr_det_precio_compra || ')', ', '), '') AS articulos " +
                    "FROM orden_compra_cabecera oc " +
                    "JOIN usuario u ON oc.id_usuario = u.id_usuario " +
                    "JOIN persona p ON u.id_persona = p.id_persona " +
                    "JOIN proveedor pr ON oc.id_proveedor = pr.id_proveedor " +
                    "JOIN sucursal s ON oc.id_sucursal = s.id_sucursal " +
                    "LEFT JOIN orden_compra_detalle od ON oc.id_orden_compra_cab = od.id_orden_compra_cab " +
                    "LEFT JOIN articulo a ON od.id_articulo = a.id_articulo " +
                    "GROUP BY oc.id_orden_compra_cab, oc.id_presupuesto_cab, oc.id_pedido_cab, " +
                    "oc.id_usuario, usuario_nombre, pr.id_proveedor, pr.prov_razon_social, " +
                    "s.id_sucursal, s.suc_descripcion, oc.ord_comp_fecha, oc.ord_comp_estado, " +
                    "oc.ord_comp_condicion_comp, oc.ord_comp_observacion " +
                    "ORDER BY oc.id_orden_compra_cab";

        presupuestoDAO = new PresupuestoDAO(conn);
        pedidoCompraDAO = new PedidoCompraDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Long idPresupuesto = rs.getLong("id_presupuesto_cab");
                Presupuesto presupuesto = idPresupuesto != 0 ? presupuestoDAO.getPresupuesto(idPresupuesto) : null;

                OrdenCompra ordenCompra = new OrdenCompra(
                    rs.getLong("id_orden_compra_cab"),
                    presupuesto,
                    pedidoCompraDAO.getPedidoCompra(rs.getLong("id_pedido_cab")),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    rs.getDate("ord_comp_fecha"),
                    rs.getString("ord_comp_estado"),
                    rs.getString("ord_comp_condicion_comp"),
                    rs.getString("ord_comp_observacion")
                );
                ordenCompra.setListaArticulos(rs.getString("articulos"));
                ordenes.add(ordenCompra);
            }
        }
        return ordenes;
    }

    public Long obtenerProximoIdOrdenCompra() throws SQLException {
        Long proximoId = null;
        String sqlSecuencia = "SELECT " +
                "CASE " +
                "   WHEN is_called THEN last_value + 1 " +
                "   ELSE start_value " +
                "END AS proximo_id " +
                "FROM orden_compra_cabecera_id_orden_compra_cab_seq;";

        try (PreparedStatement stmt = conn.prepareStatement(sqlSecuencia);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                proximoId = rs.getLong("proximo_id");
            }
        }

        String sqlVerificar = "SELECT COUNT(*) FROM orden_compra_cabecera WHERE id_orden_compra_cab = ?";
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

    public Long insertarOrdenCompra(OrdenCompra ordenCompra) throws SQLException {
        if (ordenCompra == null) {
            LOGGER.log(Level.SEVERE, "Error: La orden de compra es nula");
            return null;
        }

        String sql = "INSERT INTO orden_compra_cabecera (id_presupuesto_cab, id_pedido_cab, id_proveedor, " +
                    "id_sucursal, id_usuario, ord_comp_fecha, ord_comp_estado, ord_comp_condicion_comp, " +
                    "ord_comp_observacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (ordenCompra.getPresupuesto() != null) {
                stmt.setLong(1, ordenCompra.getPresupuesto().getIdPresupuesto());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setLong(2, ordenCompra.getPedidoCompra().getIdPedido());
            stmt.setLong(3, ordenCompra.getProveedor().getIdProveedor());
            stmt.setLong(4, ordenCompra.getSucursal().getIdSucursal());
            stmt.setLong(5, ordenCompra.getUsuario().getIdUsuario());
            stmt.setDate(6, new java.sql.Date(ordenCompra.getFecha().getTime()));
            stmt.setString(7, ordenCompra.getEstado());
            stmt.setString(8, ordenCompra.getCondicionCompra());
            stmt.setString(9, ordenCompra.getObservacion());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la orden de compra, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    ordenCompra.setIdOrdenCompra(idGenerado);
                    LOGGER.log(Level.INFO, "Orden de compra insertada correctamente con ID: {0}", idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la orden de compra.");
                }
            }
        }
    }

    public void actualizarOrdenCompra(OrdenCompra ordenCompra) throws SQLException {
        if (ordenCompra == null || ordenCompra.getIdOrdenCompra() == null) {
            LOGGER.log(Level.WARNING, "Error: orden de compra es nula");
            return;
        }

        String sql = "UPDATE orden_compra_cabecera SET id_presupuesto_cab = ?, id_pedido_cab = ?, " +
                    "id_proveedor = ?, id_sucursal = ?, id_usuario = ?, ord_comp_fecha = ?, " +
                    "ord_comp_estado = ?, ord_comp_condicion_comp = ?, ord_comp_observacion = ? " +
                    "WHERE id_orden_compra_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (ordenCompra.getPresupuesto() != null) {
                stmt.setLong(1, ordenCompra.getPresupuesto().getIdPresupuesto());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setLong(2, ordenCompra.getPedidoCompra().getIdPedido());
            stmt.setLong(3, ordenCompra.getProveedor().getIdProveedor());
            stmt.setLong(4, ordenCompra.getSucursal().getIdSucursal());
            stmt.setLong(5, ordenCompra.getUsuario().getIdUsuario());
            stmt.setDate(6, new java.sql.Date(ordenCompra.getFecha().getTime()));
            stmt.setString(7, ordenCompra.getEstado());
            stmt.setString(8, ordenCompra.getCondicionCompra());
            stmt.setString(9, ordenCompra.getObservacion());
            stmt.setLong(10, ordenCompra.getIdOrdenCompra());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Orden de compra actualizada correctamente");
            } else {
                LOGGER.log(Level.WARNING, "No se encontró la orden de compra con ID: {0}", ordenCompra.getIdOrdenCompra());
            }
        }
    }

    public void eliminarOrdenCompra(Long idOrdenCompra) throws SQLException {
        if (idOrdenCompra == null) {
            LOGGER.log(Level.WARNING, "Error: id de la orden de compra es nulo");
            return;
        }

        String sql = "DELETE FROM orden_compra_cabecera WHERE id_orden_compra_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenCompra);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                LOGGER.log(Level.INFO, "Orden de compra eliminada correctamente");
            } else {
                LOGGER.log(Level.WARNING, "No se encontró la orden de compra con ID: {0}", idOrdenCompra);
            }
        }
    }
}
