/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Date;

/**
 *
 * @author Miguel
 */
public class PedidoVenta {

    private Long idPedidoVenta;
    private Date fecha;
    private String estado;
    private Cliente cliente;
    private Sucursal sucursal;
    private Usuario usuario;
    private String listaArticulos;

    public PedidoVenta() {
    }

    public PedidoVenta(Long idPedidoVenta) {
        this.idPedidoVenta = idPedidoVenta;
    }

    public PedidoVenta(Long idPedidoVenta, Date fecha, String estado, Cliente cliente,
            Sucursal sucursal, Usuario usuario, String listaArticulos) {
        this.idPedidoVenta = idPedidoVenta;
        this.fecha = fecha;
        this.estado = estado;
        this.cliente = cliente;
        this.sucursal = sucursal;
        this.usuario = usuario;
        this.listaArticulos = listaArticulos;
    }

    public Long getIdPedidoVenta() {
        return idPedidoVenta;
    }

    public void setIdPedidoVenta(Long idPedidoVenta) {
        this.idPedidoVenta = idPedidoVenta;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getListaArticulos() {
        return listaArticulos;
    }

    public void setListaArticulos(String listaArticulos) {
        this.listaArticulos = listaArticulos;
    }
}
