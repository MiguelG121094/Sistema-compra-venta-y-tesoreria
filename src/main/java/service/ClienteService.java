/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Cliente;
import modelo.ClienteDAO;

/**
 *
 * @author Miguel
 */
public class ClienteService {

    public Cliente getCliente(Long idCliente) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            return dao.getCliente(idCliente);
        } catch (SQLException e) {
            System.out.println("Error en ClienteService: " + e);
            return null;
        }
    }

    public List<Cliente> listarClientes() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            return dao.listarClientes();
        } catch (SQLException e) {
            System.out.println("Error en ClienteService: " + e);
            return null;
        }
    }

    public Long insertarCliente(Cliente cliente) throws SQLException {
        Connection conn = null;
        Long idInserted = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            ClienteDAO dao = new ClienteDAO(conn);
            idInserted = dao.insertarCliente(cliente);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ClienteService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return idInserted;
    }

    public void actualizarCliente(Cliente cliente) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            ClienteDAO dao = new ClienteDAO(conn);
            dao.actualizarCliente(cliente);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ClienteService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void eliminarCliente(Long idCliente) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);
            ClienteDAO dao = new ClienteDAO(conn);
            dao.eliminarCliente(idCliente);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("Error en ClienteService: " + e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
