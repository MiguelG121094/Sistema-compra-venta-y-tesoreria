/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import modelo.TipoImpuesto;
import modelo.TipoImpuestoDAO;

/**
 *
 * @author Miguel
 */
public class TipoImpuestoService {
    
    public TipoImpuesto getTipoImpuesto(Long idImpuesto) throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            TipoImpuestoDAO tipoImpuestoDAO = new TipoImpuestoDAO(conn);
            TipoImpuesto tipoArticulo = tipoImpuestoDAO.getTipoImpuesto(idImpuesto);
            return tipoArticulo;
        } catch (SQLException e) {
            System.out.println("Error en TipoImpuestoService: " + e);
            return null;
        }
    }
}
