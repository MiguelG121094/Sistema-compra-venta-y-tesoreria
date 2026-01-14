/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.PedidoCompraDetalle;
import modelo.PedidoCompraDetalleDAO;

/**
 *
 * @author Miguel
 */
public class PedidoCompraDetalleService {
    
    public List<PedidoCompraDetalle> listarDetallesPorPedido(Long idPedidoCab) throws SQLException  {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            List<PedidoCompraDetalle> pedidoCompraDetalle = pedidoCompraDetalleDAO.listarDetallesPorPedido(idPedidoCab);
            return pedidoCompraDetalle;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraDetalleService: " + e);
            return null;
        }
    }
    
    public void insertarDetalle(PedidoCompraDetalle detalle) throws SQLException  {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            pedidoCompraDetalleDAO.insertarDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void eliminarDetalle(Long idPedido, Long idArticulo) throws SQLException  {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            pedidoCompraDetalleDAO.eliminarDetalle(idPedido, idArticulo);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en TipoArticuloService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPedidoDetalles(Long idPedidoCab, List<PedidoCompraDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            pedidoCompraDetalleDAO.actualizarPedidoDetalles(idPedidoCab, detalles);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
