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
public class NotaRemisionVentaDetalleDAO {

    private Connection conn;
    private NotaRemisionVentaDAO notaRemisionVentaDAO;
    private ArticuloDAO articuloDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaRemisionVentaDetalleDAO.class.getName());

    public NotaRemisionVentaDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaRemisionVentaDetalle getNotaRemisionVentaDetalle(Long idNotaRemisionVenta, Long idArticulo) throws SQLException {
        if (idNotaRemisionVenta == null || idArticulo == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return null;
        }
        NotaRemisionVentaDetalle detalle = null;
        String sql = "SELECT id_nota_remi_venta, id_articulo, not_remi_vent_cantidad " +
                    "FROM nota_remision_venta_detalle WHERE id_nota_remi_venta = ? AND id_articulo = ?";

        notaRemisionVentaDAO = new NotaRemisionVentaDAO(conn);
        articuloDAO = new ArticuloDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaRemisionVenta);
            stmt.setLong(2, idArticulo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalle = new NotaRemisionVentaDetalle(
                        notaRemisionVentaDAO.getNotaRemisionVenta(rs.getLong("id_nota_remi_venta")),
                        articuloDAO.getArticulo(rs.getLong("id_articulo")),
                        rs.getInt("not_remi_vent_cantidad")
                    );
                }
            }
        }
        return detalle;
    }

    public List<NotaRemisionVentaDetalle> listarDetallesPorNota(Long idNotaRemisionVenta) throws SQLException {
        List<NotaRemisionVentaDetalle> detalles = new ArrayList<>();
        if (idNotaRemisionVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaRemisionVenta es nulo");
            return detalles;
        }

        String sql = "SELECT id_nota_remi_venta, id_articulo, not_remi_vent_cantidad " +
                    "FROM nota_remision_venta_detalle WHERE id_nota_remi_venta = ?";

        notaRemisionVentaDAO = new NotaRemisionVentaDAO(conn);
        articuloDAO = new ArticuloDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaRemisionVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaRemisionVentaDetalle detalle = new NotaRemisionVentaDetalle(
                        notaRemisionVentaDAO.getNotaRemisionVenta(rs.getLong("id_nota_remi_venta")),
                        articuloDAO.getArticulo(rs.getLong("id_articulo")),
                        rs.getInt("not_remi_vent_cantidad")
                    );
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public void insertarNotaRemisionVentaDetalle(NotaRemisionVentaDetalle detalle) throws SQLException {
        if (detalle == null) {
            LOGGER.log(Level.SEVERE, "Error: El detalle es nulo");
            return;
        }

        String sql = "INSERT INTO nota_remision_venta_detalle (id_nota_remi_venta, id_articulo, not_remi_vent_cantidad) " +
                    "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getNotaRemisionVenta().getIdNotaRemisionVenta());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setInt(3, detalle.getCantidad());

            stmt.executeUpdate();
        }
    }

    public void insertarDetalles(List<NotaRemisionVentaDetalle> detalles) throws SQLException {
        if (detalles == null || detalles.isEmpty()) {
            LOGGER.log(Level.WARNING, "Error: lista de detalles vacía o nula");
            return;
        }

        String sql = "INSERT INTO nota_remision_venta_detalle (id_nota_remi_venta, id_articulo, not_remi_vent_cantidad) " +
                    "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (NotaRemisionVentaDetalle detalle : detalles) {
                stmt.setLong(1, detalle.getNotaRemisionVenta().getIdNotaRemisionVenta());
                stmt.setLong(2, detalle.getArticulo().getIdArticulo());
                stmt.setInt(3, detalle.getCantidad());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void actualizarNotaRemisionVentaDetalle(NotaRemisionVentaDetalle detalle) throws SQLException {
        if (detalle == null || detalle.getNotaRemisionVenta() == null || detalle.getArticulo() == null) {
            LOGGER.log(Level.WARNING, "Error: detalle o sus claves son nulas");
            return;
        }

        String sql = "UPDATE nota_remision_venta_detalle SET not_remi_vent_cantidad = ? " +
                    "WHERE id_nota_remi_venta = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detalle.getCantidad());
            stmt.setLong(2, detalle.getNotaRemisionVenta().getIdNotaRemisionVenta());
            stmt.setLong(3, detalle.getArticulo().getIdArticulo());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaRemisionVentaDetalle(Long idNotaRemisionVenta, Long idArticulo) throws SQLException {
        if (idNotaRemisionVenta == null || idArticulo == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return;
        }

        String sql = "DELETE FROM nota_remision_venta_detalle WHERE id_nota_remi_venta = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaRemisionVenta);
            stmt.setLong(2, idArticulo);
            stmt.executeUpdate();
        }
    }

    public void eliminarDetallesPorNota(Long idNotaRemisionVenta) throws SQLException {
        if (idNotaRemisionVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaRemisionVenta es nulo");
            return;
        }

        String sql = "DELETE FROM nota_remision_venta_detalle WHERE id_nota_remi_venta = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaRemisionVenta);
            stmt.executeUpdate();
        }
    }
}
