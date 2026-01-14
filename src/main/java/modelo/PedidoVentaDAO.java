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
public class PedidoVentaDAO {

    private Connection conn;
    private ClienteDAO clienteDAO;
    private SucursalDAO sucursalDAO;
    private UsuarioDAO usuarioDAO;
    private static final Logger LOGGER = Logger.getLogger(PedidoVentaDAO.class.getName());

    public PedidoVentaDAO(Connection conn) {
        this.conn = conn;
    }

    public PedidoVenta getPedidoVenta(Long idPedidoVenta) throws SQLException {
        if (idPedidoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idPedidoVenta es nulo");
            return null;
        }
        PedidoVenta pedidoVenta = null;
        String sql = "SELECT pvc.id_ped_venta_cab, pvc.ped_ven_fecha, pvc.ped_ven_estado, " +
                    "pvc.id_cliente, pvc.id_sucursal, pvc.id_usuario, " +
                    "STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM pedido_venta_cabecera pvc " +
                    "LEFT JOIN pedido_venta_detalle pvd ON pvc.id_ped_venta_cab = pvd.id_ped_venta_cab " +
                    "LEFT JOIN articulo a ON pvd.id_articulo = a.id_articulo " +
                    "WHERE pvc.id_ped_venta_cab = ? " +
                    "GROUP BY pvc.id_ped_venta_cab, pvc.ped_ven_fecha, pvc.ped_ven_estado, " +
                    "pvc.id_cliente, pvc.id_sucursal, pvc.id_usuario";

        clienteDAO = new ClienteDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pedidoVenta = new PedidoVenta(
                        rs.getLong("id_ped_venta_cab"),
                        rs.getDate("ped_ven_fecha"),
                        rs.getString("ped_ven_estado"),
                        clienteDAO.getCliente(rs.getLong("id_cliente")),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        rs.getString("lista_articulos")
                    );
                }
            }
        }
        return pedidoVenta;
    }

    public List<PedidoVenta> listarPedidosVenta() throws SQLException {
        List<PedidoVenta> pedidos = new ArrayList<>();
        String sql = "SELECT pvc.id_ped_venta_cab, pvc.ped_ven_fecha, pvc.ped_ven_estado, " +
                    "pvc.id_cliente, pvc.id_sucursal, pvc.id_usuario, " +
                    "STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM pedido_venta_cabecera pvc " +
                    "LEFT JOIN pedido_venta_detalle pvd ON pvc.id_ped_venta_cab = pvd.id_ped_venta_cab " +
                    "LEFT JOIN articulo a ON pvd.id_articulo = a.id_articulo " +
                    "GROUP BY pvc.id_ped_venta_cab, pvc.ped_ven_fecha, pvc.ped_ven_estado, " +
                    "pvc.id_cliente, pvc.id_sucursal, pvc.id_usuario";

        clienteDAO = new ClienteDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PedidoVenta pedidoVenta = new PedidoVenta(
                    rs.getLong("id_ped_venta_cab"),
                    rs.getDate("ped_ven_fecha"),
                    rs.getString("ped_ven_estado"),
                    clienteDAO.getCliente(rs.getLong("id_cliente")),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    rs.getString("lista_articulos")
                );
                pedidos.add(pedidoVenta);
            }
        }
        return pedidos;
    }

    public List<PedidoVenta> listarPedidosVentaPendientes() throws SQLException {
        List<PedidoVenta> pedidos = new ArrayList<>();
        String sql = "SELECT pvc.id_ped_venta_cab, pvc.ped_ven_fecha, pvc.ped_ven_estado, " +
                    "pvc.id_cliente, pvc.id_sucursal, pvc.id_usuario, " +
                    "STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM pedido_venta_cabecera pvc " +
                    "LEFT JOIN pedido_venta_detalle pvd ON pvc.id_ped_venta_cab = pvd.id_ped_venta_cab " +
                    "LEFT JOIN articulo a ON pvd.id_articulo = a.id_articulo " +
                    "WHERE pvc.ped_ven_estado = 'PENDIENTE' " +
                    "GROUP BY pvc.id_ped_venta_cab, pvc.ped_ven_fecha, pvc.ped_ven_estado, " +
                    "pvc.id_cliente, pvc.id_sucursal, pvc.id_usuario";

        clienteDAO = new ClienteDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PedidoVenta pedidoVenta = new PedidoVenta(
                    rs.getLong("id_ped_venta_cab"),
                    rs.getDate("ped_ven_fecha"),
                    rs.getString("ped_ven_estado"),
                    clienteDAO.getCliente(rs.getLong("id_cliente")),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    rs.getString("lista_articulos")
                );
                pedidos.add(pedidoVenta);
            }
        }
        return pedidos;
    }

    public Long insertarPedidoVenta(PedidoVenta pedidoVenta) throws SQLException {
        if (pedidoVenta == null) {
            LOGGER.log(Level.SEVERE, "Error: El pedido de venta es nulo");
            return null;
        }

        String sql = "INSERT INTO pedido_venta_cabecera (ped_ven_fecha, ped_ven_estado, id_cliente, " +
                    "id_sucursal, id_usuario) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, new java.sql.Date(pedidoVenta.getFecha().getTime()));
            stmt.setString(2, pedidoVenta.getEstado());
            stmt.setLong(3, pedidoVenta.getCliente().getIdCliente());
            stmt.setLong(4, pedidoVenta.getSucursal().getIdSucursal());
            stmt.setLong(5, pedidoVenta.getUsuario().getIdUsuario());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el pedido de venta, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    pedidoVenta.setIdPedidoVenta(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el pedido de venta.");
                }
            }
        }
    }

    public void actualizarPedidoVenta(PedidoVenta pedidoVenta) throws SQLException {
        if (pedidoVenta == null || pedidoVenta.getIdPedidoVenta() == null) {
            LOGGER.log(Level.WARNING, "Error: pedido de venta es nulo");
            return;
        }

        String sql = "UPDATE pedido_venta_cabecera SET ped_ven_fecha = ?, ped_ven_estado = ?, " +
                    "id_cliente = ?, id_sucursal = ?, id_usuario = ? WHERE id_ped_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(pedidoVenta.getFecha().getTime()));
            stmt.setString(2, pedidoVenta.getEstado());
            stmt.setLong(3, pedidoVenta.getCliente().getIdCliente());
            stmt.setLong(4, pedidoVenta.getSucursal().getIdSucursal());
            stmt.setLong(5, pedidoVenta.getUsuario().getIdUsuario());
            stmt.setLong(6, pedidoVenta.getIdPedidoVenta());

            stmt.executeUpdate();
        }
    }

    public void eliminarPedidoVenta(Long idPedidoVenta) throws SQLException {
        if (idPedidoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idPedidoVenta es nulo");
            return;
        }

        String sql = "DELETE FROM pedido_venta_cabecera WHERE id_ped_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoVenta);
            stmt.executeUpdate();
        }
    }
}
