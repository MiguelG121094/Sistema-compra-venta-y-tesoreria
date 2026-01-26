/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class FacturaCompraDetalle {

    private FacturaCompra facturaCompra;
    private Articulo articulo;
    private Long cantidad;
    private Long precioCompra;
    private String descripcion;
    private TipoImpuesto tipoImpuesto;

    public FacturaCompraDetalle() {
    }

    public FacturaCompraDetalle(FacturaCompra facturaCompra, Articulo articulo, Long cantidad,
            Long precioCompra, String descripcion) {
        this.facturaCompra = facturaCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
        this.descripcion = descripcion;
    }

    public FacturaCompraDetalle(FacturaCompra facturaCompra, Articulo articulo, Long cantidad,
            Long precioCompra, String descripcion, TipoImpuesto tipoImpuesto) {
        this.facturaCompra = facturaCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
        this.descripcion = descripcion;
        this.tipoImpuesto = tipoImpuesto;
    }

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public Long getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Long precioCompra) {
        this.precioCompra = precioCompra;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }
}
