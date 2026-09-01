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
 * DAO de creditos bancarios: depositos, transferencias recibidas, comisiones cobradas y
 * capitalizacion de intereses. La boleta de deposito es una fila de esta tabla, por eso el mismo
 * ABM cierra los requerimientos 3.8 y 3.9 (§D del MODULO_TESORERIA_PLAN.md). No mueven ningun
 * saldo: la cuenta bancaria no lo tiene, se arma en la conciliacion. Corre sobre la Connection
 * compartida (la transaccion la controla el Service).
 *
 * @author Miguel
 */
public class CreditoDAO {

    public static final String ESTADO_VIGENTE = "Vigente";
    public static final String ESTADO_ANULADO = "Anulado";

    private static final String COLUMNAS =
            "id_creditos, creditos_nro_comprobante, creditos_fecha, creditos_detalle, id_cuenta, "
            + "id_cobro, credito_monto, creditos_tipo_cambio, creditos_estado";

    private Connection conn;

    public CreditoDAO(Connection conn) {
        this.conn = conn;
    }

    private Credito mapear(ResultSet rs) throws SQLException {
        Credito credito = new Credito();
        credito.setIdCredito(rs.getLong("id_creditos"));
        credito.setNumeroComprobante(rs.getLong("creditos_nro_comprobante"));
        credito.setFecha(rs.getDate("creditos_fecha"));
        credito.setDetalle(rs.getString("creditos_detalle"));
        credito.setCuenta(new CuentaDAO(conn).getCuenta(rs.getLong("id_cuenta")));
        // El cobro llega recien con Ventas: hasta entonces id_cobro viaja nulo.
        long idCobro = rs.getLong("id_cobro");
        credito.setCobro(rs.wasNull() ? null : new Cobro(idCobro));
        credito.setMonto(rs.getLong("credito_monto"));
        double tipoCambio = rs.getDouble("creditos_tipo_cambio");
        credito.setTipoCambio(rs.wasNull() ? null : tipoCambio);
        credito.setEstado(rs.getString("creditos_estado"));
        return credito;
    }

    public Credito getCredito(Long idCredito) throws SQLException {
        if (idCredito == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM creditos WHERE id_creditos = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCredito);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Listado para el modal de busqueda: los mas nuevos primero, anulados incluidos. */
    public List<Credito> listarCreditos() throws SQLException {
        List<Credito> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM creditos ORDER BY id_creditos DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Numero que le va a tocar al proximo credito. Es el id, que es serial, asi que el valor
     * definitivo se confirma recien al insertar: si dos usuarios abren uno a la vez ven el mismo,
     * igual que pasa con el numero de la orden de pago.
     */
    public long proximoNumero() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id_creditos), 0) + 1 FROM creditos";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public Long insertarCredito(Credito credito) throws SQLException {
        String sql = "INSERT INTO creditos (creditos_nro_comprobante, creditos_fecha, creditos_detalle, "
                   + "id_cuenta, id_cobro, credito_monto, creditos_tipo_cambio, creditos_estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, credito.getNumeroComprobante());
            stmt.setDate(2, new java.sql.Date(credito.getFecha().getTime()));
            stmt.setString(3, credito.getDetalle());
            stmt.setLong(4, credito.getCuenta().getIdCuenta());
            if (credito.getCobro() == null || credito.getCobro().getIdCobro() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setLong(5, credito.getCobro().getIdCobro());
            }
            stmt.setLong(6, credito.getMonto());
            if (credito.getTipoCambio() == null) {
                stmt.setNull(7, Types.DOUBLE);
            } else {
                stmt.setDouble(7, credito.getTipoCambio());
            }
            stmt.setString(8, ESTADO_VIGENTE);

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó el crédito, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    credito.setIdCredito(id);
                    credito.setEstado(ESTADO_VIGENTE);
                    return id;
                }
            }
            throw new SQLException("No se generó id de crédito");
        }
    }

    /**
     * Marca el credito como anulado en vez de borrarlo: la conciliacion bancaria referencia
     * id_creditos, asi que la fila tiene que seguir existiendo. Devuelve false si ya estaba anulado.
     */
    public boolean anularCredito(Long idCredito) throws SQLException {
        String sql = "UPDATE creditos SET creditos_estado = ? WHERE id_creditos = ? AND creditos_estado <> ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ESTADO_ANULADO);
            stmt.setLong(2, idCredito);
            stmt.setString(3, ESTADO_ANULADO);
            return stmt.executeUpdate() > 0;
        }
    }
}
