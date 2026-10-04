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
public class PresentacionDAO {

    private Connection conn;
    private Presentacion presentacion;

    public PresentacionDAO(Connection conn) {
        this.conn = conn;
    }
    
    public Presentacion getPresentacion(Long idPresentacion) throws SQLException{
        if (idPresentacion == null) {
            System.out.println("Error el parametro idPresentacion es nulo");
            return null;
        }
        String sql = "SELECT * FROM presentacion WHERE id_presentacion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPresentacion);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    presentacion = new Presentacion(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return presentacion;
    }
    

    /** Todas las presentaciones, para el combo del ABM de articulos. */
    public List<Presentacion> listarPresentacion() throws SQLException {
        List<Presentacion> presentaciones = new ArrayList<>();
        String sql = "SELECT id_presentacion, pres_descripcion FROM presentacion ORDER BY pres_descripcion";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                presentaciones.add(new Presentacion(rs.getLong("id_presentacion"),
                        rs.getString("pres_descripcion")));
            }
        }
        return presentaciones;
    }
}
