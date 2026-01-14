/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Sucursal;
import modelo.SucursalDAO;

/**
 *
 * @author Miguel
 */
public class SucursalService {
    
    public Sucursal getSucursal(Long idSucursal) throws SQLException{
        try (Connection conn = Conexion.getConnection()) {
            SucursalDAO sucursalDAO = new SucursalDAO(conn);
            Sucursal sucursal = sucursalDAO.getSucursal(idSucursal);
            return sucursal;
        } catch (SQLException e) {
            System.out.println("Error en SucursalService: "+ e);
            return null;
        }
    }
    
    public List<Sucursal> listarSucursles() throws SQLException{
        try (Connection conn = Conexion.getConnection()) {
            SucursalDAO sucursalDAO = new SucursalDAO(conn);
            List<Sucursal> sucursales = sucursalDAO.listarSucursles();
            return sucursales;
        } catch (SQLException e) {
            System.out.println("Error en SucursalService: "+ e);
            return null;
        }
    }
}
