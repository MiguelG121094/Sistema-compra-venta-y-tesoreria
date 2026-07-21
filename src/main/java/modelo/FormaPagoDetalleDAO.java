package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO del detalle de formas de pago de una orden de pago. Una OP se puede pagar de varias
 * formas (transferencia + cheque(s)), por eso son N filas. El tipo lo da id_forma_pago_cab;
 * id_cheque es nullable (NULL en transferencia, apunta al cheque real en las líneas de cheque).
 * Corre sobre la Connection compartida; la transacción la controla el Service.
 *
 * @author Miguel
 */
public class FormaPagoDetalleDAO {

    private Connection conn;

    public FormaPagoDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertarFormaPago(FormaPagoDetalle fp, Long idOrdenPago) throws SQLException {
        String sql = "INSERT INTO forma_pago_detalle "
                   + "(id_forma_pago_cab, id_orden_pago, forma_pag_monto, forma_pag_estado, "
                   + "forma_pag_referencia, id_cuenta, forma_pag_fecha, id_cheque) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, fp.getFormaPagoCabecera().getIdFormaPagoCabecera());
            stmt.setLong(2, idOrdenPago);
            stmt.setLong(3, fp.getMonto());
            if (fp.getEstado() != null) {
                stmt.setString(4, fp.getEstado());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            if (fp.getReferencia() != null) {
                stmt.setString(5, fp.getReferencia());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.setLong(6, fp.getCuenta().getIdCuenta());
            if (fp.getFecha() != null) {
                stmt.setDate(7, new java.sql.Date(fp.getFecha().getTime()));
            } else {
                stmt.setNull(7, Types.DATE);
            }
            if (fp.getCheque() != null && fp.getCheque().getIdCheque() != null) {
                stmt.setLong(8, fp.getCheque().getIdCheque());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Formas de pago de una OP (con el tipo y la cuenta hidratados) — para ver la OP.
     */
    public List<FormaPagoDetalle> listarPorOrden(Long idOrdenPago) throws SQLException {
        List<FormaPagoDetalle> lista = new ArrayList<>();
        String sql = "SELECT fp.id_forma_pago_det, fp.id_forma_pago_cab, fc.forma_pago_descripcion, "
                   + "fp.forma_pag_monto, fp.forma_pag_estado, fp.forma_pag_referencia, "
                   + "fp.id_cuenta, fp.forma_pag_fecha, fp.id_cheque "
                   + "FROM forma_pago_detalle fp "
                   + "JOIN forma_pago_cabecera fc ON fp.id_forma_pago_cab = fc.id_forma_pago_cab "
                   + "WHERE fp.id_orden_pago = ? ORDER BY fp.id_forma_pago_det";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenPago);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FormaPagoDetalle fp = new FormaPagoDetalle();
                    fp.setIdFormaPagoDetalle(rs.getLong("id_forma_pago_det"));
                    fp.setFormaPagoCabecera(new FormaPagoCabecera(
                        rs.getLong("id_forma_pago_cab"), rs.getString("forma_pago_descripcion")));
                    fp.setMonto(rs.getLong("forma_pag_monto"));
                    fp.setEstado(rs.getString("forma_pag_estado"));
                    fp.setReferencia(rs.getString("forma_pag_referencia"));
                    fp.setCuenta(new Cuenta(rs.getLong("id_cuenta")));
                    fp.setFecha(rs.getDate("forma_pag_fecha"));
                    long idCheque = rs.getLong("id_cheque");
                    if (!rs.wasNull()) {
                        fp.setCheque(new Cheque(idCheque));
                    }
                    lista.add(fp);
                }
            }
        }
        return lista;
    }
}
