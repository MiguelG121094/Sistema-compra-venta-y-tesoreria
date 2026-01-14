/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import modelo.Presentacion;
import modelo.PresentacionDAO;

/**
 *
 * @author Miguel
 */
public class PresentacionService {

    public Presentacion getPresentacion(Long idPresentacion) throws SQLException{
        try (Connection conn = Conexion.getConnection()) {
            PresentacionDAO presentacionDAO = new PresentacionDAO(conn);
            Presentacion presentacion = presentacionDAO.getPresentacion(idPresentacion);
            return presentacion;
        } catch (SQLException e) {
            System.out.println("Error en PresentacionService: " + e);
            return null;
        }
    }
}
