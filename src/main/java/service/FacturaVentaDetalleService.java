/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.FacturaVentaDetalle;
import modelo.FacturaVentaDetalleDAO;

/**
 *
 * @author Miguel
 */
public class FacturaVentaDetalleService {

    public FacturaVentaDetalle getFacturaVentaDetalle(Long idFacturaVenta, Long idArticulo, Long idDeposito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            return dao.getFacturaVentaDetalle(idFacturaVenta, idArticulo, idDeposito);
        } catch (SQLException e) {
            System.out.println("Error en FacturaVentaDetalleService: " + e);
            return null;
        }
    }

    public List<FacturaVentaDetalle> listarDetallesPorFactura(Long idFacturaVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            return dao.listarDetallesPorFactura(idFacturaVenta);
        } catch (SQLException e) {
            System.out.println("Error en FacturaVentaDetalleService: " + e);
            return null;
        }
    }

    public void insertarFacturaVentaDetalle(FacturaVentaDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            dao.insertarFacturaVentaDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void insertarDetalles(List<FacturaVentaDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            dao.insertarDetalles(detalles);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void actualizarFacturaVentaDetalle(FacturaVentaDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            dao.actualizarFacturaVentaDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarFacturaVentaDetalle(Long idFacturaVenta, Long idArticulo, Long idDeposito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            dao.eliminarFacturaVentaDetalle(idFacturaVenta, idArticulo, idDeposito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarDetallesPorFactura(Long idFacturaVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDetalleDAO dao = new FacturaVentaDetalleDAO(conn);
            dao.eliminarDetallesPorFactura(idFacturaVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
