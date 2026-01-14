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
import javax.transaction.Transactional;

/**
 *
 * @author Miguel
 */
public class PersonaDAO {

    private Connection conn;
    private Persona persona;

    public PersonaDAO(Connection conn) {
        this.conn = conn;
    }
    
    public Persona getPersona(Long idPersona) throws SQLException{
        if (idPersona == null) {
            System.out.println("Error el parametro idPersona es nulo");
            return null;
        }
        String sqlPersona = "SELECT id_persona, per_nombre, per_apellido, per_ci, per_telefono, per_email, per_direccion, per_fecha_nac FROM persona WHERE id_persona = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPersona)) {
            stmt.setLong(1, idPersona);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    persona = new Persona(rs.getLong("id_persona"), rs.getString("per_nombre"),
                        rs.getString("per_apellido"), rs.getString("per_ci"), rs.getString("per_telefono"),
                        rs.getString("per_email"), rs.getString("per_direccion"), rs.getDate("per_fecha_nac"));
                }
            }
        }
        return persona;
    }

    public List<Persona> listarPersonas() throws SQLException {
        List<Persona> personas = new ArrayList<>();
        String sql = "SELECT * FROM persona";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Persona persona = new Persona(
                    rs.getLong("id_persona"),
                    rs.getString("per_nombre"),
                    rs.getString("per_apellido"),
                    rs.getString("per_ci"),
                    rs.getString("per_telefono"),
                    rs.getString("per_email"),
                    rs.getString("per_direccion"),
                    rs.getDate("per_fecha_nac")
                );
                personas.add(persona);
            }
        }

        return personas;
    }

    public void insertarPersona(Persona persona) throws SQLException  {
        if (persona == null) {
            System.out.println("Error el parametro persona es nulo");
            return;
        }
        String sql = "INSERT INTO persona (per_nombre, per_apellido, per_ci, per_telefono, per_email, per_direccion, per_fecha_nac) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getApellido());
            stmt.setString(3, persona.getCi());
            stmt.setString(4, persona.getTelefono());
            stmt.setString(5, persona.getEmail());
            stmt.setString(6, persona.getDireccion());
            stmt.setDate(7, persona.getFechaNacimiento() != null ? new java.sql.Date(persona.getFechaNacimiento().getTime()) : null);

            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                persona.setIdPersona(generatedKeys.getLong(1));
            }
        }
    }
    
}
