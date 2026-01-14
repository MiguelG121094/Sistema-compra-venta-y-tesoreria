/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Persona;
import modelo.PersonaDAO;

/**
 *
 * @author Miguel
 */
public class PersonaService {
    
    public Persona getPersona(Long idPersona) throws SQLException{
        try (Connection conn = Conexion.getConnection()) {
            PersonaDAO personaDAO = new PersonaDAO(conn);
            Persona persona = personaDAO.getPersona(idPersona);
            return persona;
        } catch (SQLException e) {
            System.out.println("Error en PersonaService: "+ e);
            return null;
        }
    }
     
    public List<Persona> listarPersonas() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            PersonaDAO personaDAO = new PersonaDAO(conn);
            List<Persona> personas = personaDAO.listarPersonas();
            return personas;
        } catch (SQLException e) {
            System.out.println("Error en PersonaService: "+ e);
            return null;
        }
    }
    
    public void insertarPersona(Persona persona) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            PersonaDAO personaDAO = new PersonaDAO(conn);
            personaDAO.insertarPersona(persona);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en PersonaService: " + e);
        } finally {
        if (conn != null) {
            conn.close();
        }
    }
    }
}
