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
public class MarcaDAO {

    private Connection conn;
    private Marca marca;

    public MarcaDAO(Connection conn) {
        this.conn = conn;
    }
    
    public Marca getMarca(Long idMarca) throws SQLException {
        if (idMarca == null) {
            System.out.println("Error el parametro idMarca es nulo");
            return null;
        }
        String sql = "SELECT * FROM marca WHERE id_marca = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idMarca);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    marca = new Marca(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return marca;
    }
    
}
