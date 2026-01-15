/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.OrdenCompra;
import modelo.OrdenCompraDAO;

/**
 *
 * @author Miguel
 */
public class OrdenCompraService {

    public OrdenCompra getOrdenCompra(Long idOrdenCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            return ordenCompraDAO.getOrdenCompra(idOrdenCompra);
        } catch (SQLException e) {
            System.out.println("Error en OrdenCompraService: " + e);
            return null;
        }
    }

    public List<OrdenCompra> listarOrdenesCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            return ordenCompraDAO.listarOrdenesCompra();
        } catch (SQLException e) {
            System.out.println("Error en OrdenCompraService: " + e);
            return null;
        }
    }

    public List<OrdenCompra> listarOrdenesCompraConDetalles() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            return ordenCompraDAO.listarOrdenesCompraConDetalles();
        } catch (SQLException e) {
            System.out.println("Error en OrdenCompraService: " + e);
            return null;
        }
    }

    public Long obtenerProximoIdOrdenCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            return ordenCompraDAO.obtenerProximoIdOrdenCompra();
        } catch (SQLException e) {
            System.out.println("Error en OrdenCompraService: " + e);
            return null;
        }
    }

    public Long insertarOrdenCompra(OrdenCompra ordenCompra) throws SQLException {
        Connection conn = null;
        Long idOrdenCompraInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            idOrdenCompraInserted = ordenCompraDAO.insertarOrdenCompra(ordenCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idOrdenCompraInserted;
    }

    public void actualizarOrdenCompra(OrdenCompra ordenCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            ordenCompraDAO.actualizarOrdenCompra(ordenCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarOrdenCompra(Long idOrdenCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            ordenCompraDAO.eliminarOrdenCompra(idOrdenCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Verifica si existe al menos una orden de compra asociada a un pedido de compra.
     *
     * @param idPedidoCompra ID del pedido de compra
     * @return true si existe al menos una orden de compra, false en caso contrario
     */
    public boolean existeOrdenCompraPorPedido(Long idPedidoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
            return ordenCompraDAO.existeOrdenCompraPorPedido(idPedidoCompra);
        } catch (SQLException e) {
            System.out.println("Error en OrdenCompraService: " + e);
            return false;
        }
    }
}
