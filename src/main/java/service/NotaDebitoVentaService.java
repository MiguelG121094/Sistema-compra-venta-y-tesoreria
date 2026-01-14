/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.NotaDebitoVenta;
import modelo.NotaDebitoVentaDAO;

/**
 *
 * @author Miguel
 */
public class NotaDebitoVentaService {

    public NotaDebitoVenta getNotaDebitoVenta(Long idNotaDebitoVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoVentaDAO dao = new NotaDebitoVentaDAO(conn);
            return dao.getNotaDebitoVenta(idNotaDebitoVenta);
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoVentaService: " + e);
            return null;
        }
    }

    public List<NotaDebitoVenta> listarNotasDebitoVenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoVentaDAO dao = new NotaDebitoVentaDAO(conn);
            return dao.listarNotasDebitoVenta();
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoVentaService: " + e);
            return null;
        }
    }

    public Long insertarNotaDebitoVenta(NotaDebitoVenta nota) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoVentaDAO dao = new NotaDebitoVentaDAO(conn);
            idInserted = dao.insertarNotaDebitoVenta(nota);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaDebitoVenta(NotaDebitoVenta nota) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoVentaDAO dao = new NotaDebitoVentaDAO(conn);
            dao.actualizarNotaDebitoVenta(nota);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaDebitoVenta(Long idNotaDebitoVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoVentaDAO dao = new NotaDebitoVentaDAO(conn);
            dao.eliminarNotaDebitoVenta(idNotaDebitoVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
