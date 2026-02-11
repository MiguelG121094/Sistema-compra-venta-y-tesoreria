/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Presupuesto;
import modelo.PresupuestoDAO;
import modelo.PresupuestoDetalle;
import modelo.PresupuestoDetalleDAO;

/**
 *
 * @author Miguel
 */
public class PresupuestoService {

    private static final Logger LOGGER = Logger.getLogger(PresupuestoService.class.getName());
    
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

    // ==================== MÉTODOS TRANSACCIONALES ====================

    /**
     * Guarda cabecera y detalles del presupuesto en una sola transacción.
     *
     * @param presupuesto el presupuesto a insertar
     * @param listaDetalle los detalles del presupuesto
     * @return ID del presupuesto insertado
     * @throws SQLException si ocurre un error (ya se hizo rollback)
     */
    public Long guardarPresupuestoCompleto(Presupuesto presupuesto, List<PresupuestoDetalle> listaDetalle) throws SQLException {
        Connection conn = null;
        Long idInsertado = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            idInsertado = presupuestoDAO.insertarPresupuesto(presupuesto);

            PresupuestoDetalleDAO detalleDAO = new PresupuestoDetalleDAO(conn);
            for (PresupuestoDetalle detalle : listaDetalle) {
                detalle.setPresupuesto(presupuesto);
                detalleDAO.insertarDetalle(detalle);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Presupuesto guardado completo. ID: {0}", idInsertado);
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error en guardarPresupuestoCompleto - rollback ejecutado", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInsertado;
    }

    /**
     * Actualiza cabecera y detalles del presupuesto en una sola transacción.
     *
     * @param presupuesto el presupuesto a actualizar
     * @param listaDetalle los detalles actualizados
     * @throws SQLException si ocurre un error (ya se hizo rollback)
     */
    public void actualizarPresupuestoCompleto(Presupuesto presupuesto, List<PresupuestoDetalle> listaDetalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            presupuestoDAO.actualizarPresupuestoCabecera(presupuesto);

            PresupuestoDetalleDAO detalleDAO = new PresupuestoDetalleDAO(conn);
            detalleDAO.actualizarPresupuestoDetalles(presupuesto.getIdPresupuesto(), listaDetalle);

            conn.commit();
            LOGGER.log(Level.INFO, "Presupuesto actualizado completo. ID: {0}", presupuesto.getIdPresupuesto());
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error en actualizarPresupuestoCompleto - rollback ejecutado", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Verifica si existe al menos un presupuesto asociado a un pedido de compra.
     *
     * @param idPedidoCompra ID del pedido de compra
     * @return true si existe al menos un presupuesto, false en caso contrario
     */
    public boolean existePresupuestoPorPedido(Long idPedidoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
            return presupuestoDAO.existePresupuestoPorPedido(idPedidoCompra);
        } catch (SQLException e) {
            System.out.println("Error en PresupuestoService: " + e);
            return false;
        }
    }
}
