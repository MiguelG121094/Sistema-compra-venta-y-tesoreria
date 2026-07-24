package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO del detalle de la orden de pago (las facturas de la provisión, SOLO LECTURA en la pantalla).
 * Cada fila referencia la cuenta a pagar por su FK compuesta (id_cta_pagar, id_fact_comp_cab).
 * Corre sobre la Connection compartida; la transacción la controla el Service.
 *
 * @author Miguel
 */
public class OrdenPagoDetalleDAO {

    private Connection conn;

    public OrdenPagoDetalleDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertarDetalle(OrdenPagoDetalle detalle, Long idOrdenPago) throws SQLException {
        String sql = "INSERT INTO orden_pago_detalle "
                   + "(orden_pag_det_monto, id_cta_pagar, id_fact_comp_cab, id_orden_pago) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, detalle.getMonto());
            stmt.setLong(2, detalle.getCuentaPagar().getIdCuentaPagar());
            stmt.setLong(3, detalle.getCuentaPagar().getFacturaCompra().getIdFacturaCompra());
            stmt.setLong(4, idOrdenPago);
            stmt.executeUpdate();
        }
    }

    /**
     * Detalle de una OP con la cuenta a pagar hidratada (factura, número, saldo actual) — para ver la OP.
     */
    public List<OrdenPagoDetalle> listarPorOrden(Long idOrdenPago) throws SQLException {
        List<OrdenPagoDetalle> detalles = new ArrayList<>();
        String sql = "SELECT d.id_orden_pago_det, d.orden_pag_det_monto, d.id_cta_pagar, d.id_fact_comp_cab, "
                   + "cp.cta_pag_monto, cp.cta_pag_saldo, cp.cta_pag_plazo, f.fact_comp_numero "
                   + "FROM orden_pago_detalle d "
                   + "JOIN cuenta_pagar cp ON d.id_cta_pagar = cp.id_cta_pagar AND d.id_fact_comp_cab = cp.id_fact_comp_cab "
                   + "JOIN factura_compra_cabecera f ON d.id_fact_comp_cab = f.id_fact_comp_cab "
                   + "WHERE d.id_orden_pago = ? ORDER BY d.id_orden_pago_det";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idOrdenPago);
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

                    OrdenPagoDetalle det = new OrdenPagoDetalle();
                    det.setIdOrdenPagoDet(rs.getLong("id_orden_pago_det"));
                    det.setMonto(rs.getLong("orden_pag_det_monto"));
                    // Ambos campos apuntan a la MISMA factura (invariante canónica del detalle):
                    // cuentaPagar.facturaCompra y facturaCompra son la misma instancia.
                    det.setCuentaPagar(cp);
                    det.setFacturaCompra(fc);
                    detalles.add(det);
                }
            }
        }
        return detalles;
    }
}
