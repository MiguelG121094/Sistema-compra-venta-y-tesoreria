package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la rendicion de fondo fijo, cabecera y detalle en el mismo DAO (igual que
 * ProvisionCuentaPagarDAO). La rendicion agrupa las facturas de fondo fijo ya cargadas, que llegan
 * como cuentas a pagar; de ahi que el detalle referencie la clave compuesta de cuenta_pagar.
 * Requerimiento 3.6, §E del MODULO_TESORERIA_PLAN.md.
 *
 * @author Miguel
 */
public class FondoFijoRendicionDAO {

    public static final String ESTADO_GENERADA = "Generada";
    public static final String ESTADO_PROVISIONADA = "Provisionada";
    public static final String ESTADO_ANULADO = "Anulado";

    private static final String COLUMNAS =
            "id_fondofijo_rendicion, id_fondo_fijo, fecha_emision_rendicion, "
            + "ff_rendicion_fecha_reposicion, nro_rendicion, ff_rendicion_estado";

    private Connection conn;

    public FondoFijoRendicionDAO(Connection conn) {
        this.conn = conn;
    }

    private FondoFijoRendicion mapear(ResultSet rs) throws SQLException {
        FondoFijoRendicion rendicion = new FondoFijoRendicion();
        rendicion.setIdFondoFijoRendicion(rs.getLong("id_fondofijo_rendicion"));
        rendicion.setFondoFijo(new FondoFijoDAO(conn).getFondoFijo(rs.getLong("id_fondo_fijo")));
        rendicion.setFechaEmisionRendicion(rs.getDate("fecha_emision_rendicion"));
        rendicion.setFechaReposicion(rs.getDate("ff_rendicion_fecha_reposicion"));
        long numero = rs.getLong("nro_rendicion");
        rendicion.setNumeroRendicion(rs.wasNull() ? null : numero);
        rendicion.setEstado(rs.getString("ff_rendicion_estado"));
        return rendicion;
    }

    public FondoFijoRendicion getRendicion(Long idRendicion) throws SQLException {
        if (idRendicion == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM fondo_fijo_rendicion WHERE id_fondofijo_rendicion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRendicion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Listado del modal de busqueda: las mas nuevas primero, anuladas incluidas. */
    public List<FondoFijoRendicion> listarRendiciones() throws SQLException {
        List<FondoFijoRendicion> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM fondo_fijo_rendicion ORDER BY id_fondofijo_rendicion DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Rendiciones en un estado dado: el modal de la provision solo ofrece las 'Generada'. */
    public List<FondoFijoRendicion> listarRendicionesPorEstado(String estado) throws SQLException {
        List<FondoFijoRendicion> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM fondo_fijo_rendicion "
                   + "WHERE ff_rendicion_estado = ? ORDER BY id_fondofijo_rendicion DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Completa la fecha de reposicion cuando se paga la orden de pago de la reposicion.
     * Con fecha nula la borra, que es lo que corresponde si esa orden de pago se anula.
     */
    public void registrarReposicion(Long idRendicion, java.util.Date fechaReposicion) throws SQLException {
        String sql = "UPDATE fondo_fijo_rendicion SET ff_rendicion_fecha_reposicion = ? "
                   + "WHERE id_fondofijo_rendicion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (fechaReposicion == null) {
                stmt.setNull(1, java.sql.Types.DATE);
            } else {
                stmt.setDate(1, new java.sql.Date(fechaReposicion.getTime()));
            }
            stmt.setLong(2, idRendicion);
            stmt.executeUpdate();
        }
    }

    /**
     * Proximo nro_rendicion, que es un correlativo propio y no el id. El valor definitivo se
     * confirma recien al insertar: si dos usuarios abren una rendicion a la vez ven el mismo,
     * igual que pasa con el numero de la orden de pago.
     */
    public long obtenerProximoNumero() throws SQLException {
        String sql = "SELECT COALESCE(MAX(nro_rendicion), 0) + 1 FROM fondo_fijo_rendicion";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public Long insertarRendicion(FondoFijoRendicion rendicion) throws SQLException {
        String sql = "INSERT INTO fondo_fijo_rendicion (id_fondo_fijo, fecha_emision_rendicion, "
                   + "ff_rendicion_fecha_reposicion, nro_rendicion, ff_rendicion_estado) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, rendicion.getFondoFijo().getIdFondoFijo());
            stmt.setDate(2, new java.sql.Date(rendicion.getFechaEmisionRendicion().getTime()));
            // La fecha de reposicion se completa cuando se paga la orden de pago de reposicion.
            stmt.setNull(3, java.sql.Types.DATE);
            stmt.setLong(4, rendicion.getNumeroRendicion());
            stmt.setString(5, ESTADO_GENERADA);

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó la rendición, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    rendicion.setIdFondoFijoRendicion(id);
                    rendicion.setEstado(ESTADO_GENERADA);
                    return id;
                }
            }
            throw new SQLException("No se generó id de rendición");
        }
    }

    public void insertarDetalle(FondoFijoRendicionDetalle detalle, Long idRendicion) throws SQLException {
        String sql = "INSERT INTO fondo_fijo_rendicion_detalle (id_fondofijo_rendicion, id_cta_pagar, "
                   + "id_fact_comp_cab, monto_rendido) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRendicion);
            stmt.setLong(2, detalle.getCuentaPagar().getIdCuentaPagar());
            stmt.setLong(3, detalle.getCuentaPagar().getFacturaCompra().getIdFacturaCompra());
            stmt.setLong(4, detalle.getMontoRendido());
            stmt.executeUpdate();
        }
    }

    public List<FondoFijoRendicionDetalle> listarDetallesPorRendicion(Long idRendicion) throws SQLException {
        List<FondoFijoRendicionDetalle> lista = new ArrayList<>();
        if (idRendicion == null) {
            return lista;
        }
        String sql = "SELECT id_ff_rendicion_detalle, id_cta_pagar, id_fact_comp_cab, monto_rendido "
                   + "FROM fondo_fijo_rendicion_detalle WHERE id_fondofijo_rendicion = ? "
                   + "ORDER BY id_ff_rendicion_detalle";
        CuentaPagarDAO cuentaPagarDAO = new CuentaPagarDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRendicion);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FondoFijoRendicionDetalle detalle = new FondoFijoRendicionDetalle();
                    detalle.setIdFondoFijoRendicionDetalle(rs.getLong("id_ff_rendicion_detalle"));
                    detalle.setCuentaPagar(cuentaPagarDAO.getCuentaPagar(
                            rs.getLong("id_cta_pagar"), rs.getLong("id_fact_comp_cab")));
                    detalle.setMontoRendido(rs.getLong("monto_rendido"));
                    lista.add(detalle);
                }
            }
        }
        return lista;
    }

    /** Estado de la rendicion bloqueando la fila, para que no se anule dos veces a la vez. */
    public String getEstadoBloqueado(Long idRendicion) throws SQLException {
        String sql = "SELECT ff_rendicion_estado FROM fondo_fijo_rendicion "
                   + "WHERE id_fondofijo_rendicion = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRendicion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ff_rendicion_estado");
                }
            }
        }
        return null;
    }

    public void actualizarEstado(Long idRendicion, String estado) throws SQLException {
        String sql = "UPDATE fondo_fijo_rendicion SET ff_rendicion_estado = ? "
                   + "WHERE id_fondofijo_rendicion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setLong(2, idRendicion);
            stmt.executeUpdate();
        }
    }
}
