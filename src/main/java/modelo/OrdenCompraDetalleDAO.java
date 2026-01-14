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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Miguel
 */
public class OrdenCompraDetalleDAO {

    private Connection conn;
    private ArticuloDAO articuloDAO;

    public OrdenCompraDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public List<OrdenCompraDetalle> listarDetallesPorOrdenCompra(Long idOrdenCompraCab) throws SQLException {
        if (idOrdenCompraCab == null) {
            System.out.println("El parámetro idOrdenCompraCab es nulo en: OrdenCompraDetalleDAO().listarDetallesPorOrdenCompra");
            return null;
        }
        List<OrdenCompraDetalle> detalles = new ArrayList<>();
        String sql = "SELECT * FROM orden_compra_detalle WHERE id_orden_compra_cab = ?";
        articuloDAO = new ArticuloDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenCompraCab);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrdenCompra ordenCompra = new OrdenCompra(rs.getLong("id_orden_compra_cab"));
                    Articulo articulo = articuloDAO.getArticulo(rs.getLong("id_articulo"));
                    Long cantidad = rs.getLong("ord_comp_det_cantidad");
                    Long precioCompra = rs.getLong("orden_compr_det_precio_compra");

                    OrdenCompraDetalle detalle = new OrdenCompraDetalle(ordenCompra, articulo, cantidad, precioCompra);
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public boolean insertarDetalle(OrdenCompraDetalle detalle) throws SQLException {
        if (detalle == null || detalle.getOrdenCompra() == null || detalle.getArticulo() == null) {
            System.out.println("Error: Detalle de orden de compra inválido.");
            return false;
        }

        String sql = "INSERT INTO orden_compra_detalle (id_orden_compra_cab, id_articulo, ord_comp_det_cantidad, " +
                    "orden_compr_det_precio_compra) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getOrdenCompra().getIdOrdenCompra());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setLong(3, detalle.getCantidad());
            stmt.setLong(4, detalle.getPrecioCompra());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean eliminarDetalle(Long idOrdenCompra, Long idArticulo) throws SQLException {
        if (idOrdenCompra == null || idArticulo == null) {
            System.out.println("Error: id de la orden o artículo nulo.");
            return false;
        }

        String sql = "DELETE FROM orden_compra_detalle WHERE id_orden_compra_cab = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenCompra);
            stmt.setLong(2, idArticulo);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public void actualizarOrdenCompraDetalles(Long idOrdenCompra, List<OrdenCompraDetalle> detalles) throws SQLException {
        if (idOrdenCompra == null || detalles == null) {
            System.out.println("Error: Parámetros inválidos para actualizar detalles");
            return;
        }

        String updateDetalle = "UPDATE orden_compra_detalle " +
                              "SET ord_comp_det_cantidad = ?, orden_compr_det_precio_compra = ? " +
                              "WHERE id_orden_compra_cab = ? AND id_articulo = ?";

        try (PreparedStatement stmtUpdate = conn.prepareStatement(updateDetalle)) {
            Set<Long> detallesExistentes = obtenerDetallesExistentes(idOrdenCompra);

            for (OrdenCompraDetalle detalle : detalles) {
                Long idArticulo = detalle.getArticulo().getIdArticulo();

                if (detallesExistentes.contains(idArticulo)) {
                    stmtUpdate.setLong(1, detalle.getCantidad());
                    stmtUpdate.setLong(2, detalle.getPrecioCompra());
                    stmtUpdate.setLong(3, idOrdenCompra);
                    stmtUpdate.setLong(4, idArticulo);
                    stmtUpdate.addBatch();
                } else {
                    insertarDetalle(detalle);
                }

                detallesExistentes.remove(idArticulo);
            }

            for (Long idArticulo : detallesExistentes) {
                eliminarDetalle(idOrdenCompra, idArticulo);
            }

            stmtUpdate.executeBatch();
        }
    }

    private Set<Long> obtenerDetallesExistentes(Long idOrdenCompra) throws SQLException {
        Set<Long> detallesExistentes = new HashSet<>();
        String sql = "SELECT id_articulo FROM orden_compra_detalle WHERE id_orden_compra_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detallesExistentes.add(rs.getLong("id_articulo"));
                }
            }
        }
        return detallesExistentes;
    }
}
