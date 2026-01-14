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
public class CajaDAO {

    private Connection conn;
    private SucursalDAO sucursalDAO;
    private static final Logger LOGGER = Logger.getLogger(CajaDAO.class.getName());

    public CajaDAO(Connection conn) {
        this.conn = conn;
    }

    public Caja getCaja(Long idCaja) throws SQLException {
        if (idCaja == null) {
            LOGGER.log(Level.WARNING, "Error: idCaja es nulo");
            return null;
        }
        Caja caja = null;
        String sql = "SELECT id_caja, caja_descripcion, caja_nro_expedicion, caja_estado, id_sucursal " +
                    "FROM caja WHERE id_caja = ?";

        sucursalDAO = new SucursalDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    caja = new Caja(
                        rs.getLong("id_caja"),
                        rs.getString("caja_descripcion"),
                        rs.getInt("caja_nro_expedicion"),
                        rs.getString("caja_estado"),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal"))
                    );
                }
            }
        }
        return caja;
    }

    public List<Caja> listarCajas() throws SQLException {
        List<Caja> cajas = new ArrayList<>();
        String sql = "SELECT id_caja, caja_descripcion, caja_nro_expedicion, caja_estado, id_sucursal FROM caja";

        sucursalDAO = new SucursalDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Caja caja = new Caja(
                    rs.getLong("id_caja"),
                    rs.getString("caja_descripcion"),
                    rs.getInt("caja_nro_expedicion"),
                    rs.getString("caja_estado"),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal"))
                );
                cajas.add(caja);
            }
        }
        return cajas;
    }

    public List<Caja> listarCajasActivas() throws SQLException {
        List<Caja> cajas = new ArrayList<>();
        String sql = "SELECT id_caja, caja_descripcion, caja_nro_expedicion, caja_estado, id_sucursal " +
                    "FROM caja WHERE caja_estado = 'ACTIVA'";

        sucursalDAO = new SucursalDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Caja caja = new Caja(
                    rs.getLong("id_caja"),
                    rs.getString("caja_descripcion"),
                    rs.getInt("caja_nro_expedicion"),
                    rs.getString("caja_estado"),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal"))
                );
                cajas.add(caja);
            }
        }
        return cajas;
    }

    public Long insertarCaja(Caja caja) throws SQLException {
        if (caja == null) {
            LOGGER.log(Level.SEVERE, "Error: La caja es nula");
            return null;
        }

        String sql = "INSERT INTO caja (caja_descripcion, caja_nro_expedicion, caja_estado, id_sucursal) " +
                    "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, caja.getDescripcion());
            stmt.setInt(2, caja.getNumeroExpedicion());
            stmt.setString(3, caja.getEstado());
            stmt.setLong(4, caja.getSucursal().getIdSucursal());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la caja, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    caja.setIdCaja(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la caja.");
                }
            }
        }
    }

    public void actualizarCaja(Caja caja) throws SQLException {
        if (caja == null || caja.getIdCaja() == null) {
            LOGGER.log(Level.WARNING, "Error: caja es nula");
            return;
        }

        String sql = "UPDATE caja SET caja_descripcion = ?, caja_nro_expedicion = ?, " +
                    "caja_estado = ?, id_sucursal = ? WHERE id_caja = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, caja.getDescripcion());
            stmt.setInt(2, caja.getNumeroExpedicion());
            stmt.setString(3, caja.getEstado());
            stmt.setLong(4, caja.getSucursal().getIdSucursal());
            stmt.setLong(5, caja.getIdCaja());

            stmt.executeUpdate();
        }
    }

    public void eliminarCaja(Long idCaja) throws SQLException {
        if (idCaja == null) {
            LOGGER.log(Level.WARNING, "Error: idCaja es nulo");
            return;
        }

        String sql = "DELETE FROM caja WHERE id_caja = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idCaja);
            stmt.executeUpdate();
        }
    }
}
