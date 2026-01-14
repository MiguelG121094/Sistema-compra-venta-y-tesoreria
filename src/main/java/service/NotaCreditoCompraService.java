/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.NotaCreditoCompra;
import modelo.NotaCreditoCompraDAO;

/**
 *
 * @author Miguel
 */
public class NotaCreditoCompraService {

    public NotaCreditoCompra getNotaCreditoCompra(Long idNotaCreditoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.getNotaCreditoCompra(idNotaCreditoCompra);
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoCompraService: " + e);
            return null;
        }
    }

    public List<NotaCreditoCompra> listarNotasCreditoCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.listarNotasCreditoCompra();
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoCompraService: " + e);
            return null;
        }
    }

    public Long insertarNotaCreditoCompra(NotaCreditoCompra notaCredito) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            idInserted = dao.insertarNotaCreditoCompra(notaCredito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaCreditoCompra(NotaCreditoCompra notaCredito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            dao.actualizarNotaCreditoCompra(notaCredito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaCreditoCompra(Long idNotaCreditoCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            dao.eliminarNotaCreditoCompra(idNotaCreditoCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
