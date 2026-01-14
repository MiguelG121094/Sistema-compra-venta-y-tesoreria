/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.FacturaVenta;
import modelo.FacturaVentaDAO;

/**
 *
 * @author Miguel
 */
public class FacturaVentaService {

    public FacturaVenta getFacturaVenta(Long idFacturaVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaVentaDAO dao = new FacturaVentaDAO(conn);
            return dao.getFacturaVenta(idFacturaVenta);
        } catch (SQLException e) {
            System.out.println("Error en FacturaVentaService: " + e);
            return null;
        }
    }

    public List<FacturaVenta> listarFacturasVenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaVentaDAO dao = new FacturaVentaDAO(conn);
            return dao.listarFacturasVenta();
        } catch (SQLException e) {
            System.out.println("Error en FacturaVentaService: " + e);
            return null;
        }
    }

    public Long insertarFacturaVenta(FacturaVenta factura) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDAO dao = new FacturaVentaDAO(conn);
            idInserted = dao.insertarFacturaVenta(factura);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarFacturaVenta(FacturaVenta factura) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDAO dao = new FacturaVentaDAO(conn);
            dao.actualizarFacturaVenta(factura);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarFacturaVenta(Long idFacturaVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaVentaDAO dao = new FacturaVentaDAO(conn);
            dao.eliminarFacturaVenta(idFacturaVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
