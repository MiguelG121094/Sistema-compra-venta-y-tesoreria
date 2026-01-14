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
public class NotaRemisionVentaDAO {

    private Connection conn;
    private ClienteDAO clienteDAO;
    private UsuarioDAO usuarioDAO;
    private FacturaVentaDAO facturaVentaDAO;
    private TimbradoDAO timbradoDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaRemisionVentaDAO.class.getName());

    public NotaRemisionVentaDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaRemisionVenta getNotaRemisionVenta(Long idNotaRemisionVenta) throws SQLException {
        if (idNotaRemisionVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaRemisionVenta es nulo");
            return null;
        }
        NotaRemisionVenta nota = null;
        String sql = "SELECT nrv.id_nota_remi_venta, nrv.remi_vent_fecha_emision, nrv.not_remi_vent_descripcion, " +
                    "nrv.not_remi_vent_estado, nrv.id_cliente, nrv.id_usuario, nrv.id_fact_venta_cab, " +
                    "nrv.id_timbrado, STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM nota_remision_venta_cabecera nrv " +
                    "LEFT JOIN nota_remision_venta_detalle nrvd ON nrv.id_nota_remi_venta = nrvd.id_nota_remi_venta " +
                    "LEFT JOIN articulo a ON nrvd.id_articulo = a.id_articulo " +
                    "WHERE nrv.id_nota_remi_venta = ? " +
                    "GROUP BY nrv.id_nota_remi_venta, nrv.remi_vent_fecha_emision, nrv.not_remi_vent_descripcion, " +
                    "nrv.not_remi_vent_estado, nrv.id_cliente, nrv.id_usuario, nrv.id_fact_venta_cab, nrv.id_timbrado";

        clienteDAO = new ClienteDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);
        facturaVentaDAO = new FacturaVentaDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaRemisionVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nota = new NotaRemisionVenta(
                        rs.getLong("id_nota_remi_venta"),
                        rs.getDate("remi_vent_fecha_emision"),
                        rs.getString("not_remi_vent_descripcion"),
                        rs.getString("not_remi_vent_estado"),
                        clienteDAO.getCliente(rs.getLong("id_cliente")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                        timbradoDAO.getTimbrado(rs.getLong("id_timbrado")),
                        rs.getString("lista_articulos")
                    );
                }
            }
        }
        return nota;
    }

    public List<NotaRemisionVenta> listarNotasRemisionVenta() throws SQLException {
        List<NotaRemisionVenta> notas = new ArrayList<>();
        String sql = "SELECT nrv.id_nota_remi_venta, nrv.remi_vent_fecha_emision, nrv.not_remi_vent_descripcion, " +
                    "nrv.not_remi_vent_estado, nrv.id_cliente, nrv.id_usuario, nrv.id_fact_venta_cab, " +
                    "nrv.id_timbrado, STRING_AGG(a.art_descripcion, ', ') AS lista_articulos " +
                    "FROM nota_remision_venta_cabecera nrv " +
                    "LEFT JOIN nota_remision_venta_detalle nrvd ON nrv.id_nota_remi_venta = nrvd.id_nota_remi_venta " +
                    "LEFT JOIN articulo a ON nrvd.id_articulo = a.id_articulo " +
                    "GROUP BY nrv.id_nota_remi_venta, nrv.remi_vent_fecha_emision, nrv.not_remi_vent_descripcion, " +
                    "nrv.not_remi_vent_estado, nrv.id_cliente, nrv.id_usuario, nrv.id_fact_venta_cab, nrv.id_timbrado";

        clienteDAO = new ClienteDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);
        facturaVentaDAO = new FacturaVentaDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaRemisionVenta nota = new NotaRemisionVenta(
                    rs.getLong("id_nota_remi_venta"),
                    rs.getDate("remi_vent_fecha_emision"),
                    rs.getString("not_remi_vent_descripcion"),
                    rs.getString("not_remi_vent_estado"),
                    clienteDAO.getCliente(rs.getLong("id_cliente")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    facturaVentaDAO.getFacturaVenta(rs.getLong("id_fact_venta_cab")),
                    timbradoDAO.getTimbrado(rs.getLong("id_timbrado")),
                    rs.getString("lista_articulos")
                );
                notas.add(nota);
            }
        }
        return notas;
    }

    public Long insertarNotaRemisionVenta(NotaRemisionVenta nota) throws SQLException {
        if (nota == null) {
            LOGGER.log(Level.SEVERE, "Error: La nota de remisión de venta es nula");
            return null;
        }

        String sql = "INSERT INTO nota_remision_venta_cabecera (remi_vent_fecha_emision, not_remi_vent_descripcion, " +
                    "not_remi_vent_estado, id_cliente, id_usuario, id_fact_venta_cab, id_timbrado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, new java.sql.Date(nota.getFechaEmision().getTime()));
            stmt.setString(2, nota.getDescripcion());
            stmt.setString(3, nota.getEstado());
            stmt.setLong(4, nota.getCliente().getIdCliente());
            stmt.setLong(5, nota.getUsuario().getIdUsuario());
            stmt.setLong(6, nota.getFacturaVenta().getIdFacturaVenta());
            stmt.setLong(7, nota.getTimbrado().getIdTimbrado());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la nota de remisión de venta, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    nota.setIdNotaRemisionVenta(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la nota de remisión de venta.");
                }
            }
        }
    }

    public void actualizarNotaRemisionVenta(NotaRemisionVenta nota) throws SQLException {
        if (nota == null || nota.getIdNotaRemisionVenta() == null) {
            LOGGER.log(Level.WARNING, "Error: nota de remisión de venta es nula");
            return;
        }

        String sql = "UPDATE nota_remision_venta_cabecera SET remi_vent_fecha_emision = ?, " +
                    "not_remi_vent_descripcion = ?, not_remi_vent_estado = ?, id_cliente = ?, " +
                    "id_usuario = ?, id_fact_venta_cab = ?, id_timbrado = ? WHERE id_nota_remi_venta = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(nota.getFechaEmision().getTime()));
            stmt.setString(2, nota.getDescripcion());
            stmt.setString(3, nota.getEstado());
            stmt.setLong(4, nota.getCliente().getIdCliente());
            stmt.setLong(5, nota.getUsuario().getIdUsuario());
            stmt.setLong(6, nota.getFacturaVenta().getIdFacturaVenta());
            stmt.setLong(7, nota.getTimbrado().getIdTimbrado());
            stmt.setLong(8, nota.getIdNotaRemisionVenta());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaRemisionVenta(Long idNotaRemisionVenta) throws SQLException {
        if (idNotaRemisionVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaRemisionVenta es nulo");
            return;
        }

        String sql = "DELETE FROM nota_remision_venta_cabecera WHERE id_nota_remi_venta = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaRemisionVenta);
            stmt.executeUpdate();
        }
    }
}
