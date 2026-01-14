/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.NotaCreditoVenta;
import modelo.NotaCreditoVentaDAO;

/**
 *
 * @author Miguel
 */
public class NotaCreditoVentaService {

    public NotaCreditoVenta getNotaCreditoVenta(Long idNotaCreditoVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoVentaDAO dao = new NotaCreditoVentaDAO(conn);
            return dao.getNotaCreditoVenta(idNotaCreditoVenta);
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoVentaService: " + e);
            return null;
        }
    }

    public List<NotaCreditoVenta> listarNotasCreditoVenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoVentaDAO dao = new NotaCreditoVentaDAO(conn);
            return dao.listarNotasCreditoVenta();
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoVentaService: " + e);
            return null;
        }
    }

    public Long insertarNotaCreditoVenta(NotaCreditoVenta nota) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoVentaDAO dao = new NotaCreditoVentaDAO(conn);
            idInserted = dao.insertarNotaCreditoVenta(nota);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaCreditoVenta(NotaCreditoVenta nota) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoVentaDAO dao = new NotaCreditoVentaDAO(conn);
            dao.actualizarNotaCreditoVenta(nota);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaCreditoVenta(Long idNotaCreditoVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoVentaDAO dao = new NotaCreditoVentaDAO(conn);
            dao.eliminarNotaCreditoVenta(idNotaCreditoVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
