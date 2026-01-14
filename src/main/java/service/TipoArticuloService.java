/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.TipoArticulo;
import modelo.TipoArticuloDAO;

/**
 *
 * @author Miguel
 */
public class TipoArticuloService {

    public List<TipoArticulo> listarTipoArticulo() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            TipoArticuloDAO tipoArticuloDAO = new TipoArticuloDAO(conn);
            List<TipoArticulo> tipoArticulos = tipoArticuloDAO.listarTipoArticulo();
            return tipoArticulos;
        } catch (SQLException e) {
            System.out.println("Error en TipoArticuloService: " + e);
            return null;
        }
    }

    public TipoArticulo cargarTipoaArticulo(Long id) throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            TipoArticuloDAO tipoArticuloDAO = new TipoArticuloDAO(conn);
            TipoArticulo tipoArticulo = tipoArticuloDAO.cargarTipoaArticulo(id);
            return tipoArticulo;
        } catch (SQLException e) {
            System.out.println("Error en TipoArticuloService: " + e);
            return null;
        }
    }

    public void insertarTipoArticulo(TipoArticulo tipoArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            TipoArticuloDAO tipoArticuloDAO = new TipoArticuloDAO(conn);
            tipoArticuloDAO.insertarTipoArticulo(tipoArticulo);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en TipoArticuloService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarTipoArticulo(TipoArticulo tipoArticulo) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            TipoArticuloDAO tipoArticuloDAO = new TipoArticuloDAO(conn);
            tipoArticuloDAO.actualizarTipoArticulo(tipoArticulo);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en TipoArticuloService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void eliminarTipoArticulo(Long id) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            TipoArticuloDAO tipoArticuloDAO = new TipoArticuloDAO(conn);
            tipoArticuloDAO.eliminarTipoArticulo(id);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en TipoArticuloService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
