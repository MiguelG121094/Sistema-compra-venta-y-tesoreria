package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermisoDAO {

    private Connection conn;

    public PermisoDAO(Connection conn) {
        this.conn = conn;
    }

    public Permiso getPermiso(Long idGrupo, Long idModulo) throws SQLException {
        if (idGrupo == null || idModulo == null) {
            System.out.println("Error: parametro idGrupo o idModulo es nulo");
            return null;
        }
        String sql = "SELECT p.id_grupo, p.id_modulo, p.permi_leer, p.permi_insertar, p.permi_borrar, p.permi_editar, "
                   + "m.modu_descripcion FROM permiso p "
                   + "JOIN modulo m ON p.id_modulo = m.id_modulo "
                   + "WHERE p.id_grupo = ? AND p.id_modulo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idGrupo);
            stmt.setLong(2, idModulo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPermiso(rs);
                }
            }
        }
        return null;
    }

    public List<Permiso> listarPermisosByGrupo(Long idGrupo) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        if (idGrupo == null) {
            System.out.println("Error: parametro idGrupo es nulo");
            return permisos;
        }
        String sql = "SELECT p.id_grupo, p.id_modulo, p.permi_leer, p.permi_insertar, p.permi_borrar, p.permi_editar, "
                   + "m.modu_descripcion FROM permiso p "
                   + "JOIN modulo m ON p.id_modulo = m.id_modulo "
                   + "WHERE p.id_grupo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idGrupo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapearPermiso(rs));
                }
            }
        }
        return permisos;
    }

    private Permiso mapearPermiso(ResultSet rs) throws SQLException {
        Grupo grupo = new Grupo(rs.getLong("id_grupo"));
        Modulo modulo = new Modulo(rs.getLong("id_modulo"), rs.getString("modu_descripcion"));
        Permiso permiso = new Permiso(grupo, modulo,
                rs.getBoolean("permi_leer"),
                rs.getBoolean("permi_insertar"),
                rs.getBoolean("permi_borrar"),
                rs.getBoolean("permi_editar"));
        return permiso;
    }
}
