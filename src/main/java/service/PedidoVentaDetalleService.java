/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.PedidoVentaDetalle;
import modelo.PedidoVentaDetalleDAO;

/**
 *
 * @author Miguel
 */
public class PedidoVentaDetalleService {

    public PedidoVentaDetalle getPedidoVentaDetalle(Long idPedidoVenta, Long idArticulo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            return dao.getPedidoVentaDetalle(idPedidoVenta, idArticulo);
        } catch (SQLException e) {
            System.out.println("Error en PedidoVentaDetalleService: " + e);
            return null;
        }
    }

    public List<PedidoVentaDetalle> listarDetallesPorPedido(Long idPedidoVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            return dao.listarDetallesPorPedido(idPedidoVenta);
        } catch (SQLException e) {
            System.out.println("Error en PedidoVentaDetalleService: " + e);
            return null;
        }
    }

    public void insertarPedidoVentaDetalle(PedidoVentaDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            dao.insertarPedidoVentaDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void insertarDetalles(List<PedidoVentaDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            dao.insertarDetalles(detalles);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void actualizarPedidoVentaDetalle(PedidoVentaDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            dao.actualizarPedidoVentaDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarPedidoVentaDetalle(Long idPedidoVenta, Long idArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            dao.eliminarPedidoVentaDetalle(idPedidoVenta, idArticulo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarDetallesPorPedido(Long idPedidoVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDetalleDAO dao = new PedidoVentaDetalleDAO(conn);
            dao.eliminarDetallesPorPedido(idPedidoVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
