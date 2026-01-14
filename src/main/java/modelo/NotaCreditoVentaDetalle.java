/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaCreditoVentaDetalle {

    private NotaCreditoVenta notaCreditoVenta;
    private Articulo articulo;
    private FacturaVenta facturaVenta;
    private Integer cantidad;

    public NotaCreditoVentaDetalle() {
    }

    public NotaCreditoVentaDetalle(NotaCreditoVenta notaCreditoVenta, Articulo articulo,
            FacturaVenta facturaVenta, Integer cantidad) {
        this.notaCreditoVenta = notaCreditoVenta;
        this.articulo = articulo;
        this.facturaVenta = facturaVenta;
        this.cantidad = cantidad;
    }

    public NotaCreditoVenta getNotaCreditoVenta() {
        return notaCreditoVenta;
    }

    public void setNotaCreditoVenta(NotaCreditoVenta notaCreditoVenta) {
        this.notaCreditoVenta = notaCreditoVenta;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
