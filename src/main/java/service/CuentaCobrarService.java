/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.CuentaCobrar;
import modelo.CuentaCobrarDAO;

/**
 *
 * @author Miguel
 */
public class CuentaCobrarService {

    public CuentaCobrar getCuentaCobrar(Long idCuentaCobrar, Long idFacturaVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaCobrarDAO dao = new CuentaCobrarDAO(conn);
            return dao.getCuentaCobrar(idCuentaCobrar, idFacturaVenta);
        } catch (SQLException e) {
            System.out.println("Error en CuentaCobrarService: " + e);
            return null;
        }
    }

    public List<CuentaCobrar> listarCuentasCobrar() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaCobrarDAO dao = new CuentaCobrarDAO(conn);
            return dao.listarCuentasCobrar();
        } catch (SQLException e) {
            System.out.println("Error en CuentaCobrarService: " + e);
            return null;
        }
    }

    public List<CuentaCobrar> listarCuentasCobrarPendientes() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaCobrarDAO dao = new CuentaCobrarDAO(conn);
            return dao.listarCuentasCobrarPendientes();
        } catch (SQLException e) {
            System.out.println("Error en CuentaCobrarService: " + e);
            return null;
        }
    }

    public Long insertarCuentaCobrar(CuentaCobrar cuentaCobrar) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CuentaCobrarDAO dao = new CuentaCobrarDAO(conn);
            idInserted = dao.insertarCuentaCobrar(cuentaCobrar);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaCobrarService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarCuentaCobrar(CuentaCobrar cuentaCobrar) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CuentaCobrarDAO dao = new CuentaCobrarDAO(conn);
            dao.actualizarCuentaCobrar(cuentaCobrar);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaCobrarService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarCuentaCobrar(Long idCuentaCobrar, Long idFacturaVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CuentaCobrarDAO dao = new CuentaCobrarDAO(conn);
            dao.eliminarCuentaCobrar(idCuentaCobrar, idFacturaVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaCobrarService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
