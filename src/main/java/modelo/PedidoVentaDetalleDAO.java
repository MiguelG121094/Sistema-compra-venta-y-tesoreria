/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class PedidoVentaDetalleDAO {

    private Connection conn;
    private PedidoVentaDAO pedidoVentaDAO;
    private ArticuloDAO articuloDAO;
    private static final Logger LOGGER = Logger.getLogger(PedidoVentaDetalleDAO.class.getName());

    public PedidoVentaDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public PedidoVentaDetalle getPedidoVentaDetalle(Long idPedidoVenta, Long idArticulo) throws SQLException {
        if (idPedidoVenta == null || idArticulo == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return null;
        }
        PedidoVentaDetalle detalle = null;
        String sql = "SELECT id_ped_venta_cab, id_articulo, ped_ven_cantidad " +
                    "FROM pedido_venta_detalle WHERE id_ped_venta_cab = ? AND id_articulo = ?";

        pedidoVentaDAO = new PedidoVentaDAO(conn);
        articuloDAO = new ArticuloDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoVenta);
            stmt.setLong(2, idArticulo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalle = new PedidoVentaDetalle(
                        pedidoVentaDAO.getPedidoVenta(rs.getLong("id_ped_venta_cab")),
                        articuloDAO.getArticulo(rs.getLong("id_articulo")),
                        rs.getInt("ped_ven_cantidad")
                    );
                }
            }
        }
        return detalle;
    }

    public List<PedidoVentaDetalle> listarDetallesPorPedido(Long idPedidoVenta) throws SQLException {
        List<PedidoVentaDetalle> detalles = new ArrayList<>();
        if (idPedidoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idPedidoVenta es nulo");
            return detalles;
        }

        String sql = "SELECT id_ped_venta_cab, id_articulo, ped_ven_cantidad " +
                    "FROM pedido_venta_detalle WHERE id_ped_venta_cab = ?";

        pedidoVentaDAO = new PedidoVentaDAO(conn);
        articuloDAO = new ArticuloDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PedidoVentaDetalle detalle = new PedidoVentaDetalle(
                        pedidoVentaDAO.getPedidoVenta(rs.getLong("id_ped_venta_cab")),
                        articuloDAO.getArticulo(rs.getLong("id_articulo")),
                        rs.getInt("ped_ven_cantidad")
                    );
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public void insertarPedidoVentaDetalle(PedidoVentaDetalle detalle) throws SQLException {
        if (detalle == null) {
            LOGGER.log(Level.SEVERE, "Error: El detalle es nulo");
            return;
        }

        String sql = "INSERT INTO pedido_venta_detalle (id_ped_venta_cab, id_articulo, ped_ven_cantidad) " +
                    "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getPedidoVenta().getIdPedidoVenta());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setInt(3, detalle.getCantidad());

            stmt.executeUpdate();
        }
    }

    public void insertarDetalles(List<PedidoVentaDetalle> detalles) throws SQLException {
        if (detalles == null || detalles.isEmpty()) {
            LOGGER.log(Level.WARNING, "Error: lista de detalles vacía o nula");
            return;
        }

        String sql = "INSERT INTO pedido_venta_detalle (id_ped_venta_cab, id_articulo, ped_ven_cantidad) " +
                    "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (PedidoVentaDetalle detalle : detalles) {
                stmt.setLong(1, detalle.getPedidoVenta().getIdPedidoVenta());
                stmt.setLong(2, detalle.getArticulo().getIdArticulo());
                stmt.setInt(3, detalle.getCantidad());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void actualizarPedidoVentaDetalle(PedidoVentaDetalle detalle) throws SQLException {
        if (detalle == null || detalle.getPedidoVenta() == null || detalle.getArticulo() == null) {
            LOGGER.log(Level.WARNING, "Error: detalle o sus claves son nulas");
            return;
        }

        String sql = "UPDATE pedido_venta_detalle SET ped_ven_cantidad = ? " +
                    "WHERE id_ped_venta_cab = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detalle.getCantidad());
            stmt.setLong(2, detalle.getPedidoVenta().getIdPedidoVenta());
            stmt.setLong(3, detalle.getArticulo().getIdArticulo());

            stmt.executeUpdate();
        }
    }

    public void eliminarPedidoVentaDetalle(Long idPedidoVenta, Long idArticulo) throws SQLException {
        if (idPedidoVenta == null || idArticulo == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return;
        }

        String sql = "DELETE FROM pedido_venta_detalle WHERE id_ped_venta_cab = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoVenta);
            stmt.setLong(2, idArticulo);
            stmt.executeUpdate();
        }
    }

    public void eliminarDetallesPorPedido(Long idPedidoVenta) throws SQLException {
        if (idPedidoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idPedidoVenta es nulo");
            return;
        }

        String sql = "DELETE FROM pedido_venta_detalle WHERE id_ped_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoVenta);
            stmt.executeUpdate();
        }
    }
}
