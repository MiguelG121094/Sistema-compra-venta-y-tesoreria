/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.NotaDebitoCompra;
import modelo.NotaDebitoCompraDAO;

/**
 *
 * @author Miguel
 */
public class NotaDebitoCompraService {

    public NotaDebitoCompra getNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            return dao.getNotaDebitoCompra(idNotaDebitoCompra);
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoCompraService: " + e);
            return null;
        }
    }

    public List<NotaDebitoCompra> listarNotasDebitoCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            return dao.listarNotasDebitoCompra();
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoCompraService: " + e);
            return null;
        }
    }

    public Long insertarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            idInserted = dao.insertarNotaDebitoCompra(notaDebito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            dao.actualizarNotaDebitoCompra(notaDebito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            dao.eliminarNotaDebitoCompra(idNotaDebitoCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
