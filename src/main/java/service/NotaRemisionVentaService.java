/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.NotaRemisionVenta;
import modelo.NotaRemisionVentaDAO;

/**
 *
 * @author Miguel
 */
public class NotaRemisionVentaService {

    public NotaRemisionVenta getNotaRemisionVenta(Long idNotaRemisionVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaRemisionVentaDAO dao = new NotaRemisionVentaDAO(conn);
            return dao.getNotaRemisionVenta(idNotaRemisionVenta);
        } catch (SQLException e) {
            System.out.println("Error en NotaRemisionVentaService: " + e);
            return null;
        }
    }

    public List<NotaRemisionVenta> listarNotasRemisionVenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaRemisionVentaDAO dao = new NotaRemisionVentaDAO(conn);
            return dao.listarNotasRemisionVenta();
        } catch (SQLException e) {
            System.out.println("Error en NotaRemisionVentaService: " + e);
            return null;
        }
    }

    public Long insertarNotaRemisionVenta(NotaRemisionVenta nota) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDAO dao = new NotaRemisionVentaDAO(conn);
            idInserted = dao.insertarNotaRemisionVenta(nota);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaRemisionVenta(NotaRemisionVenta nota) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDAO dao = new NotaRemisionVentaDAO(conn);
            dao.actualizarNotaRemisionVenta(nota);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaRemisionVenta(Long idNotaRemisionVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaRemisionVentaDAO dao = new NotaRemisionVentaDAO(conn);
            dao.eliminarNotaRemisionVenta(idNotaRemisionVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaRemisionVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
