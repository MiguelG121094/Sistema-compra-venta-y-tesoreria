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
public class NotaDebitoVentaDAO {

    private Connection conn;
    private UsuarioDAO usuarioDAO;
    private TimbradoDAO timbradoDAO;
    private static final Logger LOGGER = Logger.getLogger(NotaDebitoVentaDAO.class.getName());

    public NotaDebitoVentaDAO(Connection conn) {
        this.conn = conn;
    }

    public NotaDebitoVenta getNotaDebitoVenta(Long idNotaDebitoVenta) throws SQLException {
        if (idNotaDebitoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaDebitoVenta es nulo");
            return null;
        }
        NotaDebitoVenta nota = null;
        String sql = "SELECT id_nota_debi_vent_cab, nota_debi_venta_numero, nota_debi_vent_fecha_emision, " +
                    "nota_debi_vent_motivo, nota_debi_vent_observacion, nota_debi_vent_estado, " +
                    "id_usuario, id_timbrado FROM nota_debito_venta_cabecera WHERE id_nota_debi_vent_cab = ?";

        usuarioDAO = new UsuarioDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebitoVenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nota = new NotaDebitoVenta(
                        rs.getLong("id_nota_debi_vent_cab"),
                        rs.getInt("nota_debi_venta_numero"),
                        rs.getDate("nota_debi_vent_fecha_emision"),
                        rs.getString("nota_debi_vent_motivo"),
                        rs.getString("nota_debi_vent_observacion"),
                        rs.getString("nota_debi_vent_estado"),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        timbradoDAO.getTimbrado(rs.getLong("id_timbrado"))
                    );
                }
            }
        }
        return nota;
    }

    public List<NotaDebitoVenta> listarNotasDebitoVenta() throws SQLException {
        List<NotaDebitoVenta> notas = new ArrayList<>();
        String sql = "SELECT id_nota_debi_vent_cab, nota_debi_venta_numero, nota_debi_vent_fecha_emision, " +
                    "nota_debi_vent_motivo, nota_debi_vent_observacion, nota_debi_vent_estado, " +
                    "id_usuario, id_timbrado FROM nota_debito_venta_cabecera";

        usuarioDAO = new UsuarioDAO(conn);
        timbradoDAO = new TimbradoDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NotaDebitoVenta nota = new NotaDebitoVenta(
                    rs.getLong("id_nota_debi_vent_cab"),
                    rs.getInt("nota_debi_venta_numero"),
                    rs.getDate("nota_debi_vent_fecha_emision"),
                    rs.getString("nota_debi_vent_motivo"),
                    rs.getString("nota_debi_vent_observacion"),
                    rs.getString("nota_debi_vent_estado"),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    timbradoDAO.getTimbrado(rs.getLong("id_timbrado"))
                );
                notas.add(nota);
            }
        }
        return notas;
    }

    public Long insertarNotaDebitoVenta(NotaDebitoVenta nota) throws SQLException {
        if (nota == null) {
            LOGGER.log(Level.SEVERE, "Error: La nota de débito de venta es nula");
            return null;
        }

        String sql = "INSERT INTO nota_debito_venta_cabecera (nota_debi_venta_numero, nota_debi_vent_fecha_emision, " +
                    "nota_debi_vent_motivo, nota_debi_vent_observacion, nota_debi_vent_estado, " +
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
                throw new SQLException("No se insertó la nota de débito de venta, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    nota.setIdNotaDebitoVenta(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la nota de débito de venta.");
                }
            }
        }
    }

    public void actualizarNotaDebitoVenta(NotaDebitoVenta nota) throws SQLException {
        if (nota == null || nota.getIdNotaDebitoVenta() == null) {
            LOGGER.log(Level.WARNING, "Error: nota de débito de venta es nula");
            return;
        }

        String sql = "UPDATE nota_debito_venta_cabecera SET nota_debi_venta_numero = ?, " +
                    "nota_debi_vent_fecha_emision = ?, nota_debi_vent_motivo = ?, " +
                    "nota_debi_vent_observacion = ?, nota_debi_vent_estado = ?, " +
                    "id_usuario = ?, id_timbrado = ? WHERE id_nota_debi_vent_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nota.getNumero());
            stmt.setDate(2, new java.sql.Date(nota.getFechaEmision().getTime()));
            stmt.setString(3, nota.getMotivo());
            stmt.setString(4, nota.getObservacion());
            stmt.setString(5, nota.getEstado());
            stmt.setLong(6, nota.getUsuario().getIdUsuario());
            stmt.setLong(7, nota.getTimbrado().getIdTimbrado());
            stmt.setLong(8, nota.getIdNotaDebitoVenta());

            stmt.executeUpdate();
        }
    }

    public void eliminarNotaDebitoVenta(Long idNotaDebitoVenta) throws SQLException {
        if (idNotaDebitoVenta == null) {
            LOGGER.log(Level.WARNING, "Error: idNotaDebitoVenta es nulo");
            return;
        }

        String sql = "DELETE FROM nota_debito_venta_cabecera WHERE id_nota_debi_vent_cab = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idNotaDebitoVenta);
            stmt.executeUpdate();
        }
    }
}
