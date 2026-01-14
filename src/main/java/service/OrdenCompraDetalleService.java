/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.OrdenCompraDetalle;
import modelo.OrdenCompraDetalleDAO;

/**
 *
 * @author Miguel
 */
public class OrdenCompraDetalleService {

    public List<OrdenCompraDetalle> listarDetallesPorOrdenCompra(Long idOrdenCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            OrdenCompraDetalleDAO detalleDAO = new OrdenCompraDetalleDAO(conn);
            return detalleDAO.listarDetallesPorOrdenCompra(idOrdenCompra);
        } catch (SQLException e) {
            System.out.println("Error en OrdenCompraDetalleService: " + e);
            return null;
        }
    }

    public boolean insertarDetalle(OrdenCompraDetalle detalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenCompraDetalleDAO detalleDAO = new OrdenCompraDetalleDAO(conn);
            boolean resultado = detalleDAO.insertarDetalle(detalle);
            conn.commit();
            return resultado;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenCompraDetalleService: " + e);
            return false;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public boolean eliminarDetalle(Long idOrdenCompra, Long idArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenCompraDetalleDAO detalleDAO = new OrdenCompraDetalleDAO(conn);
            boolean resultado = detalleDAO.eliminarDetalle(idOrdenCompra, idArticulo);
            conn.commit();
            return resultado;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenCompraDetalleService: " + e);
            return false;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void actualizarDetalles(Long idOrdenCompra, List<OrdenCompraDetalle> detalles) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            OrdenCompraDetalleDAO detalleDAO = new OrdenCompraDetalleDAO(conn);
            detalleDAO.actualizarOrdenCompraDetalles(idOrdenCompra, detalles);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en OrdenCompraDetalleService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
