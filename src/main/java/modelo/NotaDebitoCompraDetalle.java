/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaDebitoCompraDetalle {

    private NotaDebitoCompra notaDebitoCompra;
    private Articulo articulo;
    private Long cantidad;
    private Long monto;

    public NotaDebitoCompraDetalle() {
    }

    public NotaDebitoCompraDetalle(NotaDebitoCompra notaDebitoCompra, Articulo articulo, Long cantidad, Long monto) {
        this.notaDebitoCompra = notaDebitoCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.monto = monto;
    }

    public NotaDebitoCompra getNotaDebitoCompra() {
        return notaDebitoCompra;
    }

    public void setNotaDebitoCompra(NotaDebitoCompra notaDebitoCompra) {
        this.notaDebitoCompra = notaDebitoCompra;
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
