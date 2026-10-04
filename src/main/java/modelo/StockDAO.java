/*
 * DAO de stock: el punto de reposicion (minima/maxima) por articulo y deposito.
 * Corre sobre la Connection compartida; la transaccion la controla el Service.
 */
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    private final Connection conn;

    public StockDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Articulos activos del catalogo con lo que el deposito tiene de cada uno.
     *
     * <p>El LEFT JOIN es la clave: la fila de stock la crea el trigger en la primera compra de ese
     * articulo para ese deposito, asi que un articulo nuevo todavia no la tiene. Igual se lista, en
     * cero, para poder fijarle la minima y la maxima antes de la primera compra, que es cuando
     * sirven. Los nulos se resuelven en Java, no con COALESCE en la consulta.
     */
    public List<Stock> listarPorDeposito(Long idDeposito) throws SQLException {
        List<Stock> filas = new ArrayList<>();
        if (idDeposito == null) {
            return filas;
        }
        String sql = "SELECT a.id_articulo, a.art_descripcion, a.art_codigo, "
                   + "s.stk_cantidad_minima, s.stk_cantidad_maxima, s.stk_stock_actual "
                   + "FROM articulo a "
                   + "LEFT JOIN stock s ON s.id_articulo = a.id_articulo AND s.id_deposito = ? "
                   + "WHERE a.art_estado = ? "
                   + "ORDER BY a.art_descripcion";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idDeposito);
            stmt.setString(2, ArticuloDAO.ESTADO_ACTIVO);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Articulo articulo = new Articulo(rs.getLong("id_articulo"));
                    articulo.setDescripcion(rs.getString("art_descripcion"));
                    articulo.setCodigo(rs.getString("art_codigo"));

                    Stock stock = new Stock(new Deposito(idDeposito), articulo);
                    stock.setCantidadMinima(leerLong(rs, "stk_cantidad_minima"));
                    stock.setCantidadMaxima(leerLong(rs, "stk_cantidad_maxima"));
                    stock.setStockActual(leerLong(rs, "stk_stock_actual"));
                    filas.add(stock);
                }
            }
        }
        return filas;
    }

    /**
     * Graba la minima y la maxima de un articulo en un deposito.
     *
     * <p>Si la fila todavia no existe se crea con <b>stock actual 0</b>: fijar un punto de
     * reposicion no mueve existencias. El UPDATE del conflicto <b>no toca</b> stk_stock_actual, que
     * es del trigger de compras y del ajuste de stock; esta pantalla solo escribe los dos limites.
     */
    public void guardarLimites(Long idDeposito, Long idArticulo, Long minima, Long maxima)
            throws SQLException {

        if (idDeposito == null || idArticulo == null) {
            return;
        }
        String sql = "INSERT INTO stock (id_deposito, id_articulo, stk_cantidad_minima, "
                   + "stk_cantidad_maxima, stk_stock_actual) VALUES (?, ?, ?, ?, 0) "
                   + "ON CONFLICT (id_deposito, id_articulo) DO UPDATE "
                   + "SET stk_cantidad_minima = EXCLUDED.stk_cantidad_minima, "
                   + "stk_cantidad_maxima = EXCLUDED.stk_cantidad_maxima";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idDeposito);
            stmt.setLong(2, idArticulo);
            stmt.setLong(3, minima == null ? 0L : minima);
            stmt.setLong(4, maxima == null ? 0L : maxima);
            stmt.executeUpdate();
        }
    }

    private Long leerLong(ResultSet rs, String columna) throws SQLException {
        long valor = rs.getLong(columna);
        return rs.wasNull() ? null : valor;
    }
}
