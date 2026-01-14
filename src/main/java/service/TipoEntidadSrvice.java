/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.TipoEntidad;
import modelo.TipoEntidadDAO;

/**
 *
 * @author Miguel
 */
public class TipoEntidadSrvice {
    
    public TipoEntidad getTipoEntidad(Long idTipoEntidad) throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            TipoEntidadDAO tipoEntidadDAO = new TipoEntidadDAO(conn);
            TipoEntidad tipoArticulo = tipoEntidadDAO.getTipoEntidad(idTipoEntidad);
            return tipoArticulo;
        } catch (SQLException e) {
            System.out.println("Error en TipoEntidadService: " + e);
            return null;
        }
    }
    
    public List<TipoEntidad> listarTipoEntidad() throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            TipoEntidadDAO tipoEntidadDAO = new TipoEntidadDAO(conn);
            List<TipoEntidad> tipoArticulos = tipoEntidadDAO.listarTipoEntidad();
            return tipoArticulos;
        } catch (SQLException e) {
            System.out.println("Error en TipoEntidadService: " + e);
            return null;
        }
    }
}
