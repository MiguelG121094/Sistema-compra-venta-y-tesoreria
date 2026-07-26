package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO de cheque (emisión real desde una chequera). Corre sobre la Connection compartida;
 * la transacción la controla el Service. El número ya debe venir asignado (del rango de la
 * chequera, vía ChequeraDAO.proximoNumeroCheque). Ver MODULO_TESORERIA_PLAN.md §C.
 *
 * @author Miguel
 */
public class ChequeDAO {

    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(ChequeDAO.class.getName());

    public ChequeDAO(Connection conn) {
        this.conn = conn;
    }

    public Long insertarCheque(Cheque cheque) throws SQLException {
        if (cheque == null) {
            throw new SQLException("insertarCheque: el cheque es nulo");
        }
        String sql = "INSERT INTO cheque (chq_numero, chq_fecha_emision, chq_estado, id_chequera, "
                   + "chq_a_la_orden, chq_observacion, id_tipo_cheque, chq_fecha_pago, chq_fecha_venci, id_usuario) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cheque.getNumero());
            stmt.setDate(2, new java.sql.Date(cheque.getFechaEmision().getTime()));
            stmt.setString(3, cheque.getEstado());
            stmt.setLong(4, cheque.getChequera().getIdChequera());
            stmt.setString(5, cheque.getaLaOrden());
            stmt.setString(6, cheque.getObservacion());
            stmt.setLong(7, cheque.getTipoCheque().getIdTipoCheque());
            stmt.setDate(8, new java.sql.Date(cheque.getFechaPago().getTime()));
            stmt.setDate(9, new java.sql.Date(cheque.getFechaVencimiento().getTime()));
            stmt.setLong(10, cheque.getUsuario().getIdUsuario());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó el cheque, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    cheque.setIdCheque(id);
                    LOGGER.log(Level.INFO, "Cheque emitido: id={0}, numero={1}",
                        new Object[]{id, cheque.getNumero()});
                    return id;
                }
            }
            throw new SQLException("No se generó id de cheque");
        }
    }

    /**
     * Cheque por id, con la chequera y el tipo hidratados — para ver una OP ya generada
     * (el detalle de formas de pago solo guarda el id_cheque).
     */
    public Cheque getCheque(Long idCheque) throws SQLException {
        if (idCheque == null) {
            return null;
        }
        String sql = "SELECT c.id_cheque, c.chq_numero, c.chq_fecha_emision, c.chq_estado, c.id_chequera, "
                   + "c.chq_a_la_orden, c.chq_observacion, c.id_tipo_cheque, tc.tipo_cheque_descripcion, "
                   + "c.chq_fecha_pago, c.chq_fecha_venci, c.id_usuario "
                   + "FROM cheque c "
                   + "JOIN tipo_cheque tc ON c.id_tipo_cheque = tc.id_tipo_cheque "
                   + "WHERE c.id_cheque = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCheque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cheque cheque = new Cheque(rs.getLong("id_cheque"));
                    cheque.setNumero(rs.getLong("chq_numero"));
                    cheque.setFechaEmision(rs.getDate("chq_fecha_emision"));
                    cheque.setEstado(rs.getString("chq_estado"));
                    cheque.setChequera(new ChequeraDAO(conn).getChequera(rs.getLong("id_chequera")));
                    cheque.setaLaOrden(rs.getString("chq_a_la_orden"));
                    cheque.setObservacion(rs.getString("chq_observacion"));
                    cheque.setTipoCheque(new TipoCheque(rs.getLong("id_tipo_cheque"),
                            rs.getString("tipo_cheque_descripcion")));
                    cheque.setFechaPago(rs.getDate("chq_fecha_pago"));
                    cheque.setFechaVencimiento(rs.getDate("chq_fecha_venci"));
                    cheque.setUsuario(new Usuario(rs.getLong("id_usuario")));
                    return cheque;
                }
            }
        }
        return null;
    }

    /**
     * Anula un cheque emitido (estado 'Anulado'). Se usa al anular la Orden de Pago que lo emitió.
     * Corre sobre la Connection compartida; la transacción la controla el Service.
     */
    public void anularCheque(Long idCheque) throws SQLException {
        if (idCheque == null) {
            return;
        }
        String sql = "UPDATE cheque SET chq_estado = 'Anulado' WHERE id_cheque = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCheque);
            stmt.executeUpdate();
        }
    }
}
