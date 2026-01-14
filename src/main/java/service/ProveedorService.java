/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Proveedor;
import modelo.ProveedorDAO;

/**
 *
 * @author Miguel
 */
public class ProveedorService {
    
    public Proveedor getProveedor(Long idProveedor) throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            ProveedorDAO proveedorDAO = new ProveedorDAO(conn);
            Proveedor proveedor = proveedorDAO.getProveedor(idProveedor);
            return proveedor;
        } catch (SQLException e) {
            System.out.println("Error en ProveedoresService: " + e);
            return null;
        }
    }
    
    public List<Proveedor> listarProveedors() throws SQLException{
        try (Connection conn = Conexion.getConnection()) {
            ProveedorDAO proveedorDAO = new ProveedorDAO(conn);
            List<Proveedor> proveedores = proveedorDAO.listarProveedors();
            return proveedores;
        } catch (SQLException e) {
            System.out.println("Error en ProveedoresService: "+ e);
            return null;
        }
    }
}
