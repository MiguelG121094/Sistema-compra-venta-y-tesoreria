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
import javax.transaction.Transactional;

/**
 *
 * @author Miguel
 */
public class TipoEntidadDAO {

    private Connection conn;
    private TipoEntidad tipoEntidad;

    public TipoEntidadDAO(Connection conn) {
        this.conn = conn;
    }

    public TipoEntidad getTipoEntidad(Long idTipoEntidad) throws SQLException{
        if (idTipoEntidad == null) {
            System.out.println("Error el parametro idTipoEntidad es nulo");
            return null;
        }
        String sql = "SELECT * FROM tipo_entidad WHERE id_tipo_entidad = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idTipoEntidad);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()) {
                    tipoEntidad = new TipoEntidad(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return tipoEntidad;
    }
    
    public List<TipoEntidad> listarTipoEntidad() throws SQLException{
        List listaTipoEntidades = new ArrayList<>();
        String sql = "SELECT * FROM tipo_entidad";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    tipoEntidad = new TipoEntidad(rs.getLong(1), rs.getString(2));
                    listaTipoEntidades.add(tipoEntidad);
                }
            }
        }
        return listaTipoEntidades;
    }
    
}
