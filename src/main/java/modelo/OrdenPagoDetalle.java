/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class OrdenPagoDetalle {

    private OrdenPago ordenPago;
    private Long monto;
    private CuentaPagar cuentaPagar;
    private FacturaCompra facturaCompra;

    public OrdenPagoDetalle() {
    }

    public OrdenPagoDetalle(OrdenPago ordenPago) {
        this.ordenPago = ordenPago;
    }

    public OrdenPagoDetalle(OrdenPago ordenPago, Long monto, CuentaPagar cuentaPagar,
            FacturaCompra facturaCompra) {
        this.ordenPago = ordenPago;
        this.monto = monto;
        this.cuentaPagar = cuentaPagar;
        this.facturaCompra = facturaCompra;
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
