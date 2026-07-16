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
public class EntidadFinancieraDAO {

    private Connection conn;

    public EntidadFinancieraDAO(Connection conn) {
        this.conn = conn;
    }

    public List<EntidadFinanciera> listarEntidadFinanciera() throws SQLException {
        List<EntidadFinanciera> lista = new ArrayList<>();
        String sql = "SELECT id_enti_finan, enti_finan_nombre FROM public.entidad_financiera ORDER BY enti_finan_nombre";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new EntidadFinanciera(rs.getLong("id_enti_finan"), rs.getString("enti_finan_nombre"), null));
            }
        }
        return lista;
    }

    public EntidadFinanciera getEntidadFinanciera(Long idEntidadFinanciera) throws SQLException {
        if (idEntidadFinanciera == null) {
            return null;
        }
        String sql = "SELECT id_enti_finan, enti_finan_nombre FROM public.entidad_financiera WHERE id_enti_finan = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEntidadFinanciera);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EntidadFinanciera(rs.getLong("id_enti_finan"), rs.getString("enti_finan_nombre"), null);
                }
            }
        }
        return null;
    }
}
