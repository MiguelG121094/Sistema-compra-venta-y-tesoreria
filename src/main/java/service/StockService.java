/*
 * Service de stock. Dueño de la transaccion (setAutoCommit(false)), como el resto del sistema.
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Stock;
import modelo.StockDAO;

/**
 * Puntos de reposicion por deposito: la minima y la maxima de cada articulo.
 *
 * <p>El stock actual no se toca desde aca: lo mueven el trigger de la factura de compra y, mas
 * adelante, el ajuste de stock.
 *
 * @author Miguel
 */
public class StockService {

    public List<Stock> listarPorDeposito(Long idDeposito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return new StockDAO(conn).listarPorDeposito(idDeposito);
        }
    }

    /**
     * Guarda los limites de todos los articulos del deposito en una sola transaccion: la carga es
     * de muchas filas de una sentada, y a medio grabar el deposito quedaria inconsistente.
     *
     * @return cuantas filas se grabaron
     */
    public int guardarLimites(Long idDeposito, List<Stock> filas) throws SQLException {
        if (idDeposito == null) {
            throw new SQLException("Debe seleccionar el depósito");
        }
        if (filas == null || filas.isEmpty()) {
            return 0;
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            StockDAO dao = new StockDAO(conn);
            int grabadas = 0;
            for (Stock fila : filas) {
                if (fila.getArticulo() == null || fila.getArticulo().getIdArticulo() == null) {
                    continue;
                }
                dao.guardarLimites(idDeposito, fila.getArticulo().getIdArticulo(),
                        fila.getCantidadMinima(), fila.getCantidadMaxima());
                grabadas++;
            }

            conn.commit();
            return grabadas;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en StockService.guardarLimites - rollback ejecutado: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
