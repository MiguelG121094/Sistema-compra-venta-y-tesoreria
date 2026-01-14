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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Miguel
 */
public class PresupuestoDetalleDAO {

    private Connection conn;
    private ArticuloDAO articuloDAO;
    private PresupuestoDAO presupuestoDAO;

    public PresupuestoDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public List<PresupuestoDetalle> listarDetallesPorPresupuesto(Long idPresupuestoCab) throws SQLException {
        if (idPresupuestoCab == null) {
            System.out.println("El parametro idPresupuestoCab es nulo en: PresupuestoDetalleDAO().listarDetallesPorPresupuesto");
            return null;
        }
        List<PresupuestoDetalle> detalles = new ArrayList<>();
        String sql = "SELECT * FROM presupuesto_detalle WHERE id_presupuesto_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPresupuestoCab);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                articuloDAO = new ArticuloDAO(conn);
                presupuestoDAO = new PresupuestoDAO(conn);
                Presupuesto presupuesto = presupuestoDAO.getPresupuesto(rs.getLong("id_presupuesto_cab"));
                Articulo articulo = articuloDAO.getArticulo(rs.getLong("id_articulo"));
                Long cantidad = rs.getLong("presu_det_cantidad");
                Long precioCompra = rs.getLong("presu_det_precio_compra");

                PresupuestoDetalle detalle = new PresupuestoDetalle(presupuesto, articulo, cantidad, precioCompra);
                detalle.setDescuento(rs.getLong("presu_det_descuento"));
                detalles.add(detalle);
            }
        }
        return detalles;
    }
    
    public boolean insertarDetalle(PresupuestoDetalle detalle) throws SQLException {
        if (detalle == null || detalle.getPresupuesto() == null || detalle.getArticulo() == null) {
            System.out.println("Error: Detalle de presupuesto inválido.");
            return false;
        }

        String sql = "INSERT INTO presupuesto_detalle(id_presupuesto_cab, id_articulo, presu_det_cantidad, presu_det_precio_compra, presu_det_descuento) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getPresupuesto().getIdPresupuesto());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setLong(3, detalle.getCantidad());
            stmt.setLong(4, detalle.getPrecioCompra());
            if (detalle.getDescuento() != null) {
                stmt.setLong(5, detalle.getDescuento());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean eliminarDetalle(Long idPresupuesto, Long idArticulo) throws SQLException {
        if (idPresupuesto == null || idArticulo == null) {
            System.out.println("Error id del pedido o artículo nulo.");
            return false;
        }

        String sql = "DELETE FROM presupuesto_detalle WHERE id_presupuesto_cab = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPresupuesto);
            stmt.setLong(2, idArticulo);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public void actualizarPresupuestoDetalles(Long idPresupuesto, List<PresupuestoDetalle> detalles) throws SQLException {
        if (idPresupuesto == null || detalles == null) {
            System.out.println("Error: Parámetros inválidos para actualizar detalles");
            return;
        }

        boolean autoCommitOriginal = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false); //deshabilitar el autocommit para manejar la transacción manualmente

            String updateDetalle = "UPDATE public.presupuesto_detalle " +
                                "SET presu_det_cantidad=?, presu_det_precio_compra=?, presu_det_descuento=? " +
                                "WHERE id_presupuesto_cab = ? AND id_articulo = ?";

            try (PreparedStatement stmtUpdate = conn.prepareStatement(updateDetalle)) {
                //obtiene los detalles actuales del pedido en la base de datos
                Set<Long> detallesExistentes = obtenerDetallesExistentes(conn, idPresupuesto);

                for (PresupuestoDetalle detalle : detalles) {
                    Long idArticulo = detalle.getArticulo().getIdArticulo();

                    if (detallesExistentes.contains(idArticulo)) {
                        // actualiza el detalle existente
                        stmtUpdate.setLong(1, detalle.getCantidad());
                        stmtUpdate.setLong(2, detalle.getPrecioCompra());
                        if (detalle.getDescuento() != null) {
                            stmtUpdate.setLong(3, detalle.getDescuento());
                        } else {
                            stmtUpdate.setNull(3, java.sql.Types.INTEGER);
                        }
                        stmtUpdate.setLong(4, idPresupuesto);
                        stmtUpdate.setLong(5, idArticulo);
                        stmtUpdate.addBatch(); // Agregar la actualización al batch
                    } else {
                        // sino inserta el detalle
                        insertarDetalle(detalle);
                    }

                    //marcar el detalle como procesado
                    detallesExistentes.remove(idArticulo);
                }

                //eliminar los detalles que ya no están en la lista
                for (Long idArticulo : detallesExistentes) {
                    eliminarDetalle(idPresupuesto, idArticulo);
                }

                // Ejecutar los batches
                stmtUpdate.executeBatch();
            }

            conn.commit(); // Confirmar la transacción
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir la transacción en caso de error
            }
            throw e; // Relanzar la excepción para manejarla en el servlet
        } finally {
            conn.setAutoCommit(autoCommitOriginal); // Restaurar el estado original del autocommit
        }
    }
    
    //mtodo  para obtener los detalles existentes en la base de datos
    private Set<Long> obtenerDetallesExistentes(Connection conn, Long idPresupuesto) throws SQLException {
        Set<Long> detallesExistentes = new HashSet<>();
        String sql = "SELECT id_articulo FROM presupuesto_detalle WHERE id_presupuesto_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPresupuesto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detallesExistentes.add(rs.getLong("id_articulo"));
                }
            }
        }
        return detallesExistentes;
    }

    /**
     * Obtiene las cantidades totales ya presupuestadas para cada artículo de un pedido de compra.
     * Suma las cantidades de todos los presupuestos asociados al pedido.
     *
     * @param idPedidoCompra ID del pedido de compra
     * @return Map con idArticulo como clave y cantidad total presupuestada como valor
     */
    public Map<Long, Long> obtenerCantidadesPresupuestadasPorPedido(Long idPedidoCompra) throws SQLException {
        Map<Long, Long> cantidadesPorArticulo = new HashMap<>();

        if (idPedidoCompra == null) {
            System.out.println("Error: idPedidoCompra es nulo en obtenerCantidadesPresupuestadasPorPedido");
            return cantidadesPorArticulo;
        }

        // Query que suma las cantidades de todos los presupuestos asociados al pedido
        String sql = "SELECT pd.id_articulo, SUM(pd.presu_det_cantidad) AS cantidad_total " +
                    "FROM presupuesto_detalle pd " +
                    "INNER JOIN presupuesto_cabecera pc ON pd.id_presupuesto_cab = pc.id_presupuesto_cab " +
                    "WHERE pc.id_pedido_cab = ? " +
                    "AND pc.presu_cab_estado NOT IN ('Anulado', 'Cancelado') " +
                    "GROUP BY pd.id_articulo";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long idArticulo = rs.getLong("id_articulo");
                    Long cantidadTotal = rs.getLong("cantidad_total");
                    cantidadesPorArticulo.put(idArticulo, cantidadTotal);
                }
            }
        }

        return cantidadesPorArticulo;
    }

}
