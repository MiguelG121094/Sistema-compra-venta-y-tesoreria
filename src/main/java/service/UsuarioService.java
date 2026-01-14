/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Usuario;
import modelo.UsuarioDAO;

/**
 *
 * @author Miguel
 */
public class UsuarioService {

    public Usuario validarUsuario(String user, String pass) throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            Usuario usuario = usuarioDAO.validarUsuario(user, pass);
            return usuario;
        } catch (SQLException e) {
            System.out.println("Error en UsuarioService: " + e);
            return null;
        }
    }
    
    public Usuario getUsuario(Long idUsuario) throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            Usuario usuario = usuarioDAO.getUsuario(idUsuario);
            return usuario;
        } catch (SQLException e) {
            System.out.println("Error en UsuarioService: " + e);
            return null;
        }
    }
    
    public List<Usuario> listarUsuarios() throws SQLException {
        try ( Connection conn = Conexion.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            List<Usuario> usuarios = usuarioDAO.listarUsuarios();
            return usuarios;
        } catch (SQLException e) {
            System.out.println("Error en UsuarioService: " + e);
            return null;
        }
    }
    
    public void insertarUsuario(Usuario usuario) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            usuarioDAO.insertarUsuario(usuario);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en UsuarioService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
