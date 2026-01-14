/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.Transactional;

/**
 *
 * @author Miguel
 */
public class PresupuestoDAO {

    private Connection conn;
    private Presupuesto presupuesto;
    private List<PresupuestoDetalle> presupuestoDetalle = new ArrayList<PresupuestoDetalle>();
    private PresupuestoDetalle presupuestodet;
    private PedidoCompra pedidoCompra;
    private List<PedidoCompraDetalle> listPedidoCompraDetalle = new ArrayList<PedidoCompraDetalle>();
    private PedidoCompraDetalleDAO pedidoCompraDetalleDAO;
    private PedidoCompraDAO pedidoCompraDAO;
    private Usuario usuario = new Usuario();
    private UsuarioDAO usuarioDAO;
    private Persona persona = new Persona();
    private PersonaDAO personaDAO;
    private Proveedor proveedor;
    private ProveedorDAO proveedorDAO;
    private Articulo articulo;
    private ArticuloDAO articuloDAO;
    private static final Logger LOGGER = Logger.getLogger(PresupuestoDAO.class.getName());

    public PresupuestoDAO(Connection conn) {
        this.conn = conn;
    }
    
    public Presupuesto getPresupuesto(Long idPresupuesto) throws SQLException{
        if (idPresupuesto == null) {
            System.out.println("Error el parametro idPresupuesto es nulo");
            return null;
        }
        presupuesto = null;
        String sql = "SELECT id_presupuesto_cab, id_pedido_cab, id_proveedor, presu_cab_fecha, presu_cab_estado, id_usuario "
                + "FROM presupuesto_cabecera WHERE id_presupuesto_cab = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPresupuesto);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()) {
                    pedidoCompra = pedidoCompraDAO.getPedidoCompra(rs.getLong("id_pedido_cab"));
                    proveedor = proveedorDAO.getProveedor(rs.getLong("id_proveedor"));
                    presupuesto = new Presupuesto(rs.getLong("id_presupuesto_cab"), pedidoCompra, proveedor, rs.getDate("presu_cab_fecha"),
                            rs.getString("presu_cab_estado"), usuarioDAO.getUsuario(rs.getLong("id_usuario")));
                }
            }
        }
        return presupuesto;
    }

    public List<Presupuesto> listarPresupuesto() throws SQLException {
        List<Presupuesto> pedidos = new ArrayList<>();
        String sql = "SELECT id_presupuesto_cab, id_pedido_cab, id_proveedor, presu_cab_fecha, presu_cab_estado, id_usuario "
                + "FROM presupuesto_cabecera";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                    pedidoCompra = pedidoCompraDAO.getPedidoCompra(rs.getLong("id_pedido_cab"));
                    proveedor = proveedorDAO.getProveedor(rs.getLong("id_proveedor"));
                    presupuesto = new Presupuesto(rs.getLong("id_presupuesto_cab"), pedidoCompra, proveedor, rs.getDate("presu_cab_fecha"),
                            rs.getString("presu_cab_estado"), usuarioDAO.getUsuario(rs.getLong("id_usuario")));
                pedidos.add(presupuesto);
            }
        }

        return pedidos;
    }
    
    public List<Presupuesto> listarPresupuestoConDetalles() throws SQLException {
        List<Presupuesto> presupuestos = new ArrayList<>();

        // con este query obtenemos el pedido de compra con su detalle, los articulos se concatenan en una sola columna por pedido
        String sql = "SELECT \n" +
                    "    pc.id_presupuesto_cab, \n" +
                    "    pc.id_pedido_cab,\n" +
                    "    pc.id_usuario,\n" +
                    "    p.per_nombre || ' ' || p.per_apellido AS usuario_nombre,\n" +
                    "    pr.id_proveedor,\n" +
                    "    pr.prov_razon_social,\n" +
                    "    pc.presu_cab_fecha,\n" +
                    "    pc.presu_cab_estado,\n" +
                    "    STRING_AGG(a.art_descripcion || ' (Cant: ' || pd.presu_det_cantidad || ' Precio: ' || pd.presu_det_precio_compra || ')', ', ') AS articulos\n" +
                    "FROM presupuesto_cabecera pc\n" +
                    "JOIN usuario u ON pc.id_usuario = u.id_usuario\n" +
                    "JOIN persona p ON u.id_persona = p.id_persona\n" +
                    "JOIN proveedor pr ON pc.id_proveedor = pr.id_proveedor\n" +
                    "JOIN presupuesto_detalle pd ON pc.id_presupuesto_cab = pd.id_presupuesto_cab\n" +
                    "JOIN articulo a ON pd.id_articulo = a.id_articulo\n" +
                    "GROUP BY\n" +
                    "    pc.id_presupuesto_cab, pc.id_pedido_cab, pc.id_usuario, usuario_nombre,\n" +
                    "    pr.id_proveedor, pr.prov_razon_social, pc.presu_cab_fecha, pc.presu_cab_estado\n" +
                    "ORDER BY\n" +
                    "    pc.id_presupuesto_cab;";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pedidoCompraDAO = new PedidoCompraDAO(conn);
                usuarioDAO = new UsuarioDAO(conn);
                proveedorDAO = new ProveedorDAO(conn);
                pedidoCompra = pedidoCompraDAO.getPedidoCompra(rs.getLong("id_pedido_cab"));
                Presupuesto presupuestoConDetalle = new Presupuesto(rs.getLong("id_presupuesto_cab"), pedidoCompra, 
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")), rs.getDate("presu_cab_fecha"), 
                        rs.getString("presu_cab_estado"), usuarioDAO.getUsuario(rs.getLong("id_usuario")), rs.getString("articulos"));
                presupuestos.add(presupuestoConDetalle);
            }
        }
        return presupuestos;
    }
    
    public Long obtenerUltimoIdPresupuesto() throws SQLException{
        String sql = "SELECT MAX(presu.id_presupuesto_cab) FROM presupuesto_cabecera presu;";
        Long ultimoIdPedCompra = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ultimoIdPedCompra = rs.getLong(1);
                }
            }
        }
        
        return ultimoIdPedCompra;
    }

    public Long obtenerProximoIdPresupuestoCompra() throws SQLException {
        Long proximoId = null;

        // obtenemos el próximo id (sin consumir la secuencia)
        String sqlSecuencia
                = "SELECT "
                + "CASE "
                + "   WHEN is_called THEN last_value + 1 "
                + // si ya se uso, sumamos 1
                "   ELSE start_value "
                + // si nunca se uso, usamos el valor inicial
                "END AS proximo_id "
                + "FROM presupuesto_cabecera_id_presupuesto_cab_seq;";

        try ( PreparedStatement stmt = conn.prepareStatement(sqlSecuencia);  ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                proximoId = rs.getLong("proximo_id");
            }
        }

        // verificar si ese id ya existe en la tabla
        String sqlVerificar
                = "SELECT COUNT(*) FROM presupuesto_cabecera WHERE id_presupuesto_cab = ?";

        boolean existe = true;
        while (existe) {
            try ( PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar)) {
                stmtVerificar.setLong(1, proximoId);
                try ( ResultSet rsVerificar = stmtVerificar.executeQuery()) {
                    if (rsVerificar.next()) {
                        existe = rsVerificar.getInt(1) > 0; // true si el id ya está usado
                    }
                }
            }

            // si ya existe, incrementamos manualmente el id hasta encontrar uno libre
            if (existe) {
                proximoId++;
            }
        }

        // retornar el próximo ID disponible
        return proximoId;
    }

    public Long insertarPresupuesto(Presupuesto presupuesto) throws SQLException {
        if (presupuesto == null) {
            System.out.println("Error: El presupuesto de compra es nulo");
            return null;
        }

        String sql = "INSERT INTO presupuesto_cabecera(id_pedido_cab, id_proveedor, presu_cab_fecha, presu_cab_estado, id_usuario)" +
                    "VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, presupuesto.getPedidoCompra().getIdPedido());
            stmt.setLong(2, presupuesto.getProveedor().getIdProveedor());
            stmt.setDate(3, new java.sql.Date(presupuesto.getFecha().getTime()));
            stmt.setString(4, presupuesto.getEstado());
            stmt.setLong(5, presupuesto.getUsuario().getIdUsuario());
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el pedido, ninguna fila afectada");
            }
            
//             Obtener la clave generada
            try ( ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                Long idGenerado = generatedKeys.getLong(1);
                    presupuesto.setIdPresupuesto(idGenerado);
                    LOGGER.log(Level.INFO, "Presupuesto de compra insertado correctamente con ID: {0}", presupuesto.getIdPresupuesto());
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el presupuesto.");
                }
            }
        }
    }

    public void actualizarPresupuesto(Presupuesto presupuesto) throws SQLException {
        if (presupuesto == null || presupuesto.getIdPresupuesto() == null) {
            System.out.println("Error: presupuesto de compra inválido");
            return;
        }
        
        String sql = "UPDATE presupuesto_cabecera SET id_pedido_cab=?, id_proveedor=?, " +
                    "presu_cab_fecha=?, presu_cab_estado=?, id_usuario=? WHERE id_presupuesto_cab = ?;";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, presupuesto.getPedidoCompra().getIdPedido());
            stmt.setLong(2, presupuesto.getProveedor().getIdProveedor());
            stmt.setDate(3, new java.sql.Date(presupuesto.getFecha().getTime()));
            stmt.setString(4, presupuesto.getEstado());
            stmt.setLong(5, presupuesto.getUsuario().getIdUsuario());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Presupuesto de compra actualizado correctamente");
            } else {
                System.out.println("No se encontró el presupuesto de compra con ID: " + presupuesto.getIdPresupuesto());
            }
        }
    }

    public void eliminarPresupuesto(Long idPresupuesto) throws SQLException {
        if (idPresupuesto == null) {
            System.out.println("Error: ID del presupuesto es nulo");
            return;
        }

        String sql = "DELETE FROM presupuesto_cabecera WHERE id_presupuesto_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPresupuesto);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Presupuesto de compra eliminado correctamente");
            } else {
                System.out.println("No se encontró el presupuesto con ID: " + idPresupuesto);
            }
        }
    }

    public void actualizarPresupuestoCabecera(Presupuesto presupuesto) throws SQLException {
        if (presupuesto == null || presupuesto.getIdPresupuesto() == null) {
            LOGGER.log(Level.WARNING, "Error: Presupuesto de compra es nulo");
            return;
        }
        
        // bloquear la cabecera para evitar modificaciones concurrentes
        String sqlCabecera = "SELECT id_presupuesto_cab, id_pedido_cab, id_proveedor, presu_cab_fecha, presu_cab_estado, id_usuario" +
                            "FROM presupuesto_cabecera WHERE id_presupuesto_cab = ? FOR UPDATE";
        try(PreparedStatement stmt = conn.prepareStatement(sqlCabecera)) {
            stmt.setLong(1, presupuesto.getIdPresupuesto());
            try ( ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Presupuesto no encontrado: " + presupuesto.getIdPresupuesto());
                }
            }
        }
        
        // Actualizar la cabecera
        String updateCabecera = "UPDATE presupuesto_cabecera SET id_pedido_cab=?, id_proveedor=?, " +
                "presu_cab_fecha=?, presu_cab_estado=?, id_usuario=? WHERE id_presupuesto_cab = ?;";
        try ( PreparedStatement stmt = conn.prepareStatement(updateCabecera)) {
            stmt.setLong(1, presupuesto.getPedidoCompra().getIdPedido());
            stmt.setLong(2, presupuesto.getProveedor().getIdProveedor());
            stmt.setDate(3, new java.sql.Date(presupuesto.getFecha().getTime()));
            stmt.setString(4, presupuesto.getEstado());
            stmt.setLong(5, presupuesto.getUsuario().getIdUsuario());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw e; // relanzar la excepción para manejar en el service
        }
    }
    
}
