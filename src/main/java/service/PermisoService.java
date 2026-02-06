package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Permiso;
import modelo.PermisoDAO;

public class PermisoService {

    public Permiso getPermiso(Long idGrupo, Long idModulo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PermisoDAO permisoDAO = new PermisoDAO(conn);
            return permisoDAO.getPermiso(idGrupo, idModulo);
        } catch (SQLException e) {
            System.out.println("Error en PermisoService.getPermiso: " + e);
            return null;
        }
    }

    public List<Permiso> listarPermisosByGrupo(Long idGrupo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PermisoDAO permisoDAO = new PermisoDAO(conn);
            return permisoDAO.listarPermisosByGrupo(idGrupo);
        } catch (SQLException e) {
            System.out.println("Error en PermisoService.listarPermisosByGrupo: " + e);
            return new ArrayList<>();
        }
    }
}
