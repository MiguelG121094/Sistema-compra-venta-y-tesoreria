package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO de la cabecera de orden de pago. Alineado con el esquema nuevo: la cuenta bancaria y
 * el/los cheque(s) ya NO están en la cabecera (viven en forma_pago_detalle). Corre sobre la
 * Connection compartida; la transacción (OP + detalle + formas de pago + descuento de saldo)
 * la controla OrdenPagoService. Ver MODULO_TESORERIA_PLAN.md §C.
 *
 * @author Miguel
 */
public class OrdenPagoDAO {

    private Connection conn;
    private SucursalDAO sucursalDAO;
    private ProveedorDAO proveedorDAO;
    private static final Logger LOGGER = Logger.getLogger(OrdenPagoDAO.class.getName());

    private static final String COLUMNAS =
        "id_orden_pago, ord_pag_numero, ord_pag_fecha_emision, ord_pag_monto, ord_pag_estado, "
      + "id_provi_cta_pagar_cabecera, ord_pag_nro_recibo, id_moneda, ord_pag_tipo_cambio, "
      + "id_sucursal, ord_pag_tipo_pago, id_proveedor";

    public OrdenPagoDAO(Connection conn) {
        this.conn = conn;
    }

    private OrdenPago mapear(ResultSet rs) throws SQLException {
        return new OrdenPago(
            rs.getLong("id_orden_pago"),
            rs.getInt("ord_pag_numero"),
            rs.getDate("ord_pag_fecha_emision"),
            rs.getLong("ord_pag_monto"),
            rs.getString("ord_pag_estado"),
            rs.getLong("id_provi_cta_pagar_cabecera"),
            rs.getInt("ord_pag_nro_recibo"),
            rs.getLong("id_moneda"),
            rs.getDouble("ord_pag_tipo_cambio"),
            sucursalDAO.getSucursal(rs.getLong("id_sucursal")),
            rs.getString("ord_pag_tipo_pago"),
            proveedorDAO.getProveedor(rs.getLong("id_proveedor"))
        );
    }

    public OrdenPago getOrdenPago(Long idOrdenPago) throws SQLException {
        if (idOrdenPago == null) {
            LOGGER.log(Level.WARNING, "Error: idOrdenPago es nulo");
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM orden_pago_cabecera WHERE id_orden_pago = ?";
        sucursalDAO = new SucursalDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenPago);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<OrdenPago> listarOrdenesPago() throws SQLException {
        List<OrdenPago> ordenes = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM orden_pago_cabecera ORDER BY id_orden_pago DESC";
        sucursalDAO = new SucursalDAO(conn);
        proveedorDAO = new ProveedorDAO(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ordenes.add(mapear(rs));
            }
        }
        return ordenes;
    }

    public Long insertarOrdenPago(OrdenPago ordenPago) throws SQLException {
        if (ordenPago == null) {
            throw new SQLException("insertarOrdenPago: la orden de pago es nula");
        }
        String sql = "INSERT INTO orden_pago_cabecera (ord_pag_numero, ord_pag_fecha_emision, "
                   + "ord_pag_monto, ord_pag_estado, id_provi_cta_pagar_cabecera, ord_pag_nro_recibo, "
                   + "id_moneda, ord_pag_tipo_cambio, id_sucursal, ord_pag_tipo_pago, id_proveedor) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ordenPago.getNumero());
            stmt.setDate(2, new java.sql.Date(ordenPago.getFechaEmision().getTime()));
            stmt.setLong(3, ordenPago.getMonto());
            stmt.setString(4, ordenPago.getEstado());
            stmt.setLong(5, ordenPago.getIdProvisionCtaPagar());
            stmt.setInt(6, ordenPago.getNumeroRecibo());
            stmt.setLong(7, ordenPago.getIdMoneda());
            if (ordenPago.getTipoCambio() != null) {
                stmt.setDouble(8, ordenPago.getTipoCambio());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            stmt.setLong(9, ordenPago.getSucursal().getIdSucursal());
            stmt.setString(10, ordenPago.getTipoPago());
            stmt.setLong(11, ordenPago.getProveedor().getIdProveedor());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó la orden de pago, ninguna fila afectada");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    ordenPago.setIdOrdenPago(id);
                    return id;
                }
            }
            throw new SQLException("No se generó id de orden de pago");
        }
    }

    public void anularOrdenPago(Long idOrdenPago) throws SQLException {
        String sql = "UPDATE orden_pago_cabecera SET ord_pag_estado = 'Anulado' WHERE id_orden_pago = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenPago);
            stmt.executeUpdate();
        }
    }
}
