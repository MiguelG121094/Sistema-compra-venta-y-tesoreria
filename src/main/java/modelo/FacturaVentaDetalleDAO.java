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
public class FacturaVentaDetalleDAO {

    private Connection conn;
    private FacturaVentaDAO facturaVentaDAO;
    private ArticuloDAO articuloDAO;
    private DepositoDAO depositoDAO;
    private static final Logger LOGGER = Logger.getLogger(FacturaVentaDetalleDAO.class.getName());

    public FacturaVentaDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public FacturaVentaDetalle getFacturaVentaDetalle(Long idFacturaVenta, Long idArticulo, Long idDeposito) throws SQLException {
        if (idFacturaVenta == null || idArticulo == null || idDeposito == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return null;
        }
        FacturaVentaDetalle detalle = null;
        String sql = "SELECT id_fact_venta_cab, id_articulo, id_deposito, fact_venta_cantidad, " +
                    "fact_venta_precio_venta FROM factura_venta_detalle " +
                    "WHERE id_fact_venta_cab = ? AND id_articulo = ? AND id_deposito = ?";

        facturaVentaDAO = new FacturaVentaDAO(conn);
        articuloDAO = new ArticuloDAO(conn);
        depositoDAO = new DepositoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaVenta);
            stmt.setLong(2, idArticulo);
            stmt.setLong(3, idDeposito);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalle = new FacturaVentaDetalle(
                        facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                        articuloDAO.getArticulo(rs.getLong("id_articulo")),
                        depositoDAO.getDepostio(rs.getLong("id_deposito")),
                        rs.getInt("fact_venta_cantidad"),
                        rs.getLong("fact_venta_precio_venta")
                    );
                }
            }
        }
        return detalle;
    }

    public List<FacturaVentaDetalle> listarDetallesPorFactura(Long idFacturaVenta) throws SQLException {
        List<FacturaVentaDetalle> detalles = new ArrayList<>();
        if (idFacturaVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaVenta es nulo");
            return detalles;
        }

        String sql = "SELECT id_fact_venta_cab, id_articulo, id_deposito, fact_venta_cantidad, " +
                    "fact_venta_precio_venta FROM factura_venta_detalle WHERE id_fact_venta_cab = ?";

        facturaVentaDAO = new FacturaVentaDAO(conn);
        articuloDAO = new ArticuloDAO(conn);
        depositoDAO = new DepositoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FacturaVentaDetalle detalle = new FacturaVentaDetalle(
                        facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                        articuloDAO.getArticulo(rs.getLong("id_articulo")),
                        depositoDAO.getDepostio(rs.getLong("id_deposito")),
                        rs.getInt("fact_venta_cantidad"),
                        rs.getLong("fact_venta_precio_venta")
                    );
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public void insertarFacturaVentaDetalle(FacturaVentaDetalle detalle) throws SQLException {
        if (detalle == null) {
            LOGGER.log(Level.SEVERE, "Error: El detalle es nulo");
            return;
        }

        String sql = "INSERT INTO factura_venta_detalle (id_fact_venta_cab, id_articulo, id_deposito, " +
                    "fact_venta_cantidad, fact_venta_precio_venta) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getFacturaVenta().getIdFacturaVenta());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setLong(3, detalle.getDeposito().getIdDeposito());
            stmt.setInt(4, detalle.getCantidad());
            stmt.setLong(5, detalle.getPrecioVenta());

            stmt.executeUpdate();
        }
    }

    public void insertarDetalles(List<FacturaVentaDetalle> detalles) throws SQLException {
        if (detalles == null || detalles.isEmpty()) {
            LOGGER.log(Level.WARNING, "Error: lista de detalles vacía o nula");
            return;
        }

        String sql = "INSERT INTO factura_venta_detalle (id_fact_venta_cab, id_articulo, id_deposito, " +
                    "fact_venta_cantidad, fact_venta_precio_venta) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (FacturaVentaDetalle detalle : detalles) {
                stmt.setLong(1, detalle.getFacturaVenta().getIdFacturaVenta());
                stmt.setLong(2, detalle.getArticulo().getIdArticulo());
                stmt.setLong(3, detalle.getDeposito().getIdDeposito());
                stmt.setInt(4, detalle.getCantidad());
                stmt.setLong(5, detalle.getPrecioVenta());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void actualizarFacturaVentaDetalle(FacturaVentaDetalle detalle) throws SQLException {
        if (detalle == null || detalle.getFacturaVenta() == null || detalle.getArticulo() == null) {
            LOGGER.log(Level.WARNING, "Error: detalle o sus claves son nulas");
            return;
        }

        String sql = "UPDATE factura_venta_detalle SET fact_venta_cantidad = ?, fact_venta_precio_venta = ? " +
                    "WHERE id_fact_venta_cab = ? AND id_articulo = ? AND id_deposito = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detalle.getCantidad());
            stmt.setLong(2, detalle.getPrecioVenta());
            stmt.setLong(3, detalle.getFacturaVenta().getIdFacturaVenta());
            stmt.setLong(4, detalle.getArticulo().getIdArticulo());
            stmt.setLong(5, detalle.getDeposito().getIdDeposito());

            stmt.executeUpdate();
        }
    }

    public void eliminarFacturaVentaDetalle(Long idFacturaVenta, Long idArticulo, Long idDeposito) throws SQLException {
        if (idFacturaVenta == null || idArticulo == null || idDeposito == null) {
            LOGGER.log(Level.WARNING, "Error: parámetros nulos");
            return;
        }

        String sql = "DELETE FROM factura_venta_detalle WHERE id_fact_venta_cab = ? AND id_articulo = ? AND id_deposito = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaVenta);
            stmt.setLong(2, idArticulo);
            stmt.setLong(3, idDeposito);
            stmt.executeUpdate();
        }
    }

    public void eliminarDetallesPorFactura(Long idFacturaVenta) throws SQLException {
        if (idFacturaVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaVenta es nulo");
            return;
        }

        String sql = "DELETE FROM factura_venta_detalle WHERE id_fact_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaVenta);
            stmt.executeUpdate();
        }
    }
}
