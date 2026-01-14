/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Miguel
 */
public class PedidoCompra {

    private Long idPedido;
    private Usuario usuario;
    private Sucursal sucursal;
    private Date fecha;
    private String estado;
    private String listaArticulos;
    private List<PedidoCompraDetalle> pedidoCompDetalles;

    public PedidoCompra() {
    }

    public PedidoCompra(Long idPedido) {
        this.idPedido = idPedido;
    }

    public PedidoCompra(Long idPedido, Usuario usuario, Sucursal sucursal, Date fecha, String estado) {
        this.idPedido = idPedido;
        this.usuario = usuario;
        this.sucursal = sucursal;
        this.fecha = fecha;
        this.estado = estado;
    }

    public PedidoCompra(Long idPedido, Usuario usuario, Sucursal sucursal, Date fecha, String estado, List<PedidoCompraDetalle> pedidoCompDetalles) {
        this.idPedido = idPedido;
        this.usuario = usuario;
        this.sucursal = sucursal;
        this.fecha = fecha;
        this.estado = estado;
        this.pedidoCompDetalles = pedidoCompDetalles;
    }

    public PedidoCompra(Long idPedido, Usuario usuario, Sucursal sucursal, Date fecha, String estado, String listaArticulos) {
        this.idPedido = idPedido;
        this.usuario = usuario;
        this.sucursal = sucursal;
        this.fecha = fecha;
        this.estado = estado;
        this.listaArticulos = listaArticulos;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<PedidoCompraDetalle> getDetalles() {
        return pedidoCompDetalles;
    }

    public void setDetalles(List<PedidoCompraDetalle> pedidoCompDetalles) {
        this.pedidoCompDetalles = pedidoCompDetalles;
    }

    public String getListaArticulos() {
        return listaArticulos;
    }

    public void setListaArticulos(String listaArticulos) {
        this.listaArticulos = listaArticulos;
    }

}
