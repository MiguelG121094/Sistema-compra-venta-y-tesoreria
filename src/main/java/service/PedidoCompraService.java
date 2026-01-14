/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.PedidoCompra;
import modelo.PedidoCompraDAO;
import modelo.PedidoCompraDetalle;
import modelo.PedidoCompraDetalleDAO;

/**
 *
 * @author Miguel
 */
public class PedidoCompraService {
    
    public PedidoCompra getPedidoCompra(Long idPedidoCompra) throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            PedidoCompra pedidoCompra = pedidoCompraDAO.getPedidoCompra(idPedidoCompra);
            return pedidoCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public List<PedidoCompra> listarPedidos() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            List<PedidoCompra> pedidoCompra = pedidoCompraDAO.listarPedidos();
            return pedidoCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public List<PedidoCompra> listarPedidosConDetalles() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            List<PedidoCompra> pedidoCompra = pedidoCompraDAO.listarPedidosConDetalles();
            return pedidoCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public Long obtenerProximoIdPedidoCompra() throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            Long ultimoIdPedCompra = pedidoCompraDAO.obtenerProximoIdPedidoCompra();
            return ultimoIdPedCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public Long insertarPedido(PedidoCompra pedido) throws SQLException {
        Connection conn = null;
        Long idPedCabInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            idPedCabInserted = pedidoCompraDAO.insertarPedido(pedido);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idPedCabInserted;
    }
    
    public void insertarPedidoCabeceraYDetalle(PedidoCompra pedido, List<PedidoCompraDetalle> listapedidoDetalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.insertarPedido(pedido);
            
            //insert pedidoDetalle
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            for (PedidoCompraDetalle pedidoCompraDetalle : listapedidoDetalle) {
                if (pedidoCompraDetalle.getPedido() == null || pedidoCompraDetalle.getPedido().getIdPedido() == null) {
                    pedidoCompraDetalle.setPedido(pedido);
                }
                pedidoCompraDetalleDAO.insertarDetalle(pedidoCompraDetalle);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPedido(PedidoCompra pedido) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.actualizarPedido(pedido);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void eliminarPedido(Long idPedido) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.eliminarPedido(idPedido);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPedidoCabecera(PedidoCompra pedidoCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.actualizarPedidoCabecera(pedidoCompra);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
