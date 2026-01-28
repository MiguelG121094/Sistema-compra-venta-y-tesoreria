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
import org.mindrot.jbcrypt.BCrypt;

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
        // Buscar solo por username, la verificación de contraseña se hace con BCrypt
        String sql = "SELECT * FROM usuario WHERE usu_user = ?";
        personaDAO = new PersonaDAO(conn);
        grupoDAO = new GrupoDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashGuardado = rs.getString("usu_pass");
                boolean passwordValido = false;

                // Verificar si es hash BCrypt (empieza con $2a$) o texto plano (usuarios antiguos)
                if (hashGuardado != null && hashGuardado.startsWith("$2a$")) {
                    // Contraseña hasheada con BCrypt
                    passwordValido = BCrypt.checkpw(pass, hashGuardado);
                } else {
                    // Contraseña en texto plano (usuarios antiguos sin migrar)
                    passwordValido = hashGuardado != null && hashGuardado.equals(pass);
                }

                if (passwordValido) {
                    usuario = new Usuario(rs.getLong("id_usuario"), new Persona(rs.getLong("id_persona")),
                            rs.getString("usu_user"), hashGuardado,
                            rs.getString("usu_estado"), new Grupo(rs.getLong("id_grupo")));
                }
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
            // Hashear la contraseña con BCrypt antes de guardar
            String hashedPassword = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());
            stmt.setString(3, hashedPassword);
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
