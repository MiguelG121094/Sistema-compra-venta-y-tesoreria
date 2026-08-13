/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import modelo.CuentaPagarDAO;
import modelo.LibroIvaCompra;
import modelo.LibroIvaCompraDAO;
import modelo.NotaCreditoCompra;
import modelo.NotaCreditoCompraDAO;
import modelo.NotaCreditoCompraDetalle;

/**
 *
 * @author Miguel
 */
public class NotaCreditoCompraService {

    public NotaCreditoCompra getNotaCreditoCompra(Long idNotaCreditoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.getNotaCreditoCompra(idNotaCreditoCompra);
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoCompraService: " + e);
            return null;
        }
    }

    public List<NotaCreditoCompra> listarNotasCreditoCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.listarNotasCreditoCompra();
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoCompraService: " + e);
            return null;
        }
    }

    public Long insertarNotaCreditoCompra(NotaCreditoCompra notaCredito) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            idInserted = dao.insertarNotaCreditoCompra(notaCredito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaCreditoCompra(NotaCreditoCompra notaCredito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            dao.actualizarNotaCreditoCompra(notaCredito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaCreditoCompra(Long idNotaCreditoCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            dao.eliminarNotaCreditoCompra(idNotaCreditoCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaCreditoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public List<NotaCreditoCompraDetalle> listarDetallesPorNota(Long idNotaCredito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.listarDetallesPorNota(idNotaCredito);
        } catch (SQLException e) {
            System.out.println("Error en NotaCreditoCompraService: " + e);
            return null;
        }
    }

    /** Indica si la factura tiene una nota de credito NO anulada (propaga la excepcion). */
    public boolean tieneNotaActivaPorFactura(Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.tieneNotaActivaPorFactura(idFacturaCompra);
        }
    }

    /**
     * Cantidad ya devuelta por articulo en las notas de credito activas de esa factura.
     *
     * <p>Propaga la excepcion, igual que el guard de arriba: es el insumo de una validacion, y
     * devolver un mapa vacio ante un error de BD dejaria pasar una devolucion excedida.
     */
    public Map<Long, Long> obtenerCantidadesDevueltasPorArticulo(Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            return dao.obtenerCantidadesDevueltasPorArticulo(idFacturaCompra);
        }
    }

    // ==================== MÉTODOS TRANSACCIONALES ====================

    /**
     * Guarda una nota de crédito completa en una sola transacción:
     *   1. Inserta la cabecera de la nota.
     *   2. Inserta los detalles (dispara el trigger de stock en las líneas con depósito).
     *   3. Registra la fila de libro IVA (origen NOTA_CRED, montos NEGATIVOS).
     *   4. Ajusta el saldo de la cuenta a pagar de la factura referenciada (resta; admite negativo).
     * Si algo falla, se hace rollback de todo.
     * Ver NOTA_CREDITO_DEBITO_PLAN.md §4/§5/§8.
     *
     * @param nota     cabecera de la nota (con factura referenciada seteada)
     * @param detalles líneas de la nota
     * @param libroIva montos POSITIVOS (se niegan aquí), fecha y estado del libro IVA
     * @return id de la nota insertada
     * @throws SQLException si algo falla (ya se hizo rollback)
     */
    public Long guardarNotaCreditoCompleta(NotaCreditoCompra nota,
            List<NotaCreditoCompraDetalle> detalles,
            LibroIvaCompra libroIva) throws SQLException {

        Connection conn = null;
        Long idNota = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            // 1. Cabecera
            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            idNota = dao.insertarNotaCreditoCompra(nota);

            // 2. Detalles (el trigger de stock resta las líneas con depósito)
            for (NotaCreditoCompraDetalle det : detalles) {
                dao.insertarDetalle(det, idNota);
            }

            Long idFactura = nota.getFacturaCompra().getIdFacturaCompra();
            long total = libroIva.getTotal() != null ? libroIva.getTotal() : 0L;

            // 3. Libro IVA con montos NEGATIVOS (la NC resta del período)
            negarMontos(libroIva);
            LibroIvaCompraDAO libroIvaDAO = new LibroIvaCompraDAO(conn);
            libroIvaDAO.insertarLibroIvaNota(libroIva, idFactura, "NOTA_CRED", idNota, null);

            // 4. Ajustar saldo de la cuenta a pagar (resta; puede quedar negativo = saldo a favor)
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            cuentaPagarDAO.ajustarSaldoPorNota(idFactura, total, CuentaPagarDAO.TipoNota.CREDITO, false);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en guardarNotaCreditoCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return idNota;
    }

    /**
     * Anula una nota de crédito en una sola transacción:
     *   1. Estado de la nota a 'Anulado' (dispara el trigger de reposición de stock).
     *   2. Filas de libro IVA de la nota a 'Anulado' (preserva trazabilidad).
     *   3. Reversa del ajuste de saldo de la cuenta a pagar (suma de vuelta el monto).
     * El guard de "crédito ya neteado en una provisión" se valida en el servlet antes de llamar.
     *
     * @param nota       nota a anular (con factura referenciada seteada)
     * @param montoTotal total (positivo) de la nota, para revertir el saldo
     * @throws SQLException si algo falla (ya se hizo rollback)
     */
    public void anularNotaCreditoCompleta(NotaCreditoCompra nota, long montoTotal) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            Long idNota = nota.getIdNotaCreditoCompra();
            Long idFactura = nota.getFacturaCompra().getIdFacturaCompra();

            NotaCreditoCompraDAO dao = new NotaCreditoCompraDAO(conn);
            dao.anularNotaCredito(idNota);

            LibroIvaCompraDAO libroIvaDAO = new LibroIvaCompraDAO(conn);
            libroIvaDAO.anularPorNotaCredito(idNota);

            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            cuentaPagarDAO.ajustarSaldoPorNota(idFactura, montoTotal, CuentaPagarDAO.TipoNota.CREDITO, true);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en anularNotaCreditoCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /** Invierte el signo de los montos del libro IVA (una NC resta del período). */
    private void negarMontos(LibroIvaCompra libroIva) {
        libroIva.setIva5(neg(libroIva.getIva5()));
        libroIva.setIva10(neg(libroIva.getIva10()));
        libroIva.setGravada5(neg(libroIva.getGravada5()));
        libroIva.setGravada10(neg(libroIva.getGravada10()));
        libroIva.setExenta(neg(libroIva.getExenta()));
        libroIva.setTotal(neg(libroIva.getTotal()));
    }

    private Long neg(Long v) {
        return v == null ? null : -v;
    }
}
