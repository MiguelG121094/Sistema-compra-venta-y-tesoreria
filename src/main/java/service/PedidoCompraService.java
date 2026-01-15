/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.PedidoCompra;
import modelo.PedidoCompraDAO;
import modelo.PedidoCompraDetalle;
import modelo.PedidoCompraDetalleDAO;
import modelo.PresupuestoDetalleDAO;

/**
 *
 * @author Miguel
 */
public class PedidoCompraService {
    
    public PedidoCompra getPedidoCompra(Long idPedidoCompra) throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            PedidoCompra pedidoCompra = pedidoCompraDAO.getPedidoCompra(idPedidoCompra);
            return pedidoCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public List<PedidoCompra> listarPedidos() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            List<PedidoCompra> pedidoCompra = pedidoCompraDAO.listarPedidos();
            return pedidoCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public List<PedidoCompra> listarPedidosConDetalles() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            List<PedidoCompra> pedidoCompra = pedidoCompraDAO.listarPedidosConDetalles();
            return pedidoCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }

    /**
     * Lista los pedidos de compra mostrando solo los artículos pendientes de presupuestar.
     * Útil para el modal de selección de pedidos en el módulo de presupuestos.
     * NOTA: Toda la lógica se realiza en SQL.
     *
     * @return Lista de pedidos con artículos pendientes
     */
    public List<PedidoCompra> listarPedidosConArticulosPendientesLogicaSQL() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            return pedidoCompraDAO.listarPedidosConArticulosPendientesLogicaSQL();
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }

    /**
     * Lista los pedidos de compra mostrando solo los artículos pendientes de presupuestar.
     * Si todos los artículos ya fueron presupuestados, muestra el detalle completo y marca
     * el pedido como presupuestoCompleto = true.
     * Útil para el modal de selección de pedidos en el módulo de presupuestos.
     * NOTA: La lógica de cálculo se realiza en Java.
     *
     * @return Lista de pedidos con artículos pendientes o completos
     */
    public List<PedidoCompra> listarPedidosConArticulosPendientes() throws SQLException {
        List<PedidoCompra> pedidosConPendientes = new ArrayList<>();

        try (Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            PresupuestoDetalleDAO presupuestoDetalleDAO = new PresupuestoDetalleDAO(conn);

            // Obtener todos los pedidos con sus detalles completos
            List<PedidoCompra> pedidos = pedidoCompraDAO.listarPedidosConDetalles();

            for (PedidoCompra pedido : pedidos) {
                // Obtener detalles del pedido
                List<PedidoCompraDetalle> detallesPedido = pedidoCompraDetalleDAO.listarDetallesPorPedido(pedido.getIdPedido());

                // Obtener cantidades ya presupuestadas para este pedido
                Map<Long, Long> cantidadesPresupuestadas = presupuestoDetalleDAO
                        .obtenerCantidadesPresupuestadasPorPedido(pedido.getIdPedido());

                // Construir string de artículos pendientes y completos
                StringBuilder articulosPendientes = new StringBuilder();
                StringBuilder articulosCompletos = new StringBuilder();
                boolean hayPendientes = false;

                for (PedidoCompraDetalle detalle : detallesPedido) {
                    Long idArticulo = detalle.getArticulo().getIdArticulo();
                    Long cantidadPedida = detalle.getCantidad();
                    Long cantidadPresupuestada = cantidadesPresupuestadas.getOrDefault(idArticulo, 0L);
                    Long cantidadPendiente = cantidadPedida - cantidadPresupuestada;

                    // Construir lista de artículos completos (para mostrar si todo está presupuestado)
                    if (articulosCompletos.length() > 0) {
                        articulosCompletos.append(", ");
                    }
                    articulosCompletos.append(detalle.getArticulo().getDescripcion())
                            .append(" (Cant: ")
                            .append(cantidadPedida)
                            .append(")");

                    // Solo agregar a pendientes si hay cantidad pendiente
                    if (cantidadPendiente > 0) {
                        hayPendientes = true;
                        if (articulosPendientes.length() > 0) {
                            articulosPendientes.append(", ");
                        }
                        articulosPendientes.append(detalle.getArticulo().getDescripcion())
                                .append(" (Cant: ")
                                .append(cantidadPendiente)
                                .append(")");
                    }
                }

                // Si hay artículos pendientes, mostrar solo esos; sino mostrar todos
                if (hayPendientes) {
                    pedido.setListaArticulos(articulosPendientes.toString());
                    pedido.setPresupuestoCompleto(false);
                } else {
                    pedido.setListaArticulos(articulosCompletos.toString());
                    pedido.setPresupuestoCompleto(true);
                }

                pedidosConPendientes.add(pedido);
            }
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService.listarPedidosConArticulosPendientes: " + e);
            return null;
        }

        return pedidosConPendientes;
    }

    public Long obtenerProximoIdPedidoCompra() throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            Long ultimoIdPedCompra = pedidoCompraDAO.obtenerProximoIdPedidoCompra();
            return ultimoIdPedCompra;
        } catch (SQLException e) {
            System.out.println("Error en PedidoCompraService: " + e);
            return null;
        }
    }
    
    public Long insertarPedido(PedidoCompra pedido) throws SQLException {
        Connection conn = null;
        Long idPedCabInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            idPedCabInserted = pedidoCompraDAO.insertarPedido(pedido);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idPedCabInserted;
    }
    
    public void insertarPedidoCabeceraYDetalle(PedidoCompra pedido, List<PedidoCompraDetalle> listapedidoDetalle) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.insertarPedido(pedido);
            
            //insert pedidoDetalle
            PedidoCompraDetalleDAO pedidoCompraDetalleDAO = new PedidoCompraDetalleDAO(conn);
            for (PedidoCompraDetalle pedidoCompraDetalle : listapedidoDetalle) {
                if (pedidoCompraDetalle.getPedido() == null || pedidoCompraDetalle.getPedido().getIdPedido() == null) {
                    pedidoCompraDetalle.setPedido(pedido);
                }
                pedidoCompraDetalleDAO.insertarDetalle(pedidoCompraDetalle);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPedido(PedidoCompra pedido) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.actualizarPedido(pedido);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void eliminarPedido(Long idPedido) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.eliminarPedido(idPedido);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    public void actualizarPedidoCabecera(PedidoCompra pedidoCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PedidoCompraDAO pedidoCompraDAO = new PedidoCompraDAO(conn);
            pedidoCompraDAO.actualizarPedidoCabecera(pedidoCompra);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PedidoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
