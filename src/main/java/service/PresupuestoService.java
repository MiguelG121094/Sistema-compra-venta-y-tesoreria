/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Presupuesto;
import modelo.PresupuestoDAO;

/**
 *
 * @author Miguel
 */
public class PresupuestoService {
    
    public Presupuesto getPresupuesto(Long idPresupuesto) throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            Presupuesto presupuesto = presupuestoDAO.getPresupuesto(idPresupuesto);
            return presupuesto;
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoService: " + e);
            return null;
        }
    }
    
    public List<Presupuesto> listarPresupuesto() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            List<Presupuesto> presupuestos = presupuestoDAO.listarPresupuesto();
            return presupuestos;
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoService: " + e);
            return null;
        }
    }
    
    public List<Presupuesto> listarPresupuestoConDetalles() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            List<Presupuesto> presupuestos = presupuestoDAO.listarPresupuestoConDetalles();
            return presupuestos;
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoService: " + e);
            return null;
        }
    }
    
    public Long obtenerProximoIdPresupuesto() throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            Long ultimoIdPresupuesto = presupuestoDAO.obtenerProximoIdPresupuestoCompra();
            return ultimoIdPresupuesto;
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoService: " + e);
            return null;
        }
    }
    
    public Long insertarPresupuesto(Presupuesto presupuesto) throws SQLException {
        Connection conn = null;
        Long idPresuCabInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            idPresuCabInserted = presupuestoDAO.insertarPresupuesto(presupuesto);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PresupuestoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idPresuCabInserted;
    }
    
    public void actualizarPresupuesto(Presupuesto presupuesto) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            presupuestoDAO.actualizarPresupuesto(presupuesto);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PresupuestoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPresupuestoCabecera(Presupuesto presupuesto) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            presupuestoDAO.actualizarPresupuestoCabecera(presupuesto);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PresupuestoService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
