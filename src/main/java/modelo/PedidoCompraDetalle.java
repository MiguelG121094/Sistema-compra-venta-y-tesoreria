/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Miguel
 */
public class PedidoCompraDetalle {

    private PedidoCompra pedido;
    private Articulo articulo;
    private Long cantidad;
    private Deposito deposito;

    public PedidoCompraDetalle() {
    }

    public PedidoCompraDetalle(PedidoCompra pedido, Articulo articulo, Long cantidad, Deposito deposito) {
        this.pedido = pedido;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.deposito = deposito;
    }

    public PedidoCompra getPedido() {
        return pedido;
    }

    public void setPedido(PedidoCompra pedido) {
        this.pedido = pedido;
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

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }
}
