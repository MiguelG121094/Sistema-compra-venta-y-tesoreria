/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaRemisionCompraDetalle {

    private NotaRemisionCompra notaRemisionCompra;
    private Articulo articulo;
    private Long cantidad;

    public NotaRemisionCompraDetalle() {
    }

    public NotaRemisionCompraDetalle(NotaRemisionCompra notaRemisionCompra, Articulo articulo) {
        this.notaRemisionCompra = notaRemisionCompra;
        this.articulo = articulo;
    }

    public NotaRemisionCompraDetalle(NotaRemisionCompra notaRemisionCompra, Articulo articulo,
            Long cantidad) {
        this.notaRemisionCompra = notaRemisionCompra;
        this.articulo = articulo;
        this.cantidad = cantidad;
    }

    public NotaRemisionCompra getNotaRemisionCompra() {
        return notaRemisionCompra;
    }

    public void setNotaRemisionCompra(NotaRemisionCompra notaRemisionCompra) {
        this.notaRemisionCompra = notaRemisionCompra;
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
}
