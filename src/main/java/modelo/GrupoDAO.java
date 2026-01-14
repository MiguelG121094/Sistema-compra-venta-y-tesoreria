/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import conexion.Conexion;
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
public class GrupoDAO {

    private Connection conn;
    private Grupo grupo;

    public GrupoDAO(Connection conn) {
        this.conn = conn;
    }
    
    public Grupo getGrupo(Long idGrupo) throws SQLException {
        if (idGrupo == null) {
            System.out.println("Error el parametro idGrupo es nulo");
            return null;
        }
        String sql = "SELECT * FROM grupo WHERE id_grupo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idGrupo);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    grupo = new Grupo(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return grupo;
    }
    
}
