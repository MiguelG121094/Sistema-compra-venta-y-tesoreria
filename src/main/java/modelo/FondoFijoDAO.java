package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de fondo fijo: la caja chica asignada a un responsable (requerimiento 3.5 del
 * MODULO_TESORERIA_PLAN.md §E). Sobre el fondo fijo se hacen las rendiciones de las facturas
 * compradas con esa caja. Corre sobre la Connection compartida (la transaccion la controla el
 * Service).
 *
 * @author Miguel
 */
public class FondoFijoDAO {

    private static final String COLUMNAS =
            "id_fondo_fijo, fondo_fijo_responsable, fondo_fijo_monto_asignado, "
            + "fondo_fijo_fecha_asig, id_proveedor";

    private Connection conn;

    public FondoFijoDAO(Connection conn) {
        this.conn = conn;
    }

    private FondoFijo mapear(ResultSet rs) throws SQLException {
        FondoFijo fondoFijo = new FondoFijo();
        fondoFijo.setIdFondoFijo(rs.getLong("id_fondo_fijo"));
        fondoFijo.setResponsable(rs.getString("fondo_fijo_responsable"));
        fondoFijo.setMontoAsignado(rs.getLong("fondo_fijo_monto_asignado"));
        fondoFijo.setFechaAsignacion(rs.getDate("fondo_fijo_fecha_asig"));
        fondoFijo.setProveedor(new ProveedorDAO(conn).getProveedor(rs.getLong("id_proveedor")));
        return fondoFijo;
    }

    public FondoFijo getFondoFijo(Long idFondoFijo) throws SQLException {
        if (idFondoFijo == null) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM fondo_fijo WHERE id_fondo_fijo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFondoFijo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<FondoFijo> listarFondosFijos() throws SQLException {
        List<FondoFijo> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM fondo_fijo ORDER BY id_fondo_fijo";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void insertarFondoFijo(FondoFijo fondoFijo) throws SQLException {
        String sql = "INSERT INTO fondo_fijo (fondo_fijo_responsable, fondo_fijo_monto_asignado, "
                   + "fondo_fijo_fecha_asig, id_proveedor) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fondoFijo.getResponsable());
            stmt.setLong(2, fondoFijo.getMontoAsignado());
            stmt.setDate(3, new java.sql.Date(fondoFijo.getFechaAsignacion().getTime()));
            stmt.setLong(4, fondoFijo.getProveedor().getIdProveedor());
            stmt.executeUpdate();
        }
    }

    public void actualizarFondoFijo(FondoFijo fondoFijo) throws SQLException {
        String sql = "UPDATE fondo_fijo SET fondo_fijo_responsable = ?, fondo_fijo_monto_asignado = ?, "
                   + "fondo_fijo_fecha_asig = ?, id_proveedor = ? WHERE id_fondo_fijo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fondoFijo.getResponsable());
            stmt.setLong(2, fondoFijo.getMontoAsignado());
            stmt.setDate(3, new java.sql.Date(fondoFijo.getFechaAsignacion().getTime()));
            stmt.setLong(4, fondoFijo.getProveedor().getIdProveedor());
            stmt.setLong(5, fondoFijo.getIdFondoFijo());
            stmt.executeUpdate();
        }
    }

    public void eliminarFondoFijo(Long idFondoFijo) throws SQLException {
        String sql = "DELETE FROM fondo_fijo WHERE id_fondo_fijo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFondoFijo);
            stmt.executeUpdate();
        }
    }

    /** El proveedor es responsable de algun fondo fijo. */
    public boolean esResponsableDeFondoFijo(Long idProveedor) throws SQLException {
        if (idProveedor == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM fondo_fijo WHERE id_proveedor = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProveedor);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        }
    }

    /** Rendiciones que cuelgan del fondo fijo: mientras haya alguna, no se puede borrar. */
    public long contarRendiciones(Long idFondoFijo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fondo_fijo_rendicion WHERE id_fondo_fijo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFondoFijo);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
