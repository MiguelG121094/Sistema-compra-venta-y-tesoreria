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
public class ArticuloDAO {

    private Connection conn;
    private Articulo articulo;
    private TipoArticuloDAO tipoArticuloDAO;
    private MarcaDAO marcaDAO;
    private TipoImpuestoDAO tipoImpuestoDAO;
    private PresentacionDAO presentacionDAO;
    
    public ArticuloDAO(Connection conn) {
        this.conn = conn;
    }
    
    public Articulo getArticulo(Long idArticulo) throws SQLException {
        if (idArticulo == null) {
            throw new IllegalArgumentException("Error el parametro idAticulo es nulo");
        }
        String sqlArticulo = "SELECT * FROM articulo WHERE id_articulo = ?";
        tipoArticuloDAO = new TipoArticuloDAO(conn);
        marcaDAO = new MarcaDAO(conn);
        tipoImpuestoDAO = new TipoImpuestoDAO(conn);
        presentacionDAO = new PresentacionDAO(conn);
        new PresentacionDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sqlArticulo)) {
            stmt.setLong(1, idArticulo);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    articulo = new Articulo(rs.getLong(1), tipoArticuloDAO.cargarTipoaArticulo(rs.getLong(2)), marcaDAO.getMarca(rs.getLong(3)),
                            tipoImpuestoDAO.getTipoImpuesto(rs.getLong(4)), presentacionDAO.getPresentacion(rs.getLong(5)),
                            rs.getString(6), rs.getLong(7), rs.getLong(8), rs.getString(9));
                }
            }
        }   
        return articulo;
    }

    public List<Articulo> listarArticulo() throws SQLException {
        List<Articulo> articuloList = new ArrayList<>();;
        String sql = "SELECT * FROM articulo";
        tipoArticuloDAO = new TipoArticuloDAO(conn);
        marcaDAO = new MarcaDAO(conn);
        tipoImpuestoDAO = new TipoImpuestoDAO(conn);
        presentacionDAO = new PresentacionDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    articulo = new Articulo(rs.getLong(1), tipoArticuloDAO.cargarTipoaArticulo(rs.getLong(2)), marcaDAO.getMarca(rs.getLong(3)),
                            tipoImpuestoDAO.getTipoImpuesto(rs.getLong(4)), presentacionDAO.getPresentacion(rs.getLong(5)),
                            rs.getString(6), rs.getLong(7), rs.getLong(8), rs.getString(9));
                    articuloList.add(articulo);
                }
        }
        return articuloList;
    }
    
//    public TipoArticulo cargarTipoArticulo(Long id) throws SQLException {
//        if (id == null) {
//            System.out.println("Error id del tipo de articulo es nulo");
//            return null;
//        }
//
//        TipoArticulo ta = null;
//        String sql = "SELECT * FROM tipo_articulo " +
//                    "WHERE id_tipo_articulo = ?";
//
//        new TipoArticuloDAO(conn);
//        new MarcaDAO(conn);
//        new TipoImpuestoDAO(conn);
//        new PresentacionDAO(conn);
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setLong(1, id);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    ta = new TipoArticulo(rs.getLong(1), rs.getString(2));
//                }
//            }
//        }
//        return ta;
//    }
//    
//    @Transactional
//    public void insertarTipoArticulo(TipoArticulo tipoArticulo) {
//        if (tipoArticulo == null) {
//            System.out.println("Error el tipo de articulo es nulo");
//            return;
//        }
//        String sql = "INSERT INTO public.tipo_articulo(tipo_art_descripcion) VALUES (?);";
//
//       try ( Connection conn = Conexion.getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, tipoArticulo.getDescripcion());
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            System.out.println("Error al insertar Tipo de Artículo");
//            e.printStackTrace();
//        }
//    }
//    
//    @Transactional
//    public void actualizarTipoArticulo(TipoArticulo tipoArticulo) {
//        if (tipoArticulo == null) {
//            System.out.println("Error el tipo de articulo es nulo");
//            return;
//        }
//        String sql = "UPDATE public.tipo_articulo SET tipo_art_descripcion=? " +
//                    "WHERE id_tipo_articulo=?;";
//
//       try ( Connection conn = Conexion.getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, tipoArticulo.getDescripcion());
//            stmt.setLong(2, tipoArticulo.getIdTipoArticulo());
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            System.out.println("Error al actualizar tipo de artículo");
//            e.printStackTrace();
//        }
//    }
//    
//    @Transactional
//    public void eliminarTipoArticulo(Long id) {
//        if (id == null) {
//            System.out.println("Error el id de ltipo de articulo es nulo");
//            return;
//        }
//        String sql = "DELETE FROM public.tipo_articulo " +
//                    "WHERE id_tipo_articulo= ?";
//
//       try ( Connection conn = Conexion.getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setLong(1, id);
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            System.out.println("Error al eliminar el tipo de artículo con id: " + id);
//            e.printStackTrace();
//        }
//    }
    
}
