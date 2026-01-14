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
public class NotaDebitoCompraDAO {

    private Connection conn;
    private UsuarioDAO usuarioDAO;
    private ProveedorDAO proveedorDAO;
    private FacturaCompraDAO facturaCompraDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaDebitoCompraDAO.class.getName());

    public NotaDebitoCompraDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaDebitoCompra getNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        if (idNotaDebitoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: el parámetro idNotaDebitoCompra es nulo");
            return null;
        }
        NotaDebitoCompra notaDebito = null;
        String sql = "SELECT id_nota_debi_comp_cab, nota_debi_comp_numero, nota_debi_comp_timbrado, " +
                    "nota_debi_comp_fecha_venci_timb, nota_debi_comp_fecha_emision, nota_debi_comp_fecha_carga, " +
                    "nota_debi_comp_estado, nota_debi_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_debito_motivo FROM nota_debito_compra_cabecera WHERE id_nota_debi_comp_cab = ?";

        usuarioDAO = new UsuarioDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebitoCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    notaDebito = new NotaDebitoCompra(
                        rs.getLong("id_nota_debi_comp_cab"),
                        rs.getInt("nota_debi_comp_numero"),
                        rs.getInt("nota_debi_comp_timbrado"),
                        rs.getDate("nota_debi_comp_fecha_venci_timb"),
                        rs.getDate("nota_debi_comp_fecha_emision"),
                        rs.getDate("nota_debi_comp_fecha_carga"),
                        rs.getString("nota_debi_comp_estado"),
                        rs.getString("nota_debi_comp_observacion"),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                        facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                        rs.getString("nota_debito_motivo")
                    );
                }
            }
        }
        return notaDebito;
    }

    public List<NotaDebitoCompra> listarNotasDebitoCompra() throws SQLException {
        List<NotaDebitoCompra> notas = new ArrayList<>();
        String sql = "SELECT id_nota_debi_comp_cab, nota_debi_comp_numero, nota_debi_comp_timbrado, " +
                    "nota_debi_comp_fecha_venci_timb, nota_debi_comp_fecha_emision, nota_debi_comp_fecha_carga, " +
                    "nota_debi_comp_estado, nota_debi_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_debito_motivo FROM nota_debito_compra_cabecera";

        usuarioDAO = new UsuarioDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);
        facturaCompraDAO = new FacturaCompraDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaDebitoCompra notaDebito = new NotaDebitoCompra(
                    rs.getLong("id_nota_debi_comp_cab"),
                    rs.getInt("nota_debi_comp_numero"),
                    rs.getInt("nota_debi_comp_timbrado"),
                    rs.getDate("nota_debi_comp_fecha_venci_timb"),
                    rs.getDate("nota_debi_comp_fecha_emision"),
                    rs.getDate("nota_debi_comp_fecha_carga"),
                    rs.getString("nota_debi_comp_estado"),
                    rs.getString("nota_debi_comp_observacion"),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor")),
                    facturaCompraDAO.getFacturaCompra(rs.getLong("id_fact_comp_cab")),
                    rs.getString("nota_debito_motivo")
                );
                notas.add(notaDebito);
            }
        }
        return notas;
    }

    public Long insertarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        if (notaDebito == null) {
            LOGGER.log(Level.SEVERE, "Error: La nota de débito de compra es nula");
            return null;
        }

        String sql = "INSERT INTO nota_debito_compra_cabecera (nota_debi_comp_numero, nota_debi_comp_timbrado, " +
                    "nota_debi_comp_fecha_venci_timb, nota_debi_comp_fecha_emision, nota_debi_comp_fecha_carga, " +
                    "nota_debi_comp_estado, nota_debi_comp_observacion, id_usuario, id_proveedor, " +
                    "id_fact_comp_cab, nota_debito_motivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notaDebito.getNumero());
            stmt.setInt(2, notaDebito.getTimbrado());
            stmt.setDate(3, new java.sql.Date(notaDebito.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(notaDebito.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(notaDebito.getFechaCarga().getTime()));
            stmt.setString(6, notaDebito.getEstado());
            stmt.setString(7, notaDebito.getObservacion());
            stmt.setLong(8, notaDebito.getUsuario().getIdUsuario());
            stmt.setLong(9, notaDebito.getProveedor().getIdProveedor());
            stmt.setLong(10, notaDebito.getFacturaCompra().getIdFacturaCompra());
            stmt.setString(11, notaDebito.getMotivo());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la nota de débito, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    notaDebito.setIdNotaDebitoCompra(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la nota de débito.");
                }
            }
        }
    }

    public void actualizarNotaDebitoCompra(NotaDebitoCompra notaDebito) throws SQLException {
        if (notaDebito == null || notaDebito.getIdNotaDebitoCompra() == null) {
            LOGGER.log(Level.WARNING, "Error: nota de débito de compra es nula");
            return;
        }

        String sql = "UPDATE nota_debito_compra_cabecera SET nota_debi_comp_numero = ?, nota_debi_comp_timbrado = ?, " +
                    "nota_debi_comp_fecha_venci_timb = ?, nota_debi_comp_fecha_emision = ?, nota_debi_comp_fecha_carga = ?, " +
                    "nota_debi_comp_estado = ?, nota_debi_comp_observacion = ?, id_usuario = ?, id_proveedor = ?, " +
                    "id_fact_comp_cab = ?, nota_debito_motivo = ? WHERE id_nota_debi_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaDebito.getNumero());
            stmt.setInt(2, notaDebito.getTimbrado());
            stmt.setDate(3, new java.sql.Date(notaDebito.getFechaVenciTimbrado().getTime()));
            stmt.setDate(4, new java.sql.Date(notaDebito.getFechaEmision().getTime()));
            stmt.setDate(5, new java.sql.Date(notaDebito.getFechaCarga().getTime()));
            stmt.setString(6, notaDebito.getEstado());
            stmt.setString(7, notaDebito.getObservacion());
            stmt.setLong(8, notaDebito.getUsuario().getIdUsuario());
            stmt.setLong(9, notaDebito.getProveedor().getIdProveedor());
            stmt.setLong(10, notaDebito.getFacturaCompra().getIdFacturaCompra());
            stmt.setString(11, notaDebito.getMotivo());
            stmt.setLong(12, notaDebito.getIdNotaDebitoCompra());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaDebitoCompra(Long idNotaDebitoCompra) throws SQLException {
        if (idNotaDebitoCompra == null) {
            LOGGER.log(Level.WARNING, "Error: id de la nota de débito es nulo");
            return;
        }

        String sql = "DELETE FROM nota_debito_compra_cabecera WHERE id_nota_debi_comp_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebitoCompra);
            stmt.executeUpdate();
        }
    }
}
