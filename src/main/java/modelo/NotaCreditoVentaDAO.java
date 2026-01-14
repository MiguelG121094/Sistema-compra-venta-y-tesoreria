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
public class NotaCreditoVentaDAO {

    private Connection conn;
    private UsuarioDAO usuarioDAO;
    private TimbradoDAO timbradoDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaCreditoVentaDAO.class.getName());

    public NotaCreditoVentaDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaCreditoVenta getNotaCreditoVenta(Long idNotaCreditoVenta) throws SQLException {
        if (idNotaCreditoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaCreditoVenta es nulo");
            return null;
        }
        NotaCreditoVenta nota = null;
        String sql = "SELECT id_nota_ced_venta_cab, nota_cred_venta_numero, nota_cred_vent_fecha_emision, " +
                    "nota_cred_vent_motivo, nota_cred_vent_observacion, nota_cred_vent_estado, " +
                    "id_usuario, id_timbrado FROM nota_credito_venta_cabecera WHERE id_nota_ced_venta_cab = ?";

        usuarioDAO = new UsuarioDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCreditoVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nota = new NotaCreditoVenta(
                        rs.getLong("id_nota_ced_venta_cab"),
                        rs.getInt("nota_cred_venta_numero"),
                        rs.getDate("nota_cred_vent_fecha_emision"),
                        rs.getString("nota_cred_vent_motivo"),
                        rs.getString("nota_cred_vent_observacion"),
                        rs.getString("nota_cred_vent_estado"),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        timbradoDAO.getTimbrado(rs.getLong("id_timbrado"))
                    );
                }
            }
        }
        return nota;
    }

    public List<NotaCreditoVenta> listarNotasCreditoVenta() throws SQLException {
        List<NotaCreditoVenta> notas = new ArrayList<>();
        String sql = "SELECT id_nota_ced_venta_cab, nota_cred_venta_numero, nota_cred_vent_fecha_emision, " +
                    "nota_cred_vent_motivo, nota_cred_vent_observacion, nota_cred_vent_estado, " +
                    "id_usuario, id_timbrado FROM nota_credito_venta_cabecera";

        usuarioDAO = new UsuarioDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaCreditoVenta nota = new NotaCreditoVenta(
                    rs.getLong("id_nota_ced_venta_cab"),
                    rs.getInt("nota_cred_venta_numero"),
                    rs.getDate("nota_cred_vent_fecha_emision"),
                    rs.getString("nota_cred_vent_motivo"),
                    rs.getString("nota_cred_vent_observacion"),
                    rs.getString("nota_cred_vent_estado"),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    timbradoDAO.getTimbrado(rs.getLong("id_timbrado"))
                );
                notas.add(nota);
            }
        }
        return notas;
    }

    public Long insertarNotaCreditoVenta(NotaCreditoVenta nota) throws SQLException {
        if (nota == null) {
            LOGGER.log(Level.SEVERE, "Error: La nota de crédito de venta es nula");
            return null;
        }

        String sql = "INSERT INTO nota_credito_venta_cabecera (nota_cred_venta_numero, nota_cred_vent_fecha_emision, " +
                    "nota_cred_vent_motivo, nota_cred_vent_observacion, nota_cred_vent_estado, " +
                    "id_usuario, id_timbrado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, nota.getNumero());
            stmt.setDate(2, new java.sql.Date(nota.getFechaEmision().getTime()));
            stmt.setString(3, nota.getMotivo());
            stmt.setString(4, nota.getObservacion());
            stmt.setString(5, nota.getEstado());
            stmt.setLong(6, nota.getUsuario().getIdUsuario());
            stmt.setLong(7, nota.getTimbrado().getIdTimbrado());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la nota de crédito de venta, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    nota.setIdNotaCreditoVenta(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la nota de crédito de venta.");
                }
            }
        }
    }

    public void actualizarNotaCreditoVenta(NotaCreditoVenta nota) throws SQLException {
        if (nota == null || nota.getIdNotaCreditoVenta() == null) {
            LOGGER.log(Level.WARNING, "Error: nota de crédito de venta es nula");
            return;
        }

        String sql = "UPDATE nota_credito_venta_cabecera SET nota_cred_venta_numero = ?, " +
                    "nota_cred_vent_fecha_emision = ?, nota_cred_vent_motivo = ?, " +
                    "nota_cred_vent_observacion = ?, nota_cred_vent_estado = ?, " +
                    "id_usuario = ?, id_timbrado = ? WHERE id_nota_ced_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nota.getNumero());
            stmt.setDate(2, new java.sql.Date(nota.getFechaEmision().getTime()));
            stmt.setString(3, nota.getMotivo());
            stmt.setString(4, nota.getObservacion());
            stmt.setString(5, nota.getEstado());
            stmt.setLong(6, nota.getUsuario().getIdUsuario());
            stmt.setLong(7, nota.getTimbrado().getIdTimbrado());
            stmt.setLong(8, nota.getIdNotaCreditoVenta());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaCreditoVenta(Long idNotaCreditoVenta) throws SQLException {
        if (idNotaCreditoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaCreditoVenta es nulo");
            return;
        }

        String sql = "DELETE FROM nota_credito_venta_cabecera WHERE id_nota_ced_venta_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaCreditoVenta);
            stmt.executeUpdate();
        }
    }
}
