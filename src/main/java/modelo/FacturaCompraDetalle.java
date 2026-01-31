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

    // Campos calculados (no persistidos en BD)
    private Long subtotal;
    private Long gravada10;
    private Long iva10;
    private Long gravada5;
    private Long iva5;
    private Long exenta;

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

    /**
     * Devuelve la descripción del artículo si existe, o la descripción directa del detalle.
     */
    public String getDescripcionDisplay() {
        if (articulo != null && articulo.getDescripcion() != null) {
            return articulo.getDescripcion();
        }
        return descripcion;
    }

    public Long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Long subtotal) {
        this.subtotal = subtotal;
    }

    public Long getGravada10() {
        return gravada10;
    }

    public void setGravada10(Long gravada10) {
        this.gravada10 = gravada10;
    }

    public Long getIva10() {
        return iva10;
    }

    public void setIva10(Long iva10) {
        this.iva10 = iva10;
    }

    public Long getGravada5() {
        return gravada5;
    }

    public void setGravada5(Long gravada5) {
        this.gravada5 = gravada5;
    }

    public Long getIva5() {
        return iva5;
    }

    public void setIva5(Long iva5) {
        this.iva5 = iva5;
    }

    public Long getExenta() {
        return exenta;
    }

    public void setExenta(Long exenta) {
        this.exenta = exenta;
    }
}
