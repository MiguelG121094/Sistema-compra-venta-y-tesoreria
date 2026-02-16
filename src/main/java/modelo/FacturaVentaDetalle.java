/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class FacturaVentaDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private FacturaVenta facturaVenta;
    private Articulo articulo;
    private Deposito deposito;
    private Integer cantidad;
    private Long precioVenta;

    public FacturaVentaDetalle() {
    }

    public FacturaVentaDetalle(FacturaVenta facturaVenta, Articulo articulo, Deposito deposito,
            Integer cantidad, Long precioVenta) {
        this.facturaVenta = facturaVenta;
        this.articulo = articulo;
        this.deposito = deposito;
        this.cantidad = cantidad;
        this.precioVenta = precioVenta;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Long getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Long precioVenta) {
        this.precioVenta = precioVenta;
    }
}
