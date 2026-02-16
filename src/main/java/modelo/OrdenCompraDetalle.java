/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class OrdenCompraDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private OrdenCompra ordenCompra;
    private Articulo articulo;
    private Long cantidad;
    private Long precioCompra;

    public OrdenCompraDetalle() {
    }

    public OrdenCompraDetalle(OrdenCompra ordenCompra, Articulo articulo, Long cantidad, Long precioCompra) {
        this.ordenCompra = ordenCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
    }

    public OrdenCompra getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(OrdenCompra ordenCompra) {
        this.ordenCompra = ordenCompra;
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
}
