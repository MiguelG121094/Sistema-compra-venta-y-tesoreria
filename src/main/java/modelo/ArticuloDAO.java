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
import java.sql.Statement;
import java.sql.Types;
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
                            rs.getString(6), rs.getLong(7), rs.getLong(8), rs.getString(9),
                            rs.getString("art_codigo"));
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
                            rs.getString(6), rs.getLong(7), rs.getLong(8), rs.getString(9),
                            rs.getString("art_codigo"));
                    articuloList.add(articulo);
                }
        }
        return articuloList;
    }

    /**
     * Deja en el catálogo el precio al que se compró el artículo por última vez.
     *
     * <p>Lo llama la Factura de Compra dentro de su transacción, por cada línea con artículo.
     * Así {@code art_precio_compra} deja de ser un valor que alguien mantiene a mano y pasa a
     * reflejar siempre la última compra real, que es lo que se muestra como referencia al cargar
     * un presupuesto.
     *
     * <p>Ignora los llamados sin artículo (líneas de gasto/fondo fijo) o sin precio válido, para
     * no pisar el precio del catálogo con un cero.
     *
     * <p><b>Alcance:</b> anular una factura <i>no</i> restaura el precio anterior — habría que
     * guardar un histórico para eso. Es un precio de referencia, no un dato contable: si la
     * última compra se anula, el catálogo queda mostrando ese precio hasta la próxima compra.
     */
    public void actualizarPrecioCompra(Long idArticulo, Long precioCompra) throws SQLException {
        if (idArticulo == null || precioCompra == null || precioCompra <= 0) {
            return;
        }
        String sql = "UPDATE articulo SET art_precio_compra = ? WHERE id_articulo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, precioCompra);
            stmt.setLong(2, idArticulo);
            stmt.executeUpdate();
        }
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
    

    // ==================== ABM ====================

    /** Estados de art_estado. */
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";

    /**
     * Da de alta el articulo. Solo la descripcion, el precio de venta, el estado y el impuesto son
     * obligatorios en la base; el tipo, la marca, la presentacion, el precio de compra y el codigo
     * de barras admiten nulo, asi que se mandan como NULL cuando no vienen cargados.
     *
     * <p>Corre sobre la Connection compartida; la transaccion la controla el Service.
     */
    public Long insertarArticulo(Articulo articulo) throws SQLException {
        if (articulo == null) {
            return null;
        }
        String sql = "INSERT INTO articulo (art_descripcion, art_precio_compra, art_precio_venta, "
                   + "art_estado, art_codigo, id_tipo_articulo, id_marca, id_presentacion, id_impuesto) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(stmt, articulo);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    articulo.setIdArticulo(generatedKeys.getLong(1));
                    return articulo.getIdArticulo();
                }
            }
        }
        return null;
    }

    public void actualizarArticulo(Articulo articulo) throws SQLException {
        if (articulo == null || articulo.getIdArticulo() == null) {
            return;
        }
        String sql = "UPDATE articulo SET art_descripcion = ?, art_precio_compra = ?, "
                   + "art_precio_venta = ?, art_estado = ?, art_codigo = ?, id_tipo_articulo = ?, "
                   + "id_marca = ?, id_presentacion = ?, id_impuesto = ? "
                   + "WHERE id_articulo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            cargarParametros(stmt, articulo);
            stmt.setLong(10, articulo.getIdArticulo());
            stmt.executeUpdate();
        }
    }

    /**
     * Borra el articulo de la base. Si tiene movimientos —pedidos, presupuestos, facturas o stock—
     * la FK lo impide y PostgreSQL devuelve un error de integridad (SQLState 23503), que el Service
     * traduce a un mensaje entendible.
     */
    public void eliminarArticulo(Long idArticulo) throws SQLException {
        if (idArticulo == null) {
            return;
        }
        String sql = "DELETE FROM articulo WHERE id_articulo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idArticulo);
            stmt.executeUpdate();
        }
    }

    /** Los nueve parametros que comparten el alta y la edicion, en el mismo orden. */
    private void cargarParametros(PreparedStatement stmt, Articulo articulo) throws SQLException {
        stmt.setString(1, articulo.getDescripcion());
        setNullableLong(stmt, 2, articulo.getPrecioCompra());
        stmt.setLong(3, articulo.getPrecioVenta() == null ? 0L : articulo.getPrecioVenta());
        stmt.setString(4, articulo.getEstado() != null ? articulo.getEstado() : ESTADO_ACTIVO);
        if (articulo.getCodigo() != null && !articulo.getCodigo().trim().isEmpty()) {
            stmt.setString(5, articulo.getCodigo().trim());
        } else {
            stmt.setNull(5, Types.VARCHAR);
        }
        setNullableLong(stmt, 6, articulo.getTipoArticulo() != null
                ? articulo.getTipoArticulo().getIdTipoArticulo() : null);
        setNullableLong(stmt, 7, articulo.getMarca() != null
                ? articulo.getMarca().getIdMarca() : null);
        setNullableLong(stmt, 8, articulo.getPresentacion() != null
                ? articulo.getPresentacion().getIdPresentacion() : null);
        setNullableLong(stmt, 9, articulo.getTipoImpuesto() != null
                ? articulo.getTipoImpuesto().getIdTipoImpuesto() : null);
    }

    private void setNullableLong(PreparedStatement stmt, int posicion, Long valor) throws SQLException {
        if (valor == null) {
            stmt.setNull(posicion, Types.INTEGER);
        } else {
            stmt.setLong(posicion, valor);
        }
    }
}
