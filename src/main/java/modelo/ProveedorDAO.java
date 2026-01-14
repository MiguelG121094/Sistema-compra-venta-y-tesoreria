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
public class ProveedorDAO {

    private Connection conn;
    private Proveedor proveedor;
    private TipoEntidad tipoEntidad;
    private TipoEntidadDAO tipoentidadDAO;
    private Persona persona;
    private PersonaDAO personaDAO;

    public ProveedorDAO(Connection conn) {
        this.conn = conn;
    }

    public Proveedor getProveedor(Long idProveedor) throws SQLException{
        if (idProveedor == null) {
            System.out.println("Error el parametro idProveedor es nulo");
            return null;
        }
        String sql = "SELECT * FROM proveedor WHERE id_proveedor = ?";
        tipoentidadDAO = new TipoEntidadDAO(conn);
        personaDAO = new PersonaDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProveedor);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    tipoEntidad = tipoentidadDAO.getTipoEntidad(rs.getLong("id_tipo_entidad"));
                    persona = personaDAO.getPersona(rs.getLong("id_persona"));
                    proveedor = new Proveedor(rs.getLong(1), rs.getString(2), persona, tipoEntidad, rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8));
                }
            }
        }
        return proveedor;
    }
    
    public List<Proveedor> listarProveedors() throws SQLException{
        List<Proveedor> listaProveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedor";
        tipoentidadDAO = new TipoEntidadDAO(conn);
        personaDAO = new PersonaDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    tipoEntidad = tipoentidadDAO.getTipoEntidad(rs.getLong("id_tipo_entidad"));
                    persona = personaDAO.getPersona(rs.getLong("id_persona"));
                    proveedor = new Proveedor(rs.getLong(1), rs.getString(2), persona, tipoEntidad, rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8));
                    listaProveedores.add(proveedor);
                }
            }
        }
        return listaProveedores;
    }
    
}
