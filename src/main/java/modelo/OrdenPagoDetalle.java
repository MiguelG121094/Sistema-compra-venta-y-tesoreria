/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 * Detalle de la Orden de Pago: una factura de la provisión que se está pagando.
 *
 * <p><b>Invariante canónica:</b> {@code facturaCompra} y {@code cuentaPagar.getFacturaCompra()}
 * refieren SIEMPRE a la misma factura y deben cargarse con la misma información (idealmente la
 * misma instancia). Ambos son fuentes válidas del id de factura; los DAOs los mantienen
 * sincronizados al leer y usan {@code cuentaPagar} como origen al insertar.</p>
 *
 * @author Miguel
 */
public class OrdenPagoDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idOrdenPagoDet;
    private OrdenPago ordenPago;
    private Long monto;
    private CuentaPagar cuentaPagar;
    private FacturaCompra facturaCompra;

    public OrdenPagoDetalle() {
    }

    public OrdenPagoDetalle(OrdenPago ordenPago) {
        this.ordenPago = ordenPago;
    }

    public OrdenPagoDetalle(Long idOrdenPagoDet, OrdenPago ordenPago, Long monto,
            CuentaPagar cuentaPagar, FacturaCompra facturaCompra) {
        this.idOrdenPagoDet = idOrdenPagoDet;
        this.ordenPago = ordenPago;
        this.monto = monto;
        this.cuentaPagar = cuentaPagar;
        this.facturaCompra = facturaCompra;
    }

    public Long getIdOrdenPagoDet() {
        return idOrdenPagoDet;
    }

    public void setIdOrdenPagoDet(Long idOrdenPagoDet) {
        this.idOrdenPagoDet = idOrdenPagoDet;
    }

    public OrdenPago getOrdenPago() {
        return ordenPago;
    }

    public void setOrdenPago(OrdenPago ordenPago) {
        this.ordenPago = ordenPago;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public CuentaPagar getCuentaPagar() {
        return cuentaPagar;
    }

    public void setCuentaPagar(CuentaPagar cuentaPagar) {
        this.cuentaPagar = cuentaPagar;
    }

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }
}
