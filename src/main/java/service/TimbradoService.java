/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Timbrado;
import modelo.TimbradoDAO;

/**
 *
 * @author Miguel
 */
public class TimbradoService {

    public Timbrado getTimbrado(Long idTimbrado) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            TimbradoDAO dao = new TimbradoDAO(conn);
            return dao.getTimbrado(idTimbrado);
        } catch (SQLException e) {
            System.out.println("Error en TimbradoService: " + e);
            return null;
        }
    }

    public List<Timbrado> listarTimbrados() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            TimbradoDAO dao = new TimbradoDAO(conn);
            return dao.listarTimbrados();
        } catch (SQLException e) {
            System.out.println("Error en TimbradoService: " + e);
            return null;
        }
    }

    public List<Timbrado> listarTimbradosActivos() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            TimbradoDAO dao = new TimbradoDAO(conn);
            return dao.listarTimbradosActivos();
        } catch (SQLException e) {
            System.out.println("Error en TimbradoService: " + e);
            return null;
        }
    }

    public Long insertarTimbrado(Timbrado timbrado) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            TimbradoDAO dao = new TimbradoDAO(conn);
            idInserted = dao.insertarTimbrado(timbrado);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en TimbradoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarTimbrado(Timbrado timbrado) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            TimbradoDAO dao = new TimbradoDAO(conn);
            dao.actualizarTimbrado(timbrado);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en TimbradoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarTimbrado(Long idTimbrado) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            TimbradoDAO dao = new TimbradoDAO(conn);
            dao.eliminarTimbrado(idTimbrado);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en TimbradoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
