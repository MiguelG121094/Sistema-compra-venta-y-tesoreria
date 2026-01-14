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
public class Presupuesto {

    private Long idPresupuesto;
    private PedidoCompra pedidoCompra;
    private Proveedor proveedor;    
    private Date fecha;
    private String estado;
    private Usuario usuario;
    private String listaArticulos;
    private List<PresupuestoDetalle> presupuestoDetalle;

    public Presupuesto() {
    }

    public Presupuesto(Long idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public Presupuesto(Long idPresupuesto, PedidoCompra pedidoCompra, Proveedor proveedor, Date fecha, String estado, Usuario usuario) {
        this.idPresupuesto = idPresupuesto;
        this.pedidoCompra = pedidoCompra;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.estado = estado;
        this.usuario = usuario;
    }

    public Presupuesto(Long idPresupuesto, PedidoCompra pedidoCompra, Proveedor proveedor, Date fecha, String estado, Usuario usuario, String listaArticulos) {
        this.idPresupuesto = idPresupuesto;
        this.pedidoCompra = pedidoCompra;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.estado = estado;
        this.usuario = usuario;
        this.listaArticulos = listaArticulos;
    }

    public Presupuesto(Long idPresupuesto, PedidoCompra pedidoCompra, Proveedor proveedor, Date fecha, String estado, Usuario usuario, String listaArticulos, List<PresupuestoDetalle> presupuestoDetalle) {
        this.idPresupuesto = idPresupuesto;
        this.pedidoCompra = pedidoCompra;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.estado = estado;
        this.usuario = usuario;
        this.listaArticulos = listaArticulos;
        this.presupuestoDetalle = presupuestoDetalle;
    }

    public Long getIdPresupuesto() {
        return idPresupuesto;
    }

    public void setIdPresupuesto(Long idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public PedidoCompra getPedidoCompra() {
        return pedidoCompra;
    }

    public void setPedidoCompra(PedidoCompra pedidoCompra) {
        this.pedidoCompra = pedidoCompra;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
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

    public List<PresupuestoDetalle> getPresupuestoDetalle() {
        return presupuestoDetalle;
    }

    public void setPresupuestoDetalle(List<PresupuestoDetalle> presupuestoDetalle) {
        this.presupuestoDetalle = presupuestoDetalle;
    }

    
}
