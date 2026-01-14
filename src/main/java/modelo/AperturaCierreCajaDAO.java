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
public class AperturaCierreCajaDAO {

    private Connection conn;
    private SucursalDAO sucursalDAO;
    private UsuarioDAO usuarioDAO;
    private static final Logger LOGGER = Logger.getLogger(AperturaCierreCajaDAO.class.getName());

    public AperturaCierreCajaDAO(Connection conn) {
        this.conn = conn;
    }

    public AperturaCierreCaja getAperturaCierreCaja(Long idAperturaCierreCaja) throws SQLException {
        if (idAperturaCierreCaja == null) {
            LOGGER.log(Level.WARNING, "Error: idAperturaCierreCaja es nulo");
            return null;
        }
        AperturaCierreCaja apertura = null;
        String sql = "SELECT id_aper_cier_caja, aper_cier_fecha_apertura, aper_cier_monto_inicial, " +
                    "aper_cier_fecha_cierre, aper_cier_efectivo, aper_cier_cheque, aper_cier_tarjeta, " +
                    "aper_cier_monto_cierre, aper_cier_estado, id_caja, id_sucursal, id_usuario " +
                    "FROM apertura_cierre_caja WHERE id_aper_cier_caja = ?";

        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idAperturaCierreCaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    apertura = new AperturaCierreCaja(
                        rs.getLong("id_aper_cier_caja"),
                        rs.getTimestamp("aper_cier_fecha_apertura"),
                        rs.getLong("aper_cier_monto_inicial"),
                        rs.getTimestamp("aper_cier_fecha_cierre"),
                        rs.getLong("aper_cier_efectivo"),
                        rs.getLong("aper_cier_cheque"),
                        rs.getLong("aper_cier_tarjeta"),
                        rs.getLong("aper_cier_monto_cierre"),
                        rs.getString("aper_cier_estado"),
                        rs.getLong("id_caja"),
                        sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                        usuarioDAO.getUsuario(rs.getLong("id_usuario"))
                    );
                }
            }
        }
        return apertura;
    }

    public List<AperturaCierreCaja> listarAperturasCierreCaja() throws SQLException {
        List<AperturaCierreCaja> aperturas = new ArrayList<>();
        String sql = "SELECT id_aper_cier_caja, aper_cier_fecha_apertura, aper_cier_monto_inicial, " +
                    "aper_cier_fecha_cierre, aper_cier_efectivo, aper_cier_cheque, aper_cier_tarjeta, " +
                    "aper_cier_monto_cierre, aper_cier_estado, id_caja, id_sucursal, id_usuario " +
                    "FROM apertura_cierre_caja";

        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AperturaCierreCaja apertura = new AperturaCierreCaja(
                    rs.getLong("id_aper_cier_caja"),
                    rs.getTimestamp("aper_cier_fecha_apertura"),
                    rs.getLong("aper_cier_monto_inicial"),
                    rs.getTimestamp("aper_cier_fecha_cierre"),
                    rs.getLong("aper_cier_efectivo"),
                    rs.getLong("aper_cier_cheque"),
                    rs.getLong("aper_cier_tarjeta"),
                    rs.getLong("aper_cier_monto_cierre"),
                    rs.getString("aper_cier_estado"),
                    rs.getLong("id_caja"),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario"))
                );
                aperturas.add(apertura);
            }
        }
        return aperturas;
    }

    public List<AperturaCierreCaja> listarAperturasAbiertas() throws SQLException {
        List<AperturaCierreCaja> aperturas = new ArrayList<>();
        String sql = "SELECT id_aper_cier_caja, aper_cier_fecha_apertura, aper_cier_monto_inicial, " +
                    "aper_cier_fecha_cierre, aper_cier_efectivo, aper_cier_cheque, aper_cier_tarjeta, " +
                    "aper_cier_monto_cierre, aper_cier_estado, id_caja, id_sucursal, id_usuario " +
                    "FROM apertura_cierre_caja WHERE aper_cier_estado = 'ABIERTA'";

        sucursalDAO = new SucursalDAO(conn);
        usuarioDAO = new UsuarioDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AperturaCierreCaja apertura = new AperturaCierreCaja(
                    rs.getLong("id_aper_cier_caja"),
                    rs.getTimestamp("aper_cier_fecha_apertura"),
                    rs.getLong("aper_cier_monto_inicial"),
                    rs.getTimestamp("aper_cier_fecha_cierre"),
                    rs.getLong("aper_cier_efectivo"),
                    rs.getLong("aper_cier_cheque"),
                    rs.getLong("aper_cier_tarjeta"),
                    rs.getLong("aper_cier_monto_cierre"),
                    rs.getString("aper_cier_estado"),
                    rs.getLong("id_caja"),
                    sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
                    usuarioDAO.getUsuario(rs.getLong("id_usuario"))
                );
                aperturas.add(apertura);
            }
        }
        return aperturas;
    }

    public Long insertarAperturaCierreCaja(AperturaCierreCaja apertura) throws SQLException {
        if (apertura == null) {
            LOGGER.log(Level.SEVERE, "Error: La apertura/cierre de caja es nula");
            return null;
        }

        String sql = "INSERT INTO apertura_cierre_caja (aper_cier_fecha_apertura, aper_cier_monto_inicial, " +
                    "aper_cier_fecha_cierre, aper_cier_efectivo, aper_cier_cheque, aper_cier_tarjeta, " +
                    "aper_cier_monto_cierre, aper_cier_estado, id_caja, id_sucursal, id_usuario) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, apertura.getFechaApertura());
            stmt.setLong(2, apertura.getMontoInicial());
            stmt.setTimestamp(3, apertura.getFechaCierre());
            stmt.setLong(4, apertura.getEfectivo());
            stmt.setLong(5, apertura.getCheque());
            stmt.setLong(6, apertura.getTarjeta());
            stmt.setLong(7, apertura.getMontoCierre());
            stmt.setString(8, apertura.getEstado());
            stmt.setLong(9, apertura.getIdCaja());
            stmt.setLong(10, apertura.getSucursal().getIdSucursal());
            stmt.setLong(11, apertura.getUsuario().getIdUsuario());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se insertó la apertura/cierre de caja, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGenerado = generatedKeys.getLong(1);
                    apertura.setIdAperturaCierreCaja(idGenerado);
                    return idGenerado;
                } else {
                    throw new SQLException("Error: No se generó ningún ID para la apertura/cierre de caja.");
                }
            }
        }
    }

    public void actualizarAperturaCierreCaja(AperturaCierreCaja apertura) throws SQLException {
        if (apertura == null || apertura.getIdAperturaCierreCaja() == null) {
            LOGGER.log(Level.WARNING, "Error: apertura/cierre de caja es nula");
            return;
        }

        String sql = "UPDATE apertura_cierre_caja SET aper_cier_fecha_apertura = ?, aper_cier_monto_inicial = ?, " +
                    "aper_cier_fecha_cierre = ?, aper_cier_efectivo = ?, aper_cier_cheque = ?, " +
                    "aper_cier_tarjeta = ?, aper_cier_monto_cierre = ?, aper_cier_estado = ?, " +
                    "id_caja = ?, id_sucursal = ?, id_usuario = ? WHERE id_aper_cier_caja = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, apertura.getFechaApertura());
            stmt.setLong(2, apertura.getMontoInicial());
            stmt.setTimestamp(3, apertura.getFechaCierre());
            stmt.setLong(4, apertura.getEfectivo());
            stmt.setLong(5, apertura.getCheque());
            stmt.setLong(6, apertura.getTarjeta());
            stmt.setLong(7, apertura.getMontoCierre());
            stmt.setString(8, apertura.getEstado());
            stmt.setLong(9, apertura.getIdCaja());
            stmt.setLong(10, apertura.getSucursal().getIdSucursal());
            stmt.setLong(11, apertura.getUsuario().getIdUsuario());
            stmt.setLong(12, apertura.getIdAperturaCierreCaja());

            stmt.executeUpdate();
        }
    }

    public void eliminarAperturaCierreCaja(Long idAperturaCierreCaja) throws SQLException {
        if (idAperturaCierreCaja == null) {
            LOGGER.log(Level.WARNING, "Error: idAperturaCierreCaja es nulo");
            return;
        }

        String sql = "DELETE FROM apertura_cierre_caja WHERE id_aper_cier_caja = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idAperturaCierreCaja);
            stmt.executeUpdate();
        }
    }
}
