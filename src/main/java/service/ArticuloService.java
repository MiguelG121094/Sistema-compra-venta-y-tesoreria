/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.Connection;
import conexion.Conexion;
import java.sql.SQLException;
import java.util.List;
import modelo.Articulo;
import modelo.ArticuloDAO;
/**
 *
 * @author Miguel
 */
public class ArticuloService {
    
    public Articulo getArticulo(Long idArticulo) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            ArticuloDAO articuloDAO = new ArticuloDAO(conn);
            Articulo articulo = articuloDAO.getArticulo(idArticulo);
            return articulo;
        } catch (SQLException e) {
            System.out.println("Error en ArticuloService: "+ e);
            return null;
        }
    }
     
    public List<Articulo> listarArticulo() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            ArticuloDAO articuloDAO = new ArticuloDAO(conn);
            List<Articulo> articulos = articuloDAO.listarArticulo();
            return articulos;
        } catch (SQLException e) {
            System.out.println("Error en ArticuloService: "+ e);
            return null;
        }
    }
    
}
