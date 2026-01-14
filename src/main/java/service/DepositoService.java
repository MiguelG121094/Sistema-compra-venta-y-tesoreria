/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Deposito;
import modelo.DepositoDAO;

/**
 *
 * @author Miguel
 */
public class DepositoService {
    
    public Deposito getDeposito(Long idDeposito) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            DepositoDAO depositoDAO = new DepositoDAO(conn);
            Deposito deposito = depositoDAO.getDepostio(idDeposito);
            return deposito;
        } catch (SQLException e) {
            System.out.println("Error en DepositoService: "+ e);
            return null;
        }
    }
    
    public List<Deposito> listarDepostio() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            DepositoDAO depositoDAO = new DepositoDAO(conn);
            List<Deposito> depositos = depositoDAO.listarDepostio();
            return depositos;
        } catch (SQLException e) {
            System.out.println("Error en DepositoService: "+ e);
            return null;
        }
    }
    
    public List<Deposito> listarDepostioPorSucursal(Long idSucursal) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            DepositoDAO depositoDAO = new DepositoDAO(conn);
            List<Deposito> depositos = depositoDAO.listarDepostio();
            return depositos;
        } catch (SQLException e) {
            System.out.println("Error en DepositoService: "+ e);
            return null;
        }
    }
    
}
