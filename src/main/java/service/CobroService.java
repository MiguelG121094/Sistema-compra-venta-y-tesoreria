/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Cobro;
import modelo.CobroDAO;

/**
 *
 * @author Miguel
 */
public class CobroService {

    public Cobro getCobro(Long idCobro) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CobroDAO dao = new CobroDAO(conn);
            return dao.getCobro(idCobro);
        } catch (SQLException e) {
            System.out.println("Error en CobroService: " + e);
            return null;
        }
    }

    public List<Cobro> listarCobros() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CobroDAO dao = new CobroDAO(conn);
            return dao.listarCobros();
        } catch (SQLException e) {
            System.out.println("Error en CobroService: " + e);
            return null;
        }
    }

    public Long insertarCobro(Cobro cobro) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CobroDAO dao = new CobroDAO(conn);
            idInserted = dao.insertarCobro(cobro);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CobroService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarCobro(Cobro cobro) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CobroDAO dao = new CobroDAO(conn);
            dao.actualizarCobro(cobro);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CobroService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarCobro(Long idCobro) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CobroDAO dao = new CobroDAO(conn);
            dao.eliminarCobro(idCobro);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CobroService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
