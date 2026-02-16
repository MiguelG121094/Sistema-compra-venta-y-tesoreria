/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class NotaRemisionVentaDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private NotaRemisionVenta notaRemisionVenta;
    private Articulo articulo;
    private Integer cantidad;

    public NotaRemisionVentaDetalle() {
    }

    public NotaRemisionVentaDetalle(NotaRemisionVenta notaRemisionVenta, Articulo articulo, Integer cantidad) {
        this.notaRemisionVenta = notaRemisionVenta;
        this.articulo = articulo;
        this.cantidad = cantidad;
    }

    public NotaRemisionVenta getNotaRemisionVenta() {
        return notaRemisionVenta;
    }

    public void setNotaRemisionVenta(NotaRemisionVenta notaRemisionVenta) {
        this.notaRemisionVenta = notaRemisionVenta;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
