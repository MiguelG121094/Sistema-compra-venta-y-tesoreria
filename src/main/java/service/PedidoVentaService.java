/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.PedidoVenta;
import modelo.PedidoVentaDAO;

/**
 *
 * @author Miguel
 */
public class PedidoVentaService {

    public PedidoVenta getPedidoVenta(Long idPedidoVenta) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PedidoVentaDAO dao = new PedidoVentaDAO(conn);
            return dao.getPedidoVenta(idPedidoVenta);
        } catch (SQLException e) {
            System.out.println("Error en PedidoVentaService: " + e);
            return null;
        }
    }

    public List<PedidoVenta> listarPedidosVenta() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PedidoVentaDAO dao = new PedidoVentaDAO(conn);
            return dao.listarPedidosVenta();
        } catch (SQLException e) {
            System.out.println("Error en PedidoVentaService: " + e);
            return null;
        }
    }

    public List<PedidoVenta> listarPedidosVentaPendientes() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PedidoVentaDAO dao = new PedidoVentaDAO(conn);
            return dao.listarPedidosVentaPendientes();
        } catch (SQLException e) {
            System.out.println("Error en PedidoVentaService: " + e);
            return null;
        }
    }

    public Long insertarPedidoVenta(PedidoVenta pedidoVenta) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDAO dao = new PedidoVentaDAO(conn);
            idInserted = dao.insertarPedidoVenta(pedidoVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarPedidoVenta(PedidoVenta pedidoVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDAO dao = new PedidoVentaDAO(conn);
            dao.actualizarPedidoVenta(pedidoVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarPedidoVenta(Long idPedidoVenta) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoVentaDAO dao = new PedidoVentaDAO(conn);
            dao.eliminarPedidoVenta(idPedidoVenta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en PedidoVentaService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
