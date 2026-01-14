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
public class TimbradoDAO {

    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(TimbradoDAO.class.getName());

    public TimbradoDAO(Connection conn) {
        this.conn = conn;
    }

    public Timbrado getTimbrado(Long idTimbrado) throws SQLException {
        if (idTimbrado == null) {
            LOGGER.log(Level.WARNING, "Error: idTimbrado es nulo");
            return null;
        }
        Timbrado timbrado = null;
        String sql = "SELECT id_timbrado, tim_numero, tim_fecha_autorizacion, tim_fecha_vencimineto, " +
                    "tim_estado, id_tipo_comprob FROM timbrado WHERE id_timbrado = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idTimbrado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    timbrado = new Timbrado(
                        rs.getLong("id_timbrado"),
                        rs.getInt("tim_numero"),
                        rs.getDate("tim_fecha_autorizacion"),
                        rs.getDate("tim_fecha_vencimineto"),
                        rs.getString("tim_estado"),
                        rs.getLong("id_tipo_comprob")
                    );
                }
            }
        }
        return timbrado;
    }

    public List<Timbrado> listarTimbrados() throws SQLException {
        List<Timbrado> timbrados = new ArrayList<>();
        String sql = "SELECT id_timbrado, tim_numero, tim_fecha_autorizacion, tim_fecha_vencimineto, " +
                    "tim_estado, id_tipo_comprob FROM timbrado";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timbrado timbrado = new Timbrado(
                    rs.getLong("id_timbrado"),
                    rs.getInt("tim_numero"),
                    rs.getDate("tim_fecha_autorizacion"),
                    rs.getDate("tim_fecha_vencimineto"),
                    rs.getString("tim_estado"),
                    rs.getLong("id_tipo_comprob")
                );
                timbrados.add(timbrado);
            }
        }
        return timbrados;
    }

    public List<Timbrado> listarTimbradosActivos() throws SQLException {
        List<Timbrado> timbrados = new ArrayList<>();
        String sql = "SELECT id_timbrado, tim_numero, tim_fecha_autorizacion, tim_fecha_vencimineto, " +
                    "tim_estado, id_tipo_comprob FROM timbrado WHERE tim_estado = 'ACTIVO'";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timbrado timbrado = new Timbrado(
                    rs.getLong("id_timbrado"),
                    rs.getInt("tim_numero"),
                    rs.getDate("tim_fecha_autorizacion"),
                    rs.getDate("tim_fecha_vencimineto"),
                    rs.getString("tim_estado"),
                    rs.getLong("id_tipo_comprob")
                );
                timbrados.add(timbrado);
            }
        }
        return timbrados;
    }

    public Long insertarTimbrado(Timbrado timbrado) throws SQLException {
        if (timbrado == null) {
            LOGGER.log(Level.SEVERE, "Error: El timbrado es nulo");
            return null;
        }

        String sql = "INSERT INTO timbrado (tim_numero, tim_fecha_autorizacion, tim_fecha_vencimineto, " +
                    "tim_estado, id_tipo_comprob) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, timbrado.getNumero());
            stmt.setDate(2, new java.sql.Date(timbrado.getFechaAutorizacion().getTime()));
            stmt.setDate(3, new java.sql.Date(timbrado.getFechaVencimiento().getTime()));
            stmt.setString(4, timbrado.getEstado());
            stmt.setLong(5, timbrado.getIdTipoComprobante());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el timbrado, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    timbrado.setIdTimbrado(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el timbrado.");
                }
            }
        }
    }

    public void actualizarTimbrado(Timbrado timbrado) throws SQLException {
        if (timbrado == null || timbrado.getIdTimbrado() == null) {
            LOGGER.log(Level.WARNING, "Error: timbrado es nulo");
            return;
        }

        String sql = "UPDATE timbrado SET tim_numero = ?, tim_fecha_autorizacion = ?, " +
                    "tim_fecha_vencimineto = ?, tim_estado = ?, id_tipo_comprob = ? " +
                    "WHERE id_timbrado = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, timbrado.getNumero());
            stmt.setDate(2, new java.sql.Date(timbrado.getFechaAutorizacion().getTime()));
            stmt.setDate(3, new java.sql.Date(timbrado.getFechaVencimiento().getTime()));
            stmt.setString(4, timbrado.getEstado());
            stmt.setLong(5, timbrado.getIdTipoComprobante());
            stmt.setLong(6, timbrado.getIdTimbrado());

            stmt.executeUpdate();
        }
    }

    public void eliminarTimbrado(Long idTimbrado) throws SQLException {
        if (idTimbrado == null) {
            LOGGER.log(Level.WARNING, "Error: idTimbrado es nulo");
            return;
        }

        String sql = "DELETE FROM timbrado WHERE id_timbrado = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idTimbrado);
            stmt.executeUpdate();
        }
    }
}
