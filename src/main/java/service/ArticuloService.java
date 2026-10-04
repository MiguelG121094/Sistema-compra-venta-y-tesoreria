/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.Connection;
import conexion.Conexion;
import java.sql.SQLException;
import java.util.List;
import modelo.Articulo;
import modelo.ArticuloDAO;
/**
 *
 * @author Miguel
 */
public class ArticuloService {
    
    public Articulo getArticulo(Long idArticulo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            ArticuloDAO articuloDAO = new ArticuloDAO(conn);
            Articulo articulo = articuloDAO.getArticulo(idArticulo);
            return articulo;
        } catch (SQLException e) {
            System.out.println("Error en ArticuloService: "+ e);
            return null;
        }
    }
     
    public List<Articulo> listarArticulo() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            ArticuloDAO articuloDAO = new ArticuloDAO(conn);
            List<Articulo> articulos = articuloDAO.listarArticulo();
            return articulos;
        } catch (SQLException e) {
            System.out.println("Error en ArticuloService: "+ e);
            return null;
        }
    }
    

    // ==================== ABM ====================

    /** Codigo de PostgreSQL para violacion de clave foranea. */
    private static final String SQLSTATE_FK = "23503";

    public Long insertarArticulo(Articulo articulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            Long id = new ArticuloDAO(conn).insertarArticulo(articulo);
            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ArticuloService.insertarArticulo: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    public void actualizarArticulo(Articulo articulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new ArticuloDAO(conn).actualizarArticulo(articulo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ArticuloService.actualizarArticulo: " + e);
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    /**
     * Borra el articulo. Un articulo con movimientos no se puede borrar: lo referencian los
     * detalles de pedido, presupuesto, orden de compra y factura, y las filas de stock. En vez de
     * dejar salir el error crudo de la base, se traduce a un mensaje entendible.
     */
    public void eliminarArticulo(Long idArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            new ArticuloDAO(conn).eliminarArticulo(idArticulo);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ArticuloService.eliminarArticulo: " + e);
            if (SQLSTATE_FK.equals(e.getSQLState())) {
                throw new SQLException("El artículo no se puede eliminar porque tiene movimientos "
                        + "(pedidos, presupuestos, compras o stock). Désele de baja cambiando su "
                        + "estado a Inactivo.");
            }
            throw e;
        } finally {
            cerrar(conn);
        }
    }

    private void cerrar(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
