/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class FacturaVentaDAO {

    private Connection conn;
    private PedidoVentaDAO pedidoVentaDAO;
    private ClienteDAO clienteDAO;
    private UsuarioDAO usuarioDAO;
    private SucursalDAO sucursalDAO;
    private AperturaCierreCajaDAO aperturaCierreCajaDAO;
    private TimbradoDAO timbradoDAO;
    private static final Logger LOGGER = Logger.getLogger(FacturaVentaDAO.class.getName());

    public FacturaVentaDAO(Connection conn) {
        this.conn = conn;
    }

    public FacturaVenta getFacturaVenta(Long idFacturaVenta) throws SQLException {
        if (idFacturaVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaVenta es nulo");
            return null;
        }
        FacturaVenta factura = null;
        String sql = "SELECT fvc.id_fact_venta_cab, fvc.fact_venta_fecha_emision, fvc.fact_venta_numero, " +
                    "fvc.fact_venta_condicion, fvc.fact_venta_plazo, fvc.fact_venta_observacion, " +
                    "fvc.fact_venta_estado, fvc.id_ped_venta_cab, fvc.id_cliente, fvc.id_usuario, " +
                    "fvc.id_sucursal, fvc.id_aper_cier_caja, fvc.id_timbrado, " +
                    "STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM factura_venta_cabecera fvc " +
                    "LEFT JOIN factura_venta_detalle fvd ON fvc.id_fact_venta_cab = fvd.id_fact_venta_cab " +
                    "LEFT JOIN articulo a ON fvd.id_articulo = a.id_articulo " +
                    "WHERE fvc.id_fact_venta_cab = ? " +
                    "GROUP BY fvc.id_fact_venta_cab, fvc.fact_venta_fecha_emision, fvc.fact_venta_numero, " +
                    "fvc.fact_venta_condicion, fvc.fact_venta_plazo, fvc.fact_venta_observacion, " +
                    "fvc.fact_venta_estado, fvc.id_ped_venta_cab, fvc.id_cliente, fvc.id_usuario, " +
                    "fvc.id_sucursal, fvc.id_aper_cier_caja, fvc.id_timbrado";

        pedidoVentaDAO = new PedidoVentaDAO(conn);
        clienteDAO = new ClienteDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        aperturaCierreCajaDAO = new AperturaCierreCajaDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    factura = new FacturaVenta(
                        rs.getLong("id_fact_venta_cab"),
                        rs.getDate("fact_venta_fecha_emision"),
                        rs.getInt("fact_venta_numero"),
                        rs.getString("fact_venta_condicion"),
                        rs.getInt("fact_venta_plazo"),
                        rs.getString("fact_venta_observacion"),
                        rs.getString("fact_venta_estado"),
                        pedidoVentaDAO.getPedidoVenta(rs.getLong("id_ped_venta_cab")),
                        clienteDAO.getCliente(rs.getLong("id_cliente")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        aperturaCierreCajaDAO.getAperturaCierreCaja(rs.getLong("id_aper_cier_caja")),
                        timbradoDAO.getTimbrado(rs.getLong("id_timbrado")),
                        rs.getString("lista_articulos")
                    );
                }
            }
        }
        return factura;
    }

    public List<FacturaVenta> listarFacturasVenta() throws SQLException {
        List<FacturaVenta> facturas = new ArrayList<>();
        String sql = "SELECT fvc.id_fact_venta_cab, fvc.fact_venta_fecha_emision, fvc.fact_venta_numero, " +
                    "fvc.fact_venta_condicion, fvc.fact_venta_plazo, fvc.fact_venta_observacion, " +
                    "fvc.fact_venta_estado, fvc.id_ped_venta_cab, fvc.id_cliente, fvc.id_usuario, " +
                    "fvc.id_sucursal, fvc.id_aper_cier_caja, fvc.id_timbrado, " +
                    "STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM factura_venta_cabecera fvc " +
                    "LEFT JOIN factura_venta_detalle fvd ON fvc.id_fact_venta_cab = fvd.id_fact_venta_cab " +
                    "LEFT JOIN articulo a ON fvd.id_articulo = a.id_articulo " +
                    "GROUP BY fvc.id_fact_venta_cab, fvc.fact_venta_fecha_emision, fvc.fact_venta_numero, " +
                    "fvc.fact_venta_condicion, fvc.fact_venta_plazo, fvc.fact_venta_observacion, " +
                    "fvc.fact_venta_estado, fvc.id_ped_venta_cab, fvc.id_cliente, fvc.id_usuario, " +
                    "fvc.id_sucursal, fvc.id_aper_cier_caja, fvc.id_timbrado";

        pedidoVentaDAO = new PedidoVentaDAO(conn);
        clienteDAO = new ClienteDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);
        sucursalDAO = new SucursalDAO(conn);
        aperturaCierreCajaDAO = new AperturaCierreCajaDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                FacturaVenta factura = new FacturaVenta(
                    rs.getLong("id_fact_venta_cab"),
                    rs.getDate("fact_venta_fecha_emision"),
                    rs.getInt("fact_venta_numero"),
                    rs.getString("fact_venta_condicion"),
                    rs.getInt("fact_venta_plazo"),
                    rs.getString("fact_venta_observacion"),
                    rs.getString("fact_venta_estado"),
                    pedidoVentaDAO.getPedidoVenta(rs.getLong("id_ped_venta_cab")),
                    clienteDAO.getCliente(rs.getLong("id_cliente")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    aperturaCierreCajaDAO.getAperturaCierreCaja(rs.getLong("id_aper_cier_caja")),
                    timbradoDAO.getTimbrado(rs.getLong("id_timbrado")),
                    rs.getString("lista_articulos")
                );
                facturas.add(factura);
            }
        }
        return facturas;
    }

    public Long insertarFacturaVenta(FacturaVenta factura) throws SQLException {
        if (factura == null) {
            LOGGER.log(Level.SEVERE, "Error: La factura de venta es nula");
            return null;
        }

        String sql = "INSERT INTO factura_venta_cabecera (fact_venta_fecha_emision, fact_venta_numero, " +
                    "fact_venta_condicion, fact_venta_plazo, fact_venta_observacion, fact_venta_estado, " +
                    "id_ped_venta_cab, id_cliente, id_usuario, id_sucursal, id_aper_cier_caja, id_timbrado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, new java.sql.Date(factura.getFechaEmision().getTime()));
            stmt.setInt(2, factura.getNumero());
            stmt.setString(3, factura.getCondicion());
            stmt.setInt(4, factura.getPlazo());
            stmt.setString(5, factura.getObservacion());
            stmt.setString(6, factura.getEstado());
            stmt.setLong(7, factura.getPedidoVenta().getIdPedidoVenta());
            stmt.setLong(8, factura.getCliente().getIdCliente());
            stmt.setLong(9, factura.getUsuario().getIdUsuario());
            stmt.setLong(10, factura.getSucursal().getIdSucursal());
            stmt.setLong(11, factura.getAperturaCierreCaja().getIdAperturaCierreCaja());
            stmt.setLong(12, factura.getTimbrado().getIdTimbrado());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la factura de venta, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    factura.setIdFacturaVenta(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la factura de venta.");
                }
            }
        }
    }

    public void actualizarFacturaVenta(FacturaVenta factura) throws SQLException {
        if (factura == null || factura.getIdFacturaVenta() == null) {
            LOGGER.log(Level.WARNING, "Error: factura de venta es nula");
            return;
        }

        String sql = "UPDATE factura_venta_cabecera SET fact_venta_fecha_emision = ?, fact_venta_numero = ?, " +
                    "fact_venta_condicion = ?, fact_venta_plazo = ?, fact_venta_observacion = ?, " +
                    "fact_venta_estado = ?, id_ped_venta_cab = ?, id_cliente = ?, id_usuario = ?, " +
                    "id_sucursal = ?, id_aper_cier_caja = ?, id_timbrado = ? WHERE id_fact_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(factura.getFechaEmision().getTime()));
            stmt.setInt(2, factura.getNumero());
            stmt.setString(3, factura.getCondicion());
            stmt.setInt(4, factura.getPlazo());
            stmt.setString(5, factura.getObservacion());
            stmt.setString(6, factura.getEstado());
            stmt.setLong(7, factura.getPedidoVenta().getIdPedidoVenta());
            stmt.setLong(8, factura.getCliente().getIdCliente());
            stmt.setLong(9, factura.getUsuario().getIdUsuario());
            stmt.setLong(10, factura.getSucursal().getIdSucursal());
            stmt.setLong(11, factura.getAperturaCierreCaja().getIdAperturaCierreCaja());
            stmt.setLong(12, factura.getTimbrado().getIdTimbrado());
            stmt.setLong(13, factura.getIdFacturaVenta());

            stmt.executeUpdate();
        }
    }

    public void eliminarFacturaVenta(Long idFacturaVenta) throws SQLException {
        if (idFacturaVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idFacturaVenta es nulo");
            return;
        }

        String sql = "DELETE FROM factura_venta_cabecera WHERE id_fact_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFacturaVenta);
            stmt.executeUpdate();
        }
    }
}
