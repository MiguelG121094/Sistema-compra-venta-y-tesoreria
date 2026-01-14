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
public class CobroDAO {

    private Connection conn;
    private AperturaCierreCajaDAO aperturaCierreCajaDAO;
    private UsuarioDAO usuarioDAO;
    private static final Logger LOGGER = Logger.getLogger(CobroDAO.class.getName());

    public CobroDAO(Connection conn) {
        this.conn = conn;
    }

    public Cobro getCobro(Long idCobro) throws SQLException {
        if (idCobro == null) {
            LOGGER.log(Level.WARNING, "Error: idCobro es nulo");
            return null;
        }
        Cobro cobro = null;
        String sql = "SELECT id_cobro, cob_fecha, cob_estado, id_aper_cier_caja, id_usuario, cob_det_monto " +
                    "FROM cobro WHERE id_cobro = ?";

        aperturaCierreCajaDAO = new AperturaCierreCajaDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCobro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cobro = new Cobro(
                        rs.getLong("id_cobro"),
                        rs.getDate("cob_fecha"),
                        rs.getString("cob_estado"),
                        aperturaCierreCajaDAO.getAperturaCierreCaja(rs.getLong("id_aper_cier_caja")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                        rs.getLong("cob_det_monto")
                    );
                }
            }
        }
        return cobro;
    }

    public List<Cobro> listarCobros() throws SQLException {
        List<Cobro> cobros = new ArrayList<>();
        String sql = "SELECT id_cobro, cob_fecha, cob_estado, id_aper_cier_caja, id_usuario, cob_det_monto FROM cobro";

        aperturaCierreCajaDAO = new AperturaCierreCajaDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cobro cobro = new Cobro(
                    rs.getLong("id_cobro"),
                    rs.getDate("cob_fecha"),
                    rs.getString("cob_estado"),
                    aperturaCierreCajaDAO.getAperturaCierreCaja(rs.getLong("id_aper_cier_caja")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario")),
                    rs.getLong("cob_det_monto")
                );
                cobros.add(cobro);
            }
        }
        return cobros;
    }

    public Long insertarCobro(Cobro cobro) throws SQLException {
        if (cobro == null) {
            LOGGER.log(Level.SEVERE, "Error: El cobro es nulo");
            return null;
        }

        String sql = "INSERT INTO cobro (cob_fecha, cob_estado, id_aper_cier_caja, id_usuario, cob_det_monto) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, new java.sql.Date(cobro.getFecha().getTime()));
            stmt.setString(2, cobro.getEstado());
            stmt.setLong(3, cobro.getAperturaCierreCaja().getIdAperturaCierreCaja());
            stmt.setLong(4, cobro.getUsuario().getIdUsuario());
            stmt.setLong(5, cobro.getMonto());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó el cobro, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    cobro.setIdCobro(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para el cobro.");
                }
            }
        }
    }

    public void actualizarCobro(Cobro cobro) throws SQLException {
        if (cobro == null || cobro.getIdCobro() == null) {
            LOGGER.log(Level.WARNING, "Error: cobro es nulo");
            return;
        }

        String sql = "UPDATE cobro SET cob_fecha = ?, cob_estado = ?, id_aper_cier_caja = ?, " +
                    "id_usuario = ?, cob_det_monto = ? WHERE id_cobro = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(cobro.getFecha().getTime()));
            stmt.setString(2, cobro.getEstado());
            stmt.setLong(3, cobro.getAperturaCierreCaja().getIdAperturaCierreCaja());
            stmt.setLong(4, cobro.getUsuario().getIdUsuario());
            stmt.setLong(5, cobro.getMonto());
            stmt.setLong(6, cobro.getIdCobro());

            stmt.executeUpdate();
        }
    }

    public void eliminarCobro(Long idCobro) throws SQLException {
        if (idCobro == null) {
            LOGGER.log(Level.WARNING, "Error: idCobro es nulo");
            return;
        }

        String sql = "DELETE FROM cobro WHERE id_cobro = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCobro);
            stmt.executeUpdate();
        }
    }
}
