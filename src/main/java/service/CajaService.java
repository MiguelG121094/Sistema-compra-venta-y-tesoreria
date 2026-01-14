/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Caja;
import modelo.CajaDAO;

/**
 *
 * @author Miguel
 */
public class CajaService {

    public Caja getCaja(Long idCaja) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CajaDAO dao = new CajaDAO(conn);
            return dao.getCaja(idCaja);
        } catch (SQLException e) {
            System.out.println("Error en CajaService: " + e);
            return null;
        }
    }

    public List<Caja> listarCajas() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CajaDAO dao = new CajaDAO(conn);
            return dao.listarCajas();
        } catch (SQLException e) {
            System.out.println("Error en CajaService: " + e);
            return null;
        }
    }

    public List<Caja> listarCajasActivas() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CajaDAO dao = new CajaDAO(conn);
            return dao.listarCajasActivas();
        } catch (SQLException e) {
            System.out.println("Error en CajaService: " + e);
            return null;
        }
    }

    public Long insertarCaja(Caja caja) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CajaDAO dao = new CajaDAO(conn);
            idInserted = dao.insertarCaja(caja);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CajaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarCaja(Caja caja) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CajaDAO dao = new CajaDAO(conn);
            dao.actualizarCaja(caja);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CajaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarCaja(Long idCaja) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CajaDAO dao = new CajaDAO(conn);
            dao.eliminarCaja(idCaja);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CajaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
