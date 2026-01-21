/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.TipoImpuesto;
import modelo.TipoImpuestoDAO;

/**
 *
 * @author Miguel
 */
public class TipoImpuestoService {

    public TipoImpuesto getTipoImpuesto(Long idImpuesto) throws SQLException{
        try ( Connection conn = Conexion.getConnection()) {
            TipoImpuestoDAO tipoImpuestoDAO = new TipoImpuestoDAO(conn);
            TipoImpuesto tipoArticulo = tipoImpuestoDAO.getTipoImpuesto(idImpuesto);
            return tipoArticulo;
        } catch (SQLException e) {
            System.out.println("Error en TipoImpuestoService: " + e);
            return null;
        }
    }

    public List<TipoImpuesto> listarTipoImpuesto() throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            TipoImpuestoDAO tipoImpuestoDAO = new TipoImpuestoDAO(conn);
            return tipoImpuestoDAO.listarTipoImpuesto();
        } catch (SQLException e) {
            System.out.println("Error en TipoImpuestoService.listarTipoImpuesto: " + e);
            return null;
        }
    }
}
