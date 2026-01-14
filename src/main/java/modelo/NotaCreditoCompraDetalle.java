/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaCreditoCompraDetalle {

    private NotaCreditoCompra notaCreditoCompra;
    private Articulo articulo;
    private Long cantidad;
    private Long monto;

    public NotaCreditoCompraDetalle() {
    }

    public NotaCreditoCompraDetalle(NotaCreditoCompra notaCreditoCompra, Articulo articulo, Long cantidad, Long monto) {
        this.notaCreditoCompra = notaCreditoCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.monto = monto;
    }

    public NotaCreditoCompra getNotaCreditoCompra() {
        return notaCreditoCompra;
    }

    public void setNotaCreditoCompra(NotaCreditoCompra notaCreditoCompra) {
        this.notaCreditoCompra = notaCreditoCompra;
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

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
