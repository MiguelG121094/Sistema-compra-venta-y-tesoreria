/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.OrdenPago;
import modelo.OrdenPagoDAO;

/**
 *
 * @author Miguel
 */
public class OrdenPagoService {

    public OrdenPago getOrdenPago(Long idOrdenPago) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenPagoDAO dao = new OrdenPagoDAO(conn);
            return dao.getOrdenPago(idOrdenPago);
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService: " + e);
            return null;
        }
    }

    public List<OrdenPago> listarOrdenesPago() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenPagoDAO dao = new OrdenPagoDAO(conn);
            return dao.listarOrdenesPago();
        } catch (SQLException e) {
            System.out.println("Error en OrdenPagoService: " + e);
            return null;
        }
    }

    public Long insertarOrdenPago(OrdenPago ordenPago) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenPagoDAO dao = new OrdenPagoDAO(conn);
            idInserted = dao.insertarOrdenPago(ordenPago);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenPagoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarOrdenPago(OrdenPago ordenPago) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenPagoDAO dao = new OrdenPagoDAO(conn);
            dao.actualizarOrdenPago(ordenPago);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenPagoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarOrdenPago(Long idOrdenPago) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenPagoDAO dao = new OrdenPagoDAO(conn);
            dao.eliminarOrdenPago(idOrdenPago);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenPagoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
