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
public class SucursalDAO {

    private Connection conn;
    private Sucursal sucursal;

    public SucursalDAO(Connection conn) {
        this.conn = conn;
    }

    public Sucursal getSucursal(Long idSucursal) throws SQLException{
        if (idSucursal == null) {
            System.out.println("Error el parametro idSucursal es nulo");
            return null;
        }
        String sql = "SELECT * FROM sucursal WHERE id_sucursal = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idSucursal);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    sucursal = new Sucursal(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
                }
            }
        }
        return sucursal;
    }
    
    public List listarSucursles() throws SQLException{
        List listaSucursales = new ArrayList<>();
        String sql = "SELECT * FROM sucursal";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    sucursal = new Sucursal(rs.getLong(1), rs.getString(2), rs.getString(2), rs.getString(3));
                    listaSucursales.add(sucursal);
                }
            }
        }
        return listaSucursales;
    }
    
}
