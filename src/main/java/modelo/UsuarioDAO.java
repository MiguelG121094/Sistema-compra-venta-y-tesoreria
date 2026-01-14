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

/**
 *
 * @author Miguel
 */
public class UsuarioDAO {

    private Connection conn;
    private Usuario usuario;
    private Persona persona;
    private PersonaDAO personaDAO;
    private GrupoDAO grupoDAO;

    public UsuarioDAO(Connection conn) {
        this.conn = conn;
    }

    public Usuario validarUsuario(String user, String pass) throws SQLException {
        Usuario usuario = null;
        String sql = "SELECT * FROM usuario WHERE usu_user = ? AND usu_pass = ?";
        personaDAO = new PersonaDAO(conn);
        grupoDAO = new GrupoDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usuario = new Usuario(rs.getLong("id_usuario"), new Persona(rs.getLong("id_persona")), rs.getString("usu_user"), rs.getString("usu_pass"),
                        rs.getString("usu_estado"), new Grupo(rs.getLong("id_grupo")));
            }
        }
        return usuario;
    }
    
    public Usuario getUsuario(Long idUsuario) throws SQLException{
        if (idUsuario == null) {
            System.out.println("Error el parametro idUsuario es nulo");
            return null;
        }
        String sqlUsuario = "SELECT * FROM usuario WHERE id_usuario = ?";
        personaDAO = new PersonaDAO(conn);
        grupoDAO = new GrupoDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
            stmt.setLong(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    persona = personaDAO.getPersona(rs.getLong("id_persona"));
                    return usuario = new Usuario(rs.getLong("id_usuario"), persona, rs.getString("usu_user"),
                            null, rs.getString("usu_estado"), new Grupo(rs.getLong("id_grupo")));
                }
            }
        }
        return null; // si no se encontro el usuario retorna null
    }
    
    public List<Usuario> listarUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        personaDAO = new PersonaDAO(conn);
        grupoDAO = new GrupoDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    persona = personaDAO.getPersona(rs.getLong("id_persona"));
                    Usuario usuario = new Usuario(
                        rs.getLong("id_usuario"),
                        persona,
                        rs.getString("usu_user"),
                        rs.getString("usu_pass"),
                        rs.getString("usu_estado"),
                        new Grupo(rs.getLong("id_grupo"))
                    );
                    usuarios.add(usuario);
                }
            }
        }
        return usuarios;
    }

    public void insertarUsuario(Usuario usuario) throws SQLException {
        if (usuario == null) {
            System.out.println("Error parametro usuario es nulo");
            return;
        }
        
        String sql = "INSERT INTO usuario (id_persona, usu_user, usu_pass, usu_estado, id_grupo) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, usuario.getPersona().getIdPersona());
            stmt.setString(2, usuario.getUsername());
            stmt.setString(3, usuario.getPassword());
            stmt.setString(4, usuario.getEstado());
            stmt.setLong(5, usuario.getGrupo().getIdGrupo());

            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                usuario.setIdUsuario(generatedKeys.getLong(1));
            }
        }
    }
    
}
