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
public class DepositoDAO {

    private Connection conn;
    private Deposito deposito;
    private Sucursal sucursal;
    private SucursalDAO sucursalDAO;

    public DepositoDAO(Connection conn) {
        this.conn = conn;
    }

    public Deposito getDepostio(Long idDepostio) throws SQLException {
        if (idDepostio == null) {
            System.out.println("Error el parametro idDepostio es nulo");
            return null;
        }
        String sql = "SELECT * FROM deposito WHERE id_deposito = ?";
        sucursalDAO =new SucursalDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idDepostio);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    sucursal = sucursalDAO.getSucursal(rs.getLong(4));
                    deposito = new Deposito(rs.getLong(1), rs.getString(2), rs.getString(3), sucursal);
                }
            }
        }
        return deposito;
    }
    
    public List<Deposito> listarDepostio() throws SQLException {
        List listDeposito = new ArrayList();
        String sql = "SELECT * FROM deposito";
        sucursalDAO =new SucursalDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    sucursal = sucursalDAO.getSucursal(rs.getLong(4));
                    deposito = new Deposito(rs.getLong(1), rs.getString(2), rs.getString(3), sucursal);
                    listDeposito.add(deposito);
                }
            }
        }
        return listDeposito;
    }
    
    public List<Deposito> listarDepostioPorSucursal(Long idSucursal) throws SQLException{
        List listDeposito = new ArrayList();
        String sql = "SELECT * FROM deposito WHERE id_sucursal = ?";
        sucursalDAO =new SucursalDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, idSucursal);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()) {
                    sucursal = sucursalDAO.getSucursal(rs.getLong(4));
                    deposito = new Deposito(rs.getLong(1), rs.getString(2), rs.getString(3), sucursal);
                    listDeposito.add(deposito);
                }
            }
        }
        return listDeposito;
    }
    
}
