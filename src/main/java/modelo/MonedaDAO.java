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
public class MonedaDAO {

    private Connection conn;

    public MonedaDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Moneda> listarMoneda() throws SQLException {
        List<Moneda> lista = new ArrayList<>();
        String sql = "SELECT id_moneda, moneda_descipcion FROM public.moneda ORDER BY id_moneda";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Moneda(rs.getLong("id_moneda"), rs.getString("moneda_descipcion")));
            }
        }
        return lista;
    }

    public Moneda getMoneda(Long idMoneda) throws SQLException {
        if (idMoneda == null) {
            return null;
        }
        String sql = "SELECT id_moneda, moneda_descipcion FROM public.moneda WHERE id_moneda = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idMoneda);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Moneda(rs.getLong("id_moneda"), rs.getString("moneda_descipcion"));
                }
            }
        }
        return null;
    }
}
