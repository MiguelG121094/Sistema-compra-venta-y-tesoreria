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
public class PedidoCompraDAO {

    private Connection conn;
    private PedidoCompra pedidoCompra;
    private List<PedidoCompraDetalle> listPedidoCompraDetalle = new ArrayList<PedidoCompraDetalle>();
    private PedidoCompraDetalleDAO pedidoCompraDetalleDAO;
    private Usuario usuario = new Usuario();
    private UsuarioDAO usuarioDAO;
    private Persona persona = new Persona();
    private PersonaDAO personaDAO;
    private Sucursal sucursal;
    private SucursalDAO sucursalDAO;
    private Deposito deposito;
    private DepositoDAO depositoDAO;
    private Articulo articulo;
    private ArticuloDAO articuloDAO;
    private static final Logger LOGGER = Logger.getLogger(PedidoCompraDAO.class.getName());

    public PedidoCompraDAO(Connection conn) {
        this.conn = conn;
    }
    
    public PedidoCompra getPedidoCompra(Long idPedidoCompra) throws SQLException {
        if (idPedidoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: el parámetro idPedidoCompra es nulo");
            return null;
        }
        PedidoCompra pedidoCompra = null;
        String sql = "SELECT id_pedido_cab, id_usuario, id_sucursal, ped_comp_fecha, ped_comp_estado FROM pedido_compra_cabecera WHERE id_pedido_cab = ?";
        usuarioDAO = new UsuarioDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedidoCompra);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pedidoCompra = new PedidoCompra(
                        rs.getLong("id_pedido_cab"),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        rs.getDate("ped_comp_fecha"),
                        rs.getString("ped_comp_estado")
                    );
                }
            }
        }
        return pedidoCompra;
    }

    public List<PedidoCompra> listarPedidos() throws SQLException {
        List<PedidoCompra> pedidos = new ArrayList<>();
        String sql = "SELECT id_pedido_cab, id_usuario, id_sucursal, ped_comp_fecha, ped_comp_estado FROM pedido_compra_cabecera";
        usuarioDAO = new UsuarioDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PedidoCompra pedido = new PedidoCompra(
                            rs.getLong("id_pedido_cab"),
                            usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                            sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                            rs.getDate("ped_comp_fecha"),
                            rs.getString("ped_comp_estado")
                    );
                    pedidos.add(pedido);
                }
            }
        }
        return pedidos;
    }
    
    public List<PedidoCompra> listarPedidosConDetalles() throws SQLException {
        List<PedidoCompra> pedidos = new ArrayList<>();
//        Map<Long, PedidoCompra> pedidoMap = new HashMap<>();
        
//        String sql = "SELECT pc.id_pedido_cab, " +
//                 "p.per_nombre || ' ' || p.per_apellido AS usuario_nombre, " +
//                 "s.suc_descripcion, " +
//                 "pc.ped_comp_fecha, " +
//                 "pc.ped_comp_estado, " +
//                 "a.id_articulo, a.art_descripcion, a.art_precio_compra, a.art_precio_venta, " +
//                 "d.id_deposito, d.dep_descripcion, " +
//                 " pd.ped_comp_det_cantidad " +
//                 "FROM pedido_compra_cabecera pc " +
//                 "JOIN usuario u ON pc.id_usuario = u.id_usuario " +
//                 "JOIN persona p ON u.id_persona = p.id_persona " +
//                 "JOIN sucursal s ON pc.id_sucursal = s.id_sucursal " +
//                 "LEFT JOIN pedido_compra_detalle pd ON pc.id_pedido_cab = pd.id_pedido_cab " +
//                 "LEFT JOIN articulo a ON pd.id_articulo = a.id_articulo " +
//                 "LEFT JOIN deposito d ON pd.id_deposito = d.id_deposito " +
//                 "ORDER BY pc.id_pedido_cab, a.id_articulo";

//        String sqlPedidoCabecera = "SELECT id_pedido_cab, id_usuario, id_sucursal, ped_comp_fecha, ped_comp_estado FROM pedido_compra_cabecera";
//
//        try (PreparedStatement stmt = conn.prepareStatement(sqlPedidoCabecera)) {
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                Long idPedido = rs.getLong("id_pedido_cab");
//
//                // Verificar si el pedido ya existe en el mapa
//            PedidoCompra pedido = pedidoMap.get(idPedido);
//            if (pedido == null) {
//                String usuarioNombre = rs.getString("usuario_nombre");
//                String sucursalDesc = rs.getString("suc_descripcion");
//                Date fecha = rs.getDate("ped_comp_fecha");
//                String estado = rs.getString("ped_comp_estado");
//
//                // Crear objetos relacionados
////                Usuario usuario = new Usuario(usuarioNombre);
//                persona.setNombre(usuarioNombre);
//                Usuario usuario = new Usuario(rs.getLong("id_usuario"), persona, rs.getString("usu_user"), null, null, null);
//                Sucursal sucursal = new Sucursal(rs.getLong("id_sucursal"),sucursalDesc, null,null);
//
//                // Crear pedido y agregarlo al mapa
//                pedido = new PedidoCompra(idPedido, usuario, sucursal, fecha, estado);
//                pedidoMap.put(idPedido, pedido);
//                pedidos.add(pedido);
//            }
//
//            // Si el pedido tiene detalles, agregarlos
//            Long idArticulo = rs.getLong("id_articulo");
//            if (idArticulo != 0) { // Evitar registros sin detalles
//                String articuloDesc = rs.getString("art_descripcion");
//                Long precioCompra = rs.getLong("art_precio_compra");
//                Long precioVenta = rs.getLong("art_precio_venta");
//                Long cantidad = rs.getLong("ped_comp_det_cantidad");
//
//                String depositoDesc = rs.getString("dep_descripcion");
//                Deposito deposito = new Deposito(null,depositoDesc,sucursal);
//
//                Articulo articulo = new Articulo(idArticulo, null, null, null, null, articuloDesc, precioCompra, precioVenta, null);
//                PedidoCompraDetalle detalle = new PedidoCompraDetalle(pedido, articulo, cantidad, deposito);
//                    pedido.getDetalles().add(detalle);
//                }
//            }
//            
//        } catch (SQLException e) {
//            System.out.println("Error al listar pedidos con detalles");
//            e.printStackTrace();
//        }
        
        
//        List<PedidoCompra> listPedidosCompra = listarPedidos();
//        
//        for (PedidoCompra pedCompra : listPedidosCompra) {
//            List<PedidoCompraDetalle> listPedCompraDetalle = pedidoCompraDetalleDAO.listarDetallesPorPedido(pedCompra.getIdPedido());
//            List<PedidoCompraDetalle> listPedCompraDetalleAux = new ArrayList<PedidoCompraDetalle>();
//            for (PedidoCompraDetalle pedCompDetalle : listPedCompraDetalle){
//                Articulo articulo = articuloDAO.getAticulo(pedCompDetalle.getArticulo().getIdArticulo());
//                Deposito deposito = depositoDAO.getDepostio(pedCompDetalle.getDeposito().getIdDeposito());
//                PedidoCompraDetalle pedCompDetalleAux = new PedidoCompraDetalle
//                (pedCompDetalle.getPedido(), articulo, pedCompDetalle.getCantidad(), deposito);
//                listPedidoCompraDetalle.add(pedCompDetalleAux);
//            }
//            pedidoCompra.setDetalles(listPedidoCompraDetalle);
//        }

        // con este query obtenemos el pedido de compra con su detalle, los articulos se concatenan en una sola columna por pedido
        String sql = "SELECT \n" +
                    "    pc.id_pedido_cab, \n" +
                    "    pc.id_usuario,\n" +
                    "    p.per_nombre || ' ' || p.per_apellido AS usuario_nombre, \n" +
                    "    s.id_sucursal,\n" +
                    "    s.suc_descripcion, \n" +
                    "    pc.ped_comp_fecha, \n" +
                    "    pc.ped_comp_estado, \n" +
                    "    COALESCE(STRING_AGG(a.art_descripcion || ' (Cant: ' || pd.ped_comp_det_cantidad || ')', ', ')) AS articulos\n" +
                    "FROM \n" +
                    "    pedido_compra_cabecera pc\n" +
                    "JOIN \n" +
                    "    usuario u ON pc.id_usuario = u.id_usuario\n" +
                    "JOIN \n" +
                    "    persona p ON u.id_persona = p.id_persona\n" +
                    "JOIN \n" +
                    "    sucursal s ON pc.id_sucursal = s.id_sucursal\n" +
                    "LEFT JOIN \n" +
                    "    pedido_compra_detalle pd ON pc.id_pedido_cab = pd.id_pedido_cab\n" +
                    "LEFT JOIN \n" +
                    "    articulo a ON pd.id_articulo = a.id_articulo\n" +
                    "GROUP BY \n" +
                    "    pc.id_pedido_cab, pc.id_usuario, usuario_nombre, s.id_sucursal, \n" +
                    "    s.suc_descripcion, pc.ped_comp_fecha, pc.ped_comp_estado\n" +
                    "ORDER BY\n" +
                    "    pc.id_pedido_cab;";
        usuarioDAO = new UsuarioDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuario = usuarioDAO.getUsuario(rs.getLong("id_usuario"));
                    sucursal = sucursalDAO.getSucursal(rs.getLong("id_sucursal"));
                    PedidoCompra pedCompConDetalleConcat = new PedidoCompra(rs.getLong("id_pedido_cab"),
                            usuario, sucursal, rs.getDate("ped_comp_fecha"), rs.getString("ped_comp_estado"), rs.getString("articulos"));
                    pedidos.add(pedCompConDetalleConcat);
                }
            }
        }
        return pedidos;
    }
    
    public Long obtenerUltimoIdPedidoCompra_old() throws SQLException{
//        String sql = "SELECT MAX(pc.id_pedido_cab) FROM pedido_compra_cabecera pc;";
        String sql = "SELECT last_value + increment_by AS proximo_id\n" +
                    "FROM pg_sequences\n" +
                    "WHERE schemaname = 'public' AND sequencename = 'pedido_compra_cabecera_id_pedido_cab_seq';";
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
    
    public Long obtenerProximoIdPedidoCompra() throws SQLException {
    Long proximoId = null;

    // obtenemos el próximo id (sin consumir la secuencia)
    String sqlSecuencia = 
        "SELECT " +
        "CASE " +
        "   WHEN is_called THEN last_value + 1 " + // si ya se uso, sumamos 1
        "   ELSE start_value " +                   // si nunca se uso, usamos el valor inicial
        "END AS proximo_id " +
        "FROM pedido_compra_cabecera_id_pedido_cab_seq;";

    try (PreparedStatement stmt = conn.prepareStatement(sqlSecuencia);
         ResultSet rs = stmt.executeQuery()) {

        if (rs.next()) {
            proximoId = rs.getLong("proximo_id");
        }
    }

    // verificar si ese id ya existe en la tabla
    String sqlVerificar =
        "SELECT COUNT(*) FROM pedido_compra_cabecera WHERE id_pedido_cab = ?";

    boolean existe = true;
    while (existe) {
        try (PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar)) {
            stmtVerificar.setLong(1, proximoId);
            try (ResultSet rsVerificar = stmtVerificar.executeQuery()) {
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

    public Long insertarPedido(PedidoCompra pedido) throws SQLException {
        if (pedido == null) {
            LOGGER.log(Level.SEVERE, "Error: El pedido de compra es nulo");
            return null;
        }

        String sql = "INSERT INTO pedido_compra_cabecera (id_usuario, id_sucursal, ped_comp_fecha, ped_comp_estado) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, pedido.getUsuario().getIdUsuario());
            stmt.setLong(2, pedido.getSucursal().getIdSucursal());
            stmt.setDate(3, new java.sql.Date(pedido.getFecha().getTime()));
            stmt.setString(4, pedido.getEstado());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el pedido, ninguna fila afectada");
            }
//             Obtener la clave generada
            try ( ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                Long idGenerado = generatedKeys.getLong(1);
                    pedido.setIdPedido(idGenerado);
                    System.out.println("ID generado: " + pedido.getIdPedido());
                    LOGGER.log(Level.INFO, "Pedido de compra insertado correctamente con ID: {0}", pedido.getIdPedido());
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el pedido.");
                }
            }
        }
    }

    public void actualizarPedido(PedidoCompra pedido) throws SQLException {
        if (pedido == null || pedido.getIdPedido() == null) {
            LOGGER.log(Level.WARNING, "Error: pedido de compra es nulo");
            return;
        }

        String sql = "UPDATE pedido_compra_cabecera SET id_usuario = ?, id_sucursal = ?, ped_comp_fecha = ?, ped_comp_estado = ? WHERE id_pedido_cab = ?";

         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pedido.getUsuario().getIdUsuario());
            stmt.setLong(2, pedido.getSucursal().getIdSucursal());
            stmt.setDate(3, new java.sql.Date(pedido.getFecha().getTime()));
            stmt.setString(4, pedido.getEstado());
            stmt.setLong(5, pedido.getIdPedido());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el pedido, ninguna fila afectada");
            }
            // Obtener la clave generada
            try ( ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pedido.setIdPedido(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el pedido.");
                }
            }
        }
    }

    public void eliminarPedido(Long idPedido) throws SQLException {
        if (idPedido == null) {
            LOGGER.log(Level.WARNING, "Error: id del pedido de compra es nulo");
            return;
        }

        String sql = "DELETE FROM pedido_compra_cabecera WHERE id_pedido_cab = ?";

         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedido);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Pedido de compra eliminado correctamente");
            } else {
                System.out.println("No se encontró el pedido de compra con ID: " + idPedido);
            }
        }
    }

    public void actualizarPedidoCabecera(PedidoCompra pedidoCompra) throws SQLException {
        if (pedidoCompra == null || pedidoCompra.getIdPedido() == null) {
            throw new IllegalArgumentException("El pedido de compra es nulo");
        }
        
        // bloquear la cabecera del pedido para evitar modificaciones concurrentes
        String sqlCabecera = "SELECT 1 FROM pedido_compra_cabecera WHERE id_pedido_cab = ? FOR UPDATE";
        try ( PreparedStatement stmt = conn.prepareStatement(sqlCabecera)) {
            stmt.setLong(1, pedidoCompra.getIdPedido());
            try ( ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Pedido no encontrado: " + pedidoCompra.getIdPedido());
                }
            }
        }

        // Actualizar la cabecera del pedido
        String updateCabecera = "UPDATE pedido_compra_cabecera "
                + "SET id_usuario = ?, id_sucursal = ?, ped_comp_fecha = ?, ped_comp_estado = ? "
                + "WHERE id_pedido_cab = ?";
        try ( PreparedStatement stmt = conn.prepareStatement(updateCabecera)) {
            stmt.setLong(1, pedidoCompra.getUsuario().getIdUsuario());
            stmt.setLong(2, pedidoCompra.getSucursal().getIdSucursal());
            stmt.setDate(3, new java.sql.Date(pedidoCompra.getFecha().getTime()));
            stmt.setString(4, pedidoCompra.getEstado());
            stmt.setLong(5, pedidoCompra.getIdPedido());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("Error: No se actualizó ningún pedido");
            }
        }
    }

}
