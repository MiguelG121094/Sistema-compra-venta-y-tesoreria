/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class PedidoVentaDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private PedidoVenta pedidoVenta;
    private Articulo articulo;
    private Integer cantidad;

    public PedidoVentaDetalle() {
    }

    public PedidoVentaDetalle(PedidoVenta pedidoVenta, Articulo articulo, Integer cantidad) {
        this.pedidoVenta = pedidoVenta;
        this.articulo = articulo;
        this.cantidad = cantidad;
    }

    public PedidoVenta getPedidoVenta() {
        return pedidoVenta;
    }

    public void setPedidoVenta(PedidoVenta pedidoVenta) {
        this.pedidoVenta = pedidoVenta;
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
