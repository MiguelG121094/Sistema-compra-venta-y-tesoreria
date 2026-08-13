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
import modelo.*;

/**
 *
 * @author Miguel
 */
public class FacturaCompraService {

    private static final Logger LOGGER = Logger.getLogger(FacturaCompraService.class.getName());

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
            LOGGER.log(Level.SEVERE, "Error en eliminarFacturaCompra - rollback ejecutado", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
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

    // ==================== MÉTODOS TRANSACCIONALES ====================

    /**
     * Guarda una factura de compra completa en una sola transacción:
     * 1. Inserta cabecera de factura
     * 2. Inserta todos los detalles
     * 3. Crea la cuenta a pagar
     * 4. Registra en el libro IVA compra
     * 5. Actualiza estados de documentos relacionados (Orden, Pedido, Presupuesto) a "Completado"
     *
     * Si cualquier paso falla, se hace rollback de todo.
     *
     * @param facturaCompra la factura a insertar
     * @param listaDetalle los detalles de la factura
     * @param cuentaPagar la cuenta a pagar a crear
     * @param libroIvaCompra el registro del libro IVA compra
     * @param ordenCompra la orden de compra relacionada (puede ser null)
     * @return ID de la factura insertada
     * @throws SQLException si ocurre un error (ya se hizo rollback)
     */
    public Long guardarFacturaCompleta(FacturaCompra facturaCompra,
            List<FacturaCompraDetalle> listaDetalle,
            CuentaPagar cuentaPagar,
            LibroIvaCompra libroIvaCompra,
            OrdenCompra ordenCompra) throws SQLException {

        Connection conn = null;
        Long idInsertado = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar cabecera
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            idInsertado = facturaCompraDAO.insertarFacturaCompra(facturaCompra);

            // 2. Insertar detalles
            FacturaCompraDetalleDAO detalleDAO = new FacturaCompraDetalleDAO(conn);
            for (FacturaCompraDetalle detalle : listaDetalle) {
                detalle.setFacturaCompra(new FacturaCompra(idInsertado));
                detalleDAO.insertarDetalle(detalle);
            }

            // 2b. Refrescar el precio de compra del catálogo con el de esta factura
            actualizarPrecioCompraCatalogo(conn, listaDetalle);

            // 3. Crear cuenta a pagar
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            cuentaPagar.setFacturaCompra(new FacturaCompra(idInsertado));
            cuentaPagarDAO.insertarCuentaPagar(cuentaPagar);

            // 4. Registrar en libro IVA compra
            LibroIvaCompraDAO libroIvaDAO = new LibroIvaCompraDAO(conn);
            libroIvaCompra.setFacturaCompra(new FacturaCompra(idInsertado));
            libroIvaDAO.insertarLibroIvaCompra(libroIvaCompra);

            // 5. Actualizar documentos relacionados a "Completado"
            if (ordenCompra != null) {
                OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
                ordenCompra.setEstado("Completado");
                ordenCompraDAO.actualizarOrdenCompra(ordenCompra);

                PedidoCompra pedido = ordenCompra.getPedidoCompra();
                if (pedido != null) {
                    PedidoCompraDAO pedidoDAO = new PedidoCompraDAO(conn);
                    pedido.setEstado("Completado");
                    pedidoDAO.actualizarPedidoCabecera(pedido);
                }

                Presupuesto presupuesto = ordenCompra.getPresupuesto();
                if (presupuesto != null) {
                    PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
                    presupuesto.setEstado("Completado");
                    presupuestoDAO.actualizarPresupuestoCabecera(presupuesto);
                }
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Factura guardada completa. ID: {0}", idInsertado);
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error en guardarFacturaCompleta - rollback ejecutado", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return idInsertado;
    }

    /**
     * Actualiza cabecera, detalles y cuenta a pagar de una factura en una sola transacción.
     *
     * @param facturaCompra la factura a actualizar
     * @param listaDetalle los detalles actualizados
     * @param cuentaPagar cuenta a pagar reconstruida con los nuevos montos/fecha (puede ser null si no aplica)
     * @throws SQLException si ocurre un error (ya se hizo rollback)
     */
    public void actualizarFacturaCompleta(FacturaCompra facturaCompra,
            List<FacturaCompraDetalle> listaDetalle,
            CuentaPagar cuentaPagar) throws SQLException {

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            facturaCompraDAO.actualizarFacturaCompra(facturaCompra);

            FacturaCompraDetalleDAO detalleDAO = new FacturaCompraDetalleDAO(conn);
            detalleDAO.actualizarFacturaCompraDetalles(
                facturaCompra.getIdFacturaCompra(), listaDetalle);

            // Los precios editados también actualizan el catálogo, igual que al guardar.
            actualizarPrecioCompraCatalogo(conn, listaDetalle);

            if (cuentaPagar != null && cuentaPagar.getIdCuentaPagar() != null) {
                CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
                cuentaPagar.setFacturaCompra(new FacturaCompra(facturaCompra.getIdFacturaCompra()));
                cuentaPagarDAO.actualizarCuentaPagar(cuentaPagar);
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Factura actualizada completa. ID: {0}", facturaCompra.getIdFacturaCompra());
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error en actualizarFacturaCompleta - rollback ejecutado", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Deja en cada artículo el precio al que se lo acaba de comprar.
     *
     * <p>Corre sobre la conexión de la transacción de la factura: si la factura falla y se hace
     * rollback, los precios del catálogo tampoco quedan tocados.
     *
     * <p>Sirve para que la carga de un presupuesto muestre el último precio real del artículo sin
     * tener que consultar el historial de facturas. Las líneas de gasto/fondo fijo no tienen
     * artículo y el DAO las ignora; si el mismo artículo aparece en dos líneas de la misma
     * factura, queda el precio de la última.
     */
    private void actualizarPrecioCompraCatalogo(Connection conn,
            List<FacturaCompraDetalle> listaDetalle) throws SQLException {

        if (listaDetalle == null) {
            return;
        }
        ArticuloDAO articuloDAO = new ArticuloDAO(conn);
        for (FacturaCompraDetalle detalle : listaDetalle) {
            if (detalle.getArticulo() != null) {
                articuloDAO.actualizarPrecioCompra(
                        detalle.getArticulo().getIdArticulo(), detalle.getPrecioCompra());
            }
        }
    }

    /**
     * Anula una factura y revierte documentos relacionados en una sola transacción.
     * 1. Cambia estado de la factura a "Anulado"
     * 2. Elimina el registro del libro IVA compra
     * 3. Elimina la cuenta a pagar asociada
     * 4. Revierte estados de documentos relacionados (Orden, Pedido, Presupuesto) a "Pendiente"
     *
     * @param facturaCompra la factura a anular
     * @param ordenCompra la orden de compra relacionada (puede ser null)
     * @throws SQLException si ocurre un error (ya se hizo rollback)
     */
    public void anularFacturaCompleta(FacturaCompra facturaCompra,
            OrdenCompra ordenCompra) throws SQLException {

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            // 1. Anular factura
            FacturaCompraDAO facturaCompraDAO = new FacturaCompraDAO(conn);
            facturaCompra.setEstado("Anulado");
            facturaCompraDAO.actualizarFacturaCompra(facturaCompra);

            // 2. Anular registro del libro IVA compra (UPDATE, no DELETE,
            //    para preservar trazabilidad fiscal/SET)
            LibroIvaCompraDAO libroIvaDAO = new LibroIvaCompraDAO(conn);
            libroIvaDAO.anularPorFactura(facturaCompra.getIdFacturaCompra());

            // 3. Anular cuenta a pagar asociada (UPDATE, no DELETE, para preservar trazabilidad)
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            cuentaPagarDAO.anularPorFactura(facturaCompra.getIdFacturaCompra());

            // 4. Revertir documentos relacionados a "Pendiente"
            if (ordenCompra != null) {
                OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO(conn);
                ordenCompra.setEstado("Pendiente");
                ordenCompraDAO.actualizarOrdenCompra(ordenCompra);

                PedidoCompra pedido = ordenCompra.getPedidoCompra();
                if (pedido != null) {
                    PedidoCompraDAO pedidoDAO = new PedidoCompraDAO(conn);
                    pedido.setEstado("Pendiente");
                    pedidoDAO.actualizarPedidoCabecera(pedido);
                }

                Presupuesto presupuesto = ordenCompra.getPresupuesto();
                if (presupuesto != null) {
                    PresupuestoDAO presupuestoDAO = new PresupuestoDAO(conn);
                    presupuesto.setEstado("Pendiente");
                    presupuestoDAO.actualizarPresupuestoCabecera(presupuesto);
                }
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Factura anulada completa. ID: {0}", facturaCompra.getIdFacturaCompra());
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error en anularFacturaCompleta - rollback ejecutado", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
