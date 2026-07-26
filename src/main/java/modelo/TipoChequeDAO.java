package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO del catálogo de tipos de cheque (diferido, a la vista, a la orden, al portador, cruzado).
 * Se usa para el combo de la línea de cheque en el carrito de formas de pago de la Orden de Pago.
 *
 * @author Miguel
 */
public class TipoChequeDAO {

    private Connection conn;

    public TipoChequeDAO(Connection conn) {
        this.conn = conn;
    }

    public List<TipoCheque> listarTipoCheque() throws SQLException {
        List<TipoCheque> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_cheque, tipo_cheque_descripcion FROM public.tipo_cheque "
                   + "ORDER BY id_tipo_cheque";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new TipoCheque(rs.getLong("id_tipo_cheque"),
                        rs.getString("tipo_cheque_descripcion")));
            }
        }
        return lista;
    }

    public TipoCheque getTipoCheque(Long idTipoCheque) throws SQLException {
        if (idTipoCheque == null) {
            return null;
        }
        String sql = "SELECT id_tipo_cheque, tipo_cheque_descripcion FROM public.tipo_cheque "
                   + "WHERE id_tipo_cheque = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idTipoCheque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new TipoCheque(rs.getLong("id_tipo_cheque"),
                            rs.getString("tipo_cheque_descripcion"));
                }
            }
        }
        return null;
    }
}
