/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class CobroDetalle {

    private Cobro cobro;
    private CuentaCobrar cuentaCobrar;
    private FacturaVenta facturaVenta;
    private Long cantidad;

    public CobroDetalle() {
    }

    public CobroDetalle(Cobro cobro, CuentaCobrar cuentaCobrar, FacturaVenta facturaVenta) {
        this.cobro = cobro;
        this.cuentaCobrar = cuentaCobrar;
        this.facturaVenta = facturaVenta;
    }

    public CobroDetalle(Cobro cobro, CuentaCobrar cuentaCobrar, FacturaVenta facturaVenta, Long cantidad) {
        this.cobro = cobro;
        this.cuentaCobrar = cuentaCobrar;
        this.facturaVenta = facturaVenta;
        this.cantidad = cantidad;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public CuentaCobrar getCuentaCobrar() {
        return cuentaCobrar;
    }

    public void setCuentaCobrar(CuentaCobrar cuentaCobrar) {
        this.cuentaCobrar = cuentaCobrar;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }
}
