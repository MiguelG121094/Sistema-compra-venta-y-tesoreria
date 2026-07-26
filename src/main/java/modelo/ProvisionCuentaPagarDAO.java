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
 * DAO de provisión de cuenta a pagar (cabecera por proveedor + detalle de facturas).
 * Corre sobre la Connection compartida; la transacción la controla el Service.
 *
 * @author Miguel
 */
public class ProvisionCuentaPagarDAO {

    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(ProvisionCuentaPagarDAO.class.getName());

    public ProvisionCuentaPagarDAO(Connection conn) {
        this.conn = conn;
    }

    public Long insertarProvision(ProvisionCuentaPagar provision) throws SQLException {
        if (provision == null) {
            LOGGER.log(Level.SEVERE, "Error: la provisión es nula");
            return null;
        }
        String sql = "INSERT INTO provision_cuenta_pagar (prov_cta_pag_estado, prov_cta_pag_fecha, id_proveedor) "
                   + "VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, provision.getEstado());
            stmt.setDate(2, new java.sql.Date(provision.getFecha().getTime()));
            stmt.setLong(3, provision.getProveedor().getIdProveedor());
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó la provisión, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    provision.setIdProvisionCuentaPagar(id);
                    return id;
                }
            }
            throw new SQLException("No se generó id de provisión");
        }
    }

    public void insertarDetalle(ProvisionCuentaPagarDetalle detalle, Long idProvision) throws SQLException {
        String sql = "INSERT INTO provision_cuenta_pagar_detalle "
                   + "(id_provi_cta_pagar_cabecera, id_cta_pagar, id_fact_comp_cab, prov_cta_pag_monto) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProvision);
            stmt.setLong(2, detalle.getCuentaPagar().getIdCuentaPagar());
            stmt.setLong(3, detalle.getCuentaPagar().getFacturaCompra().getIdFacturaCompra());
            stmt.setLong(4, detalle.getMonto());
            stmt.executeUpdate();
        }
    }

    public List<ProvisionCuentaPagar> listarProvisiones() throws SQLException {
        List<ProvisionCuentaPagar> lista = new ArrayList<>();
        String sql = "SELECT id_provi_cta_pagar_cabecera, prov_cta_pag_estado, prov_cta_pag_fecha, id_proveedor "
                   + "FROM provision_cuenta_pagar ORDER BY id_provi_cta_pagar_cabecera DESC";
        ProveedorDAO proveedorDAO = new ProveedorDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new ProvisionCuentaPagar(
                    rs.getLong("id_provi_cta_pagar_cabecera"),
                    rs.getString("prov_cta_pag_estado"),
                    rs.getDate("prov_cta_pag_fecha"),
                    proveedorDAO.getProveedor(rs.getLong("id_proveedor"))));
            }
        }
        return lista;
    }

    /**
     * Provisiones filtradas por estado. La Orden de Pago solo puede partir de una provisión
     * 'Pendiente' (las 'Procesada' ya fueron pagadas y las 'Anulado' no sirven).
     */
    public List<ProvisionCuentaPagar> listarProvisionesPorEstado(String estado) throws SQLException {
        List<ProvisionCuentaPagar> lista = new ArrayList<>();
        String sql = "SELECT id_provi_cta_pagar_cabecera, prov_cta_pag_estado, prov_cta_pag_fecha, id_proveedor "
                   + "FROM provision_cuenta_pagar WHERE prov_cta_pag_estado = ? "
                   + "ORDER BY id_provi_cta_pagar_cabecera DESC";
        ProveedorDAO proveedorDAO = new ProveedorDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ProvisionCuentaPagar(
                        rs.getLong("id_provi_cta_pagar_cabecera"),
                        rs.getString("prov_cta_pag_estado"),
                        rs.getDate("prov_cta_pag_fecha"),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor"))));
                }
            }
        }
        return lista;
    }

    public ProvisionCuentaPagar getProvision(Long idProvision) throws SQLException {
        if (idProvision == null) {
            return null;
        }
        String sql = "SELECT id_provi_cta_pagar_cabecera, prov_cta_pag_estado, prov_cta_pag_fecha, id_proveedor "
                   + "FROM provision_cuenta_pagar WHERE id_provi_cta_pagar_cabecera = ?";
        ProveedorDAO proveedorDAO = new ProveedorDAO(conn);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProvision);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ProvisionCuentaPagar(
                        rs.getLong("id_provi_cta_pagar_cabecera"),
                        rs.getString("prov_cta_pag_estado"),
                        rs.getDate("prov_cta_pag_fecha"),
                        proveedorDAO.getProveedor(rs.getLong("id_proveedor")));
                }
            }
        }
        return null;
    }

    /**
     * Detalle de una provisión, con la cuenta a pagar hidratada (factura, monto, saldo, plazo).
     */
    public List<ProvisionCuentaPagarDetalle> listarDetallesPorProvision(Long idProvision) throws SQLException {
        List<ProvisionCuentaPagarDetalle> detalles = new ArrayList<>();
        String sql = "SELECT d.id_provi_cta_pagar_detalle, d.id_cta_pagar, d.id_fact_comp_cab, d.prov_cta_pag_monto, "
                   + "cp.cta_pag_monto, cp.cta_pag_saldo, cp.cta_pag_plazo, f.fact_comp_numero "
                   + "FROM provision_cuenta_pagar_detalle d "
                   + "JOIN cuenta_pagar cp ON d.id_cta_pagar = cp.id_cta_pagar AND d.id_fact_comp_cab = cp.id_fact_comp_cab "
                   + "JOIN factura_compra_cabecera f ON d.id_fact_comp_cab = f.id_fact_comp_cab "
                   + "WHERE d.id_provi_cta_pagar_cabecera = ? ORDER BY d.id_provi_cta_pagar_detalle";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProvision);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FacturaCompra fc = new FacturaCompra(rs.getLong("id_fact_comp_cab"));
                    fc.setNumero(rs.getString("fact_comp_numero"));

                    CuentaPagar cp = new CuentaPagar();
                    cp.setIdCuentaPagar(rs.getLong("id_cta_pagar"));
                    cp.setFacturaCompra(fc);
                    cp.setMonto(rs.getLong("cta_pag_monto"));
                    cp.setSaldo(rs.getLong("cta_pag_saldo"));
                    long plazo = rs.getLong("cta_pag_plazo");
                    cp.setPlazo(rs.wasNull() ? null : plazo);

                    ProvisionCuentaPagarDetalle det = new ProvisionCuentaPagarDetalle();
                    det.setIdProvisionCuentaPagarDetalle(rs.getLong("id_provi_cta_pagar_detalle"));
                    det.setCuentaPagar(cp);
                    det.setMonto(rs.getLong("prov_cta_pag_monto"));
                    detalles.add(det);
                }
            }
        }
        return detalles;
    }

    public void anularProvision(Long idProvision) throws SQLException {
        String sql = "UPDATE provision_cuenta_pagar SET prov_cta_pag_estado = 'Anulado' "
                   + "WHERE id_provi_cta_pagar_cabecera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProvision);
            stmt.executeUpdate();
        }
    }

    /**
     * Lee el estado de la provisión bloqueando la fila hasta el commit (FOR UPDATE). Sirve para
     * consumir la provisión al generar la Orden de Pago sin condición de carrera: dos OPs
     * concurrentes sobre la misma provisión se serializan y solo la primera la ve 'Pendiente'.
     * Ver MODULO_TESORERIA_PLAN.md §C (evita el doble pago).
     *
     * @return el estado actual, o null si la provisión no existe
     */
    public String getEstadoBloqueado(Long idProvision) throws SQLException {
        if (idProvision == null) {
            return null;
        }
        String sql = "SELECT prov_cta_pag_estado FROM provision_cuenta_pagar "
                   + "WHERE id_provi_cta_pagar_cabecera = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProvision);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("prov_cta_pag_estado");
                }
            }
        }
        return null;
    }

    /**
     * Cambia el estado de la provisión (p. ej. 'Pendiente' -&gt; 'Procesada' al generar la OP, o
     * 'Procesada' -&gt; 'Pendiente' al anularla). Corre sobre la Connection compartida (la
     * transacción la controla el Service).
     */
    public void actualizarEstado(Long idProvision, String estado) throws SQLException {
        String sql = "UPDATE provision_cuenta_pagar SET prov_cta_pag_estado = ? "
                   + "WHERE id_provi_cta_pagar_cabecera = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setLong(2, idProvision);
            stmt.executeUpdate();
        }
    }
}
