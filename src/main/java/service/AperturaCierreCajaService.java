/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.AperturaCierreCaja;
import modelo.AperturaCierreCajaDAO;

/**
 *
 * @author Miguel
 */
public class AperturaCierreCajaService {

    public AperturaCierreCaja getAperturaCierreCaja(Long idAperturaCierreCaja) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            AperturaCierreCajaDAO dao = new AperturaCierreCajaDAO(conn);
            return dao.getAperturaCierreCaja(idAperturaCierreCaja);
        } catch (SQLException e) {
            System.out.println("Error en AperturaCierreCajaService: " + e);
            return null;
        }
    }

    public List<AperturaCierreCaja> listarAperturasCierreCaja() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            AperturaCierreCajaDAO dao = new AperturaCierreCajaDAO(conn);
            return dao.listarAperturasCierreCaja();
        } catch (SQLException e) {
            System.out.println("Error en AperturaCierreCajaService: " + e);
            return null;
        }
    }

    public List<AperturaCierreCaja> listarAperturasAbiertas() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            AperturaCierreCajaDAO dao = new AperturaCierreCajaDAO(conn);
            return dao.listarAperturasAbiertas();
        } catch (SQLException e) {
            System.out.println("Error en AperturaCierreCajaService: " + e);
            return null;
        }
    }

    public Long insertarAperturaCierreCaja(AperturaCierreCaja apertura) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            AperturaCierreCajaDAO dao = new AperturaCierreCajaDAO(conn);
            idInserted = dao.insertarAperturaCierreCaja(apertura);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en AperturaCierreCajaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarAperturaCierreCaja(AperturaCierreCaja apertura) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            AperturaCierreCajaDAO dao = new AperturaCierreCajaDAO(conn);
            dao.actualizarAperturaCierreCaja(apertura);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en AperturaCierreCajaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarAperturaCierreCaja(Long idAperturaCierreCaja) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            AperturaCierreCajaDAO dao = new AperturaCierreCajaDAO(conn);
            dao.eliminarAperturaCierreCaja(idAperturaCierreCaja);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en AperturaCierreCajaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
