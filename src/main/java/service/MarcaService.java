/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import modelo.Marca;
import modelo.MarcaDAO;

/**
 *
 * @author Miguel
 */
public class MarcaService {
    
    public Marca getArticulo(Long idMarca) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            MarcaDAO marcaDAO = new MarcaDAO(conn);
            Marca marca = marcaDAO.getMarca(idMarca);
            return marca;
        } catch (SQLException e) {
            System.out.println("Error en MarcaService: "+ e);
            return null;
        }
    }
}
