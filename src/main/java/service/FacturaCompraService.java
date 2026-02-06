/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.FacturaCompra;
import modelo.FacturaCompraDAO;

/**
 *
 * @author Miguel
 */
public class FacturaCompraService {

    public FacturaCompra getFacturaCompra(Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            return facturaCompraDAO.getFacturaCompra(idFacturaCompra);
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraService: " + e);
            return null;
        }
    }

    public List<FacturaCompra> listarFacturasCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            return facturaCompraDAO.listarFacturasCompra();
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraService: " + e);
            return null;
        }
    }

    public Long obtenerProximoIdFacturaCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            return facturaCompraDAO.obtenerProximoIdFacturaCompra();
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraService: " + e);
            return null;
        }
    }

    public Long insertarFacturaCompra(FacturaCompra facturaCompra) throws SQLException {
        Connection conn = null;
        Long idFacturaInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            idFacturaInserted = facturaCompraDAO.insertarFacturaCompra(facturaCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idFacturaInserted;
    }

    public void actualizarFacturaCompra(FacturaCompra facturaCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            facturaCompraDAO.actualizarFacturaCompra(facturaCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarFacturaCompra(Long idFacturaCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            facturaCompraDAO.eliminarFacturaCompra(idFacturaCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en FacturaCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Verifica si existe al menos una factura de compra asociada a un pedido de compra.
     *
     * @param idPedidoCompra ID del pedido de compra
     * @return true si existe al menos una factura de compra, false en caso contrario
     */
    public boolean existeFacturaCompraPorPedido(Long idPedidoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            return facturaCompraDAO.existeFacturaCompraPorPedido(idPedidoCompra);
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraService: " + e);
            return false;
        }
    }

    /**
     * Verifica si existe al menos una factura de compra asociada a un presupuesto.
     *
     * @param idPresupuesto ID del presupuesto
     * @return true si existe al menos una factura de compra, false en caso contrario
     */
    public boolean existeFacturaCompraPorPresupuesto(Long idPresupuesto) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            return facturaCompraDAO.existeFacturaCompraPorPresupuesto(idPresupuesto);
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraService: " + e);
            return false;
        }
    }

    /**
     * Verifica si existe al menos una factura de compra asociada a una orden de compra.
     *
     * @param idOrdenCompra ID de la orden de compra
     * @return true si existe al menos una factura de compra, false en caso contrario
     */
    public boolean existeFacturaCompraPorOrden(Long idOrdenCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            return facturaCompraDAO.existeFacturaCompraPorOrden(idOrdenCompra);
        } catch (SQLException e) {
            System.out.println("Error en FacturaCompraService: " + e);
            return false;
        }
    }
}
