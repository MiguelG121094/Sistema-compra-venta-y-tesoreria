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
public class FacturaCompraDetalleDAO {

    private Connection conn;
    private ArticuloDAO articuloDAO;

    public FacturaCompraDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public List<FacturaCompraDetalle> listarDetallesPorFactura(Long idFacturaCompra) throws SQLException {
        if (idFacturaCompra == null) {
            System.out.println("El parámetro idFacturaCompra es nulo en: FacturaCompraDetalleDAO().listarDetallesPorFactura");
            return null;
        }
        List<FacturaCompraDetalle> detalles = new ArrayList<>();
        String sql = "SELECT * FROM factura_compra_detalle WHERE id_fact_comp_cab = ?";
        articuloDAO = new ArticuloDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FacturaCompra facturaCompra = new FacturaCompra(rs.getLong("id_fact_comp_cab"));
                    Long idArticulo = rs.getLong("id_articulo");
                    Articulo articulo = idArticulo != 0 ? articuloDAO.getArticulo(idArticulo) : null;
                    Long cantidad = rs.getLong("fact_comp_cantidad");
                    Long precioCompra = rs.getLong("fact_comp_precio_compra");
                    String descripcion = rs.getString("fact_det_descripcion");

                    FacturaCompraDetalle detalle = new FacturaCompraDetalle(facturaCompra, articulo, cantidad, precioCompra, descripcion);
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public boolean insertarDetalle(FacturaCompraDetalle detalle) throws SQLException {
        if (detalle == null || detalle.getFacturaCompra() == null) {
            System.out.println("Error: Detalle de factura de compra inválido.");
            return false;
        }

        String sql = "INSERT INTO factura_compra_detalle (id_fact_comp_cab, id_articulo, fact_comp_cantidad, " +
                    "fact_comp_precio_compra, fact_det_descripcion) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getFacturaCompra().getIdFacturaCompra());
            if (detalle.getArticulo() != null) {
                stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            if (detalle.getCantidad() != null) {
                stmt.setLong(3, detalle.getCantidad());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            if (detalle.getPrecioCompra() != null) {
                stmt.setLong(4, detalle.getPrecioCompra());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setString(5, detalle.getDescripcion());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean eliminarDetalle(Long idFacturaCompra, Long idArticulo) throws SQLException {
        if (idFacturaCompra == null) {
            System.out.println("Error: id de la factura nulo.");
            return false;
        }

        String sql;
        if (idArticulo != null) {
            sql = "DELETE FROM factura_compra_detalle WHERE id_fact_comp_cab = ? AND id_articulo = ?";
        } else {
            sql = "DELETE FROM factura_compra_detalle WHERE id_fact_comp_cab = ? AND id_articulo IS NULL";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaCompra);
            if (idArticulo != null) {
                stmt.setLong(2, idArticulo);
            }

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public void actualizarFacturaCompraDetalles(Long idFacturaCompra, List<FacturaCompraDetalle> detalles) throws SQLException {
        if (idFacturaCompra == null || detalles == null) {
            System.out.println("Error: Parámetros inválidos para actualizar detalles");
            return;
        }

        // Eliminar detalles existentes y reinsertar
        String deleteAll = "DELETE FROM factura_compra_detalle WHERE id_fact_comp_cab = ?";
        try (PreparedStatement stmtDelete = conn.prepareStatement(deleteAll)) {
            stmtDelete.setLong(1, idFacturaCompra);
            stmtDelete.executeUpdate();
        }

        // Insertar todos los detalles nuevos
        for (FacturaCompraDetalle detalle : detalles) {
            detalle.setFacturaCompra(new FacturaCompra(idFacturaCompra));
            insertarDetalle(detalle);
        }
    }
}
