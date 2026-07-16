/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.CuentaPagarDAO;
import modelo.LibroIvaCompra;
import modelo.LibroIvaCompraDAO;
import modelo.NotaDebitoCompra;
import modelo.NotaDebitoCompraDAO;
import modelo.NotaDebitoCompraDetalle;

/**
 *
 * @author Miguel
 */
public class NotaDebitoCompraService {

    public NotaDebitoCompra getNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            return dao.getNotaDebitoCompra(idNotaDebitoCompra);
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoCompraService: " + e);
            return null;
        }
    }

    public List<NotaDebitoCompra> listarNotasDebitoCompra() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            return dao.listarNotasDebitoCompra();
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoCompraService: " + e);
            return null;
        }
    }

    public Long insertarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            idInserted = dao.insertarNotaDebitoCompra(notaDebito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            dao.actualizarNotaDebitoCompra(notaDebito);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            dao.eliminarNotaDebitoCompra(idNotaDebitoCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en NotaDebitoCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public List<NotaDebitoCompraDetalle> listarDetallesPorNota(Long idNotaDebito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            return dao.listarDetallesPorNota(idNotaDebito);
        } catch (SQLException e) {
            System.out.println("Error en NotaDebitoCompraService: " + e);
            return null;
        }
    }

    /** Indica si la factura tiene una nota de debito NO anulada (propaga la excepcion). */
    public boolean tieneNotaActivaPorFactura(Long idFacturaCompra) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            return dao.tieneNotaActivaPorFactura(idFacturaCompra);
        }
    }

    // ==================== MÉTODOS TRANSACCIONALES ====================

    /**
     * Guarda una nota de débito completa en una sola transacción:
     *   1. Inserta la cabecera de la nota.
     *   2. Inserta los detalles (la ND NO mueve stock).
     *   3. Registra la fila de libro IVA (origen NOTA_DEBI, montos POSITIVOS).
     *   4. Ajusta el saldo de la cuenta a pagar de la factura referenciada (suma).
     * Si algo falla, se hace rollback de todo.
     * Ver NOTA_CREDITO_DEBITO_PLAN.md §4/§5/§8.
     *
     * @param nota     cabecera de la nota (con factura referenciada seteada)
     * @param detalles líneas de la nota
     * @param libroIva montos POSITIVOS, fecha y estado del libro IVA
     * @return id de la nota insertada
     * @throws SQLException si algo falla (ya se hizo rollback)
     */
    public Long guardarNotaDebitoCompleta(NotaDebitoCompra nota,
            List<NotaDebitoCompraDetalle> detalles,
            LibroIvaCompra libroIva) throws SQLException {

        Connection conn = null;
        Long idNota = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            // 1. Cabecera
            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            idNota = dao.insertarNotaDebitoCompra(nota);

            // 2. Detalles (la ND no toca stock)
            for (NotaDebitoCompraDetalle det : detalles) {
                dao.insertarDetalle(det, idNota);
            }

            Long idFactura = nota.getFacturaCompra().getIdFacturaCompra();
            long total = libroIva.getTotal() != null ? libroIva.getTotal() : 0L;

            // 3. Libro IVA con montos POSITIVOS (la ND suma al período)
            LibroIvaCompraDAO libroIvaDAO = new LibroIvaCompraDAO(conn);
            libroIvaDAO.insertarLibroIvaNota(libroIva, idFactura, "NOTA_DEBI", null, idNota);

            // 4. Ajustar saldo de la cuenta a pagar (suma la deuda adicional)
            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            cuentaPagarDAO.ajustarSaldoPorNota(idFactura, total, CuentaPagarDAO.TipoNota.DEBITO, false);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en guardarNotaDebitoCompleta - rollback ejecutado: " + e);
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
     * Anula una nota de débito en una sola transacción:
     *   1. Estado de la nota a 'Anulado'.
     *   2. Filas de libro IVA de la nota a 'Anulado' (preserva trazabilidad).
     *   3. Reversa del ajuste de saldo de la cuenta a pagar (resta lo sumado).
     *
     * @param nota       nota a anular (con factura referenciada seteada)
     * @param montoTotal total (positivo) de la nota, para revertir el saldo
     * @throws SQLException si algo falla (ya se hizo rollback)
     */
    public void anularNotaDebitoCompleta(NotaDebitoCompra nota, long montoTotal) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            Long idNota = nota.getIdNotaDebitoCompra();
            Long idFactura = nota.getFacturaCompra().getIdFacturaCompra();

            NotaDebitoCompraDAO dao = new NotaDebitoCompraDAO(conn);
            dao.anularNotaDebito(idNota);

            LibroIvaCompraDAO libroIvaDAO = new LibroIvaCompraDAO(conn);
            libroIvaDAO.anularPorNotaDebito(idNota);

            CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
            cuentaPagarDAO.ajustarSaldoPorNota(idFactura, montoTotal, CuentaPagarDAO.TipoNota.DEBITO, true);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en anularNotaDebitoCompleta - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
