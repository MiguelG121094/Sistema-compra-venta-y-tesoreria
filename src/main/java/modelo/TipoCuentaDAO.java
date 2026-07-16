package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Miguel
 */
public class TipoCuentaDAO {

    private Connection conn;

    public TipoCuentaDAO(Connection conn) {
        this.conn = conn;
    }

    public List<TipoCuenta> listarTipoCuenta() throws SQLException {
        List<TipoCuenta> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_cuenta, tipo_cuenta_descripcion FROM public.tipo_cuenta ORDER BY id_tipo_cuenta";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new TipoCuenta(rs.getLong("id_tipo_cuenta"), rs.getString("tipo_cuenta_descripcion")));
            }
        }
        return lista;
    }

    public TipoCuenta getTipoCuenta(Long idTipoCuenta) throws SQLException {
        if (idTipoCuenta == null) {
            return null;
        }
        String sql = "SELECT id_tipo_cuenta, tipo_cuenta_descripcion FROM public.tipo_cuenta WHERE id_tipo_cuenta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idTipoCuenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new TipoCuenta(rs.getLong("id_tipo_cuenta"), rs.getString("tipo_cuenta_descripcion"));
                }
            }
        }
        return null;
    }
}
