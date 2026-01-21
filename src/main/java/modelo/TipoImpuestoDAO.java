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
public class TipoImpuestoDAO {

    private Connection conn;
    private TipoImpuesto tipoImpuesto;

    public TipoImpuestoDAO(Connection conn) {
        this.conn = conn;
    }

    public TipoImpuesto getTipoImpuesto(Long idImpuesto) throws SQLException{
        if (idImpuesto == null) {
            System.out.println("Error el parametro idImpuesto es nulo");
            return null;
        }
        String sql = "SELECT * FROM impuesto WHERE id_impuesto = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idImpuesto);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()) {
                    tipoImpuesto = new TipoImpuesto(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return tipoImpuesto;
    }

    public List<TipoImpuesto> listarTipoImpuesto() throws SQLException {
        List<TipoImpuesto> lista = new ArrayList<>();
        String sql = "SELECT id_impuesto, imp_descripcion FROM impuesto ORDER BY id_impuesto";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TipoImpuesto tipo = new TipoImpuesto(rs.getLong("id_impuesto"), rs.getString("imp_descripcion"));
                lista.add(tipo);
            }
        }
        return lista;
    }

}
