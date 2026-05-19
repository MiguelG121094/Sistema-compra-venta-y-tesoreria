/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.CuentaPagar;
import modelo.CuentaPagarDAO;

/**
 *
 * @author Miguel
 */
public class CuentaPagarService {

    public CuentaPagar getCuentaPagar(Long idCuentaPagar, Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            return dao.getCuentaPagar(idCuentaPagar, idFacturaCompra);
        } catch (SQLException e) {
            System.out.println("Error en CuentaPagarService: " + e);
            return null;
        }
    }

    public CuentaPagar getByFactura(Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            return dao.getByFactura(idFacturaCompra);
        } catch (SQLException e) {
            System.out.println("Error en CuentaPagarService: " + e);
            return null;
        }
    }

    public List<CuentaPagar> listarCuentasPagar() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            return dao.listarCuentasPagar();
        } catch (SQLException e) {
            System.out.println("Error en CuentaPagarService: " + e);
            return null;
        }
    }

    public List<CuentaPagar> listarCuentasPagarPendientes() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            return dao.listarCuentasPagarPendientes();
        } catch (SQLException e) {
            System.out.println("Error en CuentaPagarService: " + e);
            return null;
        }
    }

    public Long insertarCuentaPagar(CuentaPagar cuentaPagar) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            idInserted = dao.insertarCuentaPagar(cuentaPagar);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaPagarService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarCuentaPagar(CuentaPagar cuentaPagar) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            dao.actualizarCuentaPagar(cuentaPagar);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaPagarService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarCuentaPagar(Long idCuentaPagar, Long idFacturaCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            CuentaPagarDAO dao = new CuentaPagarDAO(conn);
            dao.eliminarCuentaPagar(idCuentaPagar, idFacturaCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en CuentaPagarService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
