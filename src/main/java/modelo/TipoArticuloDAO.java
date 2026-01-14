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
public class TipoArticuloDAO {

    private Connection conn;

    public TipoArticuloDAO(Connection conn) {
        this.conn = conn;
    }

    public List<TipoArticulo> listarTipoArticulo() throws SQLException {
        List<TipoArticulo> tipoArticulo = new ArrayList<>();;
        String sql = "SELECT * FROM tipo_articulo";

        try ( PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TipoArticulo tipoArticuloAux = new TipoArticulo(rs.getLong(1), rs.getString(2));
                tipoArticulo.add(tipoArticuloAux);
            }
            if (rs.next()) {
            }
        }
        return tipoArticulo;
    }
    
    public TipoArticulo cargarTipoaArticulo(Long id) throws SQLException {
        if (id == null) {
            System.out.println("Error id del tipo de articulo es nulo");
            return null;
        }

        TipoArticulo ta = null;
        String sql = "SELECT * FROM tipo_articulo " +
                    "WHERE id_tipo_articulo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ta = new TipoArticulo(rs.getLong("id_tipo_articulo"), rs.getString("tipo_art_descripcion"));
                }
            }
        }
        return ta;
    }
    
    public void insertarTipoArticulo(TipoArticulo tipoArticulo) throws SQLException {
        if (tipoArticulo == null) {
            System.out.println("Error el tipo de articulo es nulo");
            return;
        }
        String sql = "INSERT INTO public.tipo_articulo(tipo_art_descripcion) VALUES (?);";

        try ( PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoArticulo.getDescripcion());
            stmt.executeUpdate();

        }
    }
    
    public void actualizarTipoArticulo(TipoArticulo tipoArticulo) throws SQLException {
        if (tipoArticulo == null) {
            System.out.println("Error el tipo de articulo es nulo");
            return;
        }
        String sql = "UPDATE public.tipo_articulo SET tipo_art_descripcion=? " +
                    "WHERE id_tipo_articulo=?;";

        try ( PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoArticulo.getDescripcion());
            stmt.setLong(2, tipoArticulo.getIdTipoArticulo());
            stmt.executeUpdate();

        }
    }
    
    public void eliminarTipoArticulo(Long id) throws SQLException {
        if (id == null) {
            System.out.println("Error el id del tipo de articulo es nulo");
            return;
        }
        String sql = "DELETE FROM public.tipo_articulo " +
                    "WHERE id_tipo_articulo= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
}
