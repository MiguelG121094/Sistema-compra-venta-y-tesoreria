/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import modelo.Grupo;
import modelo.GrupoDAO;

/**
 *
 * @author Miguel
 */
public class GrupoService {
    
    public Grupo getArticulo(Long idGrupo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            GrupoDAO grupoDAO = new GrupoDAO(conn);
            Grupo grupo = grupoDAO.getGrupo(idGrupo);
            return grupo;
        } catch (SQLException e) {
            System.out.println("Error en DepositoService: "+ e);
            return null;
        }
    }
     
}
