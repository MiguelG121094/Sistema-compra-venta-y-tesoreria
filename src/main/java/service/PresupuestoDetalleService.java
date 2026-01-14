/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.PresupuestoDetalle;
import modelo.PresupuestoDetalleDAO;

/**
 *
 * @author Miguel
 */
public class PresupuestoDetalleService {
    
    public List<PresupuestoDetalle> listarDetallesPorPresupuesto(Long idPresupuestoCab) throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PresupuestoDetalleDAO presupuestoDetalleDAO = new PresupuestoDetalleDAO(conn);
            List<PresupuestoDetalle> presupuestoDetalle = presupuestoDetalleDAO.listarDetallesPorPresupuesto(idPresupuestoCab);
            return presupuestoDetalle;
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoDetalleService: " + e);
            return null;
        }
    }
    
    public void insertarDetalle(PresupuestoDetalle detalle) throws SQLException  {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PresupuestoDetalleDAO presupuestoDetalleDAO = new PresupuestoDetalleDAO(conn);
            presupuestoDetalleDAO.insertarDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PresupuestoDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void eliminarDetalle(Long idPresupuesto, Long idArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PresupuestoDetalleDAO presupuestoDetalleDAO = new PresupuestoDetalleDAO(conn);
            presupuestoDetalleDAO.eliminarDetalle(idPresupuesto, idArticulo);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PresupuestoDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPresupuestoDetalles(Long idPresupuesto, List<PresupuestoDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PresupuestoDetalleDAO presupuestoDetalleDAO = new PresupuestoDetalleDAO(conn);
            presupuestoDetalleDAO.actualizarPresupuestoDetalles(idPresupuesto, detalles);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PresupuestoDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Obtiene las cantidades totales ya presupuestadas para cada artículo de un pedido de compra.
     *
     * @param idPedidoCompra ID del pedido de compra
     * @return Map con idArticulo como clave y cantidad total presupuestada como valor
     */
    public Map<Long, Long> obtenerCantidadesPresupuestadasPorPedido(Long idPedidoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PresupuestoDetalleDAO presupuestoDetalleDAO = new PresupuestoDetalleDAO(conn);
            return presupuestoDetalleDAO.obtenerCantidadesPresupuestadasPorPedido(idPedidoCompra);
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoDetalleService: " + e);
            return new HashMap<>();
        }
    }
}
