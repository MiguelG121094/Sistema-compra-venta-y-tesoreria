/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import conexion.Conexion;
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

        String sql = "INSERT INTO presupuesto_detalle(id_presupuesto_cab, id_articulo, presu_det_cantidad, presu_det_precio_compra) VALUES (?, ?, ?, ?);";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getPresupuesto().getIdPresupuesto());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setLong(3, detalle.getCantidad());
            stmt.setLong(4, detalle.getPrecioCompra());

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
        PreparedStatement stmt = null;
        try {
            conn = Conexion.getConnection();
            PreparedStatement stmtUpdate = null;
            conn.setAutoCommit(false); //deshabilitar el autocommit para manejar la transacción manualmente

            String updateDetalle = "UPDATE public.presupuesto_detalle " +
                                "SET presu_det_cantidad=?, presu_det_precio_compra=? " +
                                "WHERE id_presupuesto_cab = ? AND id_articulo = ?";
            
            stmtUpdate = conn.prepareStatement(updateDetalle);

            //obteniene los detalles actuales del pedido en la base de datos
            Set<Long> detallesExistentes = obtenerDetallesExistentes(conn, idPresupuesto);

        for (PresupuestoDetalle detalle : detalles) {
            Long idArticulo = detalle.getArticulo().getIdArticulo();

            if (detallesExistentes.contains(idArticulo)) {
                // actualiza el detalle existente
                stmtUpdate.setLong(1, detalle.getCantidad());
                stmtUpdate.setLong(2, detalle.getPrecioCompra());
                stmtUpdate.setLong(3, idPresupuesto);
                stmtUpdate.setLong(4, idArticulo);
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

            conn.commit(); // Confirmar la transacción
            conn.setAutoCommit(true); //habilita de vuelta el autocommit
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir la transacción en caso de error
            }
            throw e; // Relanzar la excepción para manejarla en el servlet
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

}
