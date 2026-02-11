package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
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
}
