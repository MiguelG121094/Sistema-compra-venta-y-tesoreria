package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de debitos bancarios: comisiones, gastos administrativos del banco y debitos automaticos.
 * Son movimientos que no vienen de una orden de pago y alimentan la conciliacion (§D y §F del
 * MODULO_TESORERIA_PLAN.md). No mueven ningun saldo: la cuenta bancaria no lo tiene, se arma en
 * la conciliacion. Corre sobre la Connection compartida (la transaccion la controla el Service).
 *
 * @author Miguel
 */
public class DebitoDAO {

    public static final String ESTADO_VIGENTE = "Vigente";
    public static final String ESTADO_ANULADO = "Anulado";

    private static final String COLUMNAS =
            "id_debitos, debitos_nro_comprobante, debitos_fecha, debitos_detalle, id_cuenta, "
            + "debito_monto, debitos_tipo_cambio, debitos_estado";

    private Connection conn;

    public DebitoDAO(Connection conn) {
        this.conn = conn;
    }

    private Debito mapear(ResultSet rs) throws SQLException {
        Debito debito = new Debito();
        debito.setIdDebito(rs.getLong("id_debitos"));
        debito.setNumeroComprobante(rs.getLong("debitos_nro_comprobante"));
        debito.setFecha(rs.getDate("debitos_fecha"));
        debito.setDetalle(rs.getString("debitos_detalle"));
        debito.setCuenta(new CuentaDAO(conn).getCuenta(rs.getLong("id_cuenta")));
        debito.setMonto(rs.getLong("debito_monto"));
        double tipoCambio = rs.getDouble("debitos_tipo_cambio");
        debito.setTipoCambio(rs.wasNull() ? null : tipoCambio);
        debito.setEstado(rs.getString("debitos_estado"));
        return debito;
    }

    public Debito getDebito(Long idDebito) throws SQLException {
        if (idDebito == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM debitos WHERE id_debitos = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idDebito);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Listado para el modal de busqueda: los mas nuevos primero, anulados incluidos. */
    public List<Debito> listarDebitos() throws SQLException {
        List<Debito> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM debitos ORDER BY id_debitos DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Long insertarDebito(Debito debito) throws SQLException {
        String sql = "INSERT INTO debitos (debitos_nro_comprobante, debitos_fecha, debitos_detalle, "
                   + "id_cuenta, debito_monto, debitos_tipo_cambio, debitos_estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, debito.getNumeroComprobante());
            stmt.setDate(2, new java.sql.Date(debito.getFecha().getTime()));
            stmt.setString(3, debito.getDetalle());
            stmt.setLong(4, debito.getCuenta().getIdCuenta());
            stmt.setLong(5, debito.getMonto());
            if (debito.getTipoCambio() == null) {
                stmt.setNull(6, Types.DOUBLE);
            } else {
                stmt.setDouble(6, debito.getTipoCambio());
            }
            stmt.setString(7, ESTADO_VIGENTE);

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó el débito, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    debito.setIdDebito(id);
                    debito.setEstado(ESTADO_VIGENTE);
                    return id;
                }
            }
            throw new SQLException("No se generó id de débito");
        }
    }

    /**
     * Marca el debito como anulado en vez de borrarlo: la conciliacion bancaria referencia
     * id_debitos, asi que la fila tiene que seguir existiendo. Devuelve false si ya estaba anulado.
     */
    public boolean anularDebito(Long idDebito) throws SQLException {
        String sql = "UPDATE debitos SET debitos_estado = ? WHERE id_debitos = ? AND debitos_estado <> ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ESTADO_ANULADO);
            stmt.setLong(2, idDebito);
            stmt.setString(3, ESTADO_ANULADO);
            return stmt.executeUpdate() > 0;
        }
    }
}
