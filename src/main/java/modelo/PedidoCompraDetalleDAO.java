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
import javax.transaction.Transactional;

/**
 *
 * @author Miguel
 */
public class PedidoCompraDetalleDAO {
    
    private Connection conn;
    ArticuloDAO articuloDAO;
    DepositoDAO depositodAO;

    public PedidoCompraDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public List<PedidoCompraDetalle> listarDetallesPorPedido(Long idPedidoCab) throws SQLException  {
        if (idPedidoCab == null) {
            System.out.println("El parametro idPedidoCab es nulo en: PedidoCompraDetalleDAO().listarDetallesPorPedido");
            return null;
        }
        List<PedidoCompraDetalle> detalles = new ArrayList<>();
        String sql = "SELECT * FROM pedido_compra_detalle WHERE id_pedido_cab = ?";
        articuloDAO = new ArticuloDAO(conn);
        depositodAO = new DepositoDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoCab);
            try ( ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    PedidoCompra pedido = new PedidoCompra(rs.getLong("id_pedido_cab"));
                    Articulo articulo = articuloDAO.getArticulo(rs.getLong("id_articulo"));
                    Long cantidad = rs.getLong("ped_comp_det_cantidad");
                    Deposito deposito = depositodAO.getDepostio(rs.getLong("id_deposito"));

                    PedidoCompraDetalle detalle = new PedidoCompraDetalle(pedido, articulo, cantidad, deposito);
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }
    
    public boolean insertarDetalle(PedidoCompraDetalle detalle) throws SQLException  {
        if (detalle == null || detalle.getPedido() == null || detalle.getArticulo() == null || detalle.getDeposito() == null) {
            System.out.println("Error: Detalle de pedido inválido.");
            return false;
        }

        String sql = "INSERT INTO pedido_compra_detalle (id_pedido_cab, id_articulo, ped_comp_det_cantidad, id_deposito) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getPedido().getIdPedido());
            stmt.setLong(2, detalle.getArticulo().getIdArticulo());
            stmt.setLong(3, detalle.getCantidad());
            stmt.setLong(4, detalle.getDeposito().getIdDeposito());

            int filasAfectadas = stmt.executeUpdate();
//            return filasAfectadas > 0;
            return true;
        }
    }

    public boolean eliminarDetalle(Long idPedido, Long idArticulo) throws SQLException  {
        if (idPedido == null || idArticulo == null) {
            System.out.println("Error id del pedido o artículo nulo.");
            return false;
        }

        String sql = "DELETE FROM pedido_compra_detalle WHERE id_pedido_cab = ? AND id_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedido);
            stmt.setLong(2, idArticulo);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public void actualizarPedidoDetalles(Long idPedidoCab, List<PedidoCompraDetalle> detalles) throws SQLException {
        if (idPedidoCab == null || detalles == null) {
            System.out.println("Error: Detalle de pedido inválido");
            return;
        }
        String updateDetalle = "UPDATE pedido_compra_detalle " +
                               "SET ped_comp_det_cantidad = ?, id_deposito = ? " +
                               "WHERE id_pedido_cab = ? AND id_articulo = ?";
        
        try(PreparedStatement stmtUpdate = conn.prepareStatement(updateDetalle);){
            //obteniene los detalles actuales del pedido en la base de datos
            Set<Long> detallesExistentes = obtenerDetallesExistentes(idPedidoCab);

        for (PedidoCompraDetalle detalle : detalles) {
            Long idArticulo = detalle.getArticulo().getIdArticulo();

            if (detallesExistentes.contains(idArticulo)) {
                // actualiza el detalle existente
                stmtUpdate.setLong(1, detalle.getCantidad());
                stmtUpdate.setLong(2, detalle.getDeposito().getIdDeposito());
                stmtUpdate.setLong(3, idPedidoCab);
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
            eliminarDetalle(idPedidoCab, idArticulo);
        }

            // Ejecutar los batches
            stmtUpdate.executeBatch();
        }
    }
    
    // Método auxiliar para obtener los detalles existentes en la base de datos
    private Set<Long> obtenerDetallesExistentes(Long idPedidoCab) throws SQLException {
        Set<Long> detallesExistentes = new HashSet<>();
        String sql = "SELECT id_articulo FROM pedido_compra_detalle WHERE id_pedido_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoCab);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detallesExistentes.add(rs.getLong("id_articulo"));
                }
            }
        }
        return detallesExistentes;
    }

}
