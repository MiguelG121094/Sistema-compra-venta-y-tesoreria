/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.NotaRemisionVentaDetalle;
import modelo.NotaRemisionVentaDetalleDAO;

/**
 *
 * @author Miguel
 */
public class NotaRemisionVentaDetalleService {

    public NotaRemisionVentaDetalle getNotaRemisionVentaDetalle(Long idNotaRemisionVenta, Long idArticulo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            return dao.getNotaRemisionVentaDetalle(idNotaRemisionVenta, idArticulo);
        } catch (SQLException e) {
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
            return null;
        }
    }

    public List<NotaRemisionVentaDetalle> listarDetallesPorNota(Long idNotaRemisionVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            return dao.listarDetallesPorNota(idNotaRemisionVenta);
        } catch (SQLException e) {
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
            return null;
        }
    }

    public void insertarNotaRemisionVentaDetalle(NotaRemisionVentaDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            dao.insertarNotaRemisionVentaDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void insertarDetalles(List<NotaRemisionVentaDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            dao.insertarDetalles(detalles);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void actualizarNotaRemisionVentaDetalle(NotaRemisionVentaDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            dao.actualizarNotaRemisionVentaDetalle(detalle);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaRemisionVentaDetalle(Long idNotaRemisionVenta, Long idArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            dao.eliminarNotaRemisionVentaDetalle(idNotaRemisionVenta, idArticulo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarDetallesPorNota(Long idNotaRemisionVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDetalleDAO dao = new NotaRemisionVentaDetalleDAO(conn);
            dao.eliminarDetallesPorNota(idNotaRemisionVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
