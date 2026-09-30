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
                    sucursal = mapear(rs);
                }
            }
        }
        return sucursal;
    }
    
    /**
     * Mapea la fila por nombre de columna. Antes se leia por indice, y el listado repetia la
     * descripcion en la direccion y corria el estado un lugar.
     */
    private Sucursal mapear(ResultSet rs) throws SQLException {
        Sucursal suc = new Sucursal(rs.getLong("id_sucursal"), rs.getString("suc_descripcion"),
                rs.getString("suc_direccion"), rs.getString("suc_estado"));
        suc.setEstablecimiento(rs.getString("suc_establecimiento"));
        return suc;
    }

    public List listarSucursles() throws SQLException{
        List listaSucursales = new ArrayList<>();
        String sql = "SELECT * FROM sucursal";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    sucursal = mapear(rs);
                    listaSucursales.add(sucursal);
                }
            }
        }
        return listaSucursales;
    }
    
}
