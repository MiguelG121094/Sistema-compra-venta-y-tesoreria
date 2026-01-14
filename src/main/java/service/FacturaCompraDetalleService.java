/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.FacturaCompraDetalle;
import modelo.FacturaCompraDetalleDAO;

/**
 *
 * @author Miguel
 */
public class FacturaCompraDetalleService {

    public List<FacturaCompraDetalle> listarDetallesPorFactura(Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDetalleDAO detalleDAO = new FacturaCompraDetalleDAO(conn);
            return detalleDAO.listarDetallesPorFactura(idFacturaCompra);
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraDetalleService: " + e);
            return null;
        }
    }

    public boolean insertarDetalle(FacturaCompraDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaCompraDetalleDAO detalleDAO = new FacturaCompraDetalleDAO(conn);
            boolean resultado = detalleDAO.insertarDetalle(detalle);
            conn.commit();
            return resultado;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaCompraDetalleService: " + e);
            return false;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public boolean eliminarDetalle(Long idFacturaCompra, Long idArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaCompraDetalleDAO detalleDAO = new FacturaCompraDetalleDAO(conn);
            boolean resultado = detalleDAO.eliminarDetalle(idFacturaCompra, idArticulo);
            conn.commit();
            return resultado;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaCompraDetalleService: " + e);
            return false;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void actualizarDetalles(Long idFacturaCompra, List<FacturaCompraDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaCompraDetalleDAO detalleDAO = new FacturaCompraDetalleDAO(conn);
            detalleDAO.actualizarFacturaCompraDetalles(idFacturaCompra, detalles);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaCompraDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
