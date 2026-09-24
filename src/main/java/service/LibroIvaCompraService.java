package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import modelo.LibroIvaCompra;
import modelo.LibroIvaCompraDAO;

/**
 * Service para operaciones del libro IVA compra.
 *
 * @author Miguel
 */
public class LibroIvaCompraService {

    public Long insertarLibroIvaCompra(LibroIvaCompra libroIva) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            LibroIvaCompraDAO dao = new LibroIvaCompraDAO(conn);
            idInserted = dao.insertarLibroIvaCompra(libroIva);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en LibroIvaCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void eliminarPorFactura(Long idFacturaCompra) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            LibroIvaCompraDAO dao = new LibroIvaCompraDAO(conn);
            dao.eliminarPorFactura(idFacturaCompra);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en LibroIvaCompraService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // ==================== INFORME LIBRO DE COMPRAS ====================

    /** Filas del libro de compras del periodo, para el informe. Deja salir el error de base. */
    public List<LibroIvaCompra> listarParaInforme(Date desde, Date hasta, String origen)
            throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new LibroIvaCompraDAO(conn).listarParaInforme(desde, hasta, origen);
        }
    }

    /**
     * Fila de totales del informe: la suma de cada columna de importe. Se calcula en Java y no en
     * la consulta, como el resto del sistema. Las notas de credito vienen en negativo, asi que la
     * suma ya sale neta.
     */
    public static LibroIvaCompra calcularTotales(List<LibroIvaCompra> filas) {
        LibroIvaCompra totales = new LibroIvaCompra();
        long gravada10 = 0, iva10 = 0, gravada5 = 0, iva5 = 0, exenta = 0, total = 0;
        if (filas != null) {
            for (LibroIvaCompra fila : filas) {
                gravada10 += valor(fila.getGravada10());
                iva10 += valor(fila.getIva10());
                gravada5 += valor(fila.getGravada5());
                iva5 += valor(fila.getIva5());
                exenta += valor(fila.getExenta());
                total += valor(fila.getTotal());
            }
        }
        totales.setGravada10(gravada10);
        totales.setIva10(iva10);
        totales.setGravada5(gravada5);
        totales.setIva5(iva5);
        totales.setExenta(exenta);
        totales.setTotal(total);
        return totales;
    }

    private static long valor(Long monto) {
        return monto == null ? 0L : monto;
    }
}
