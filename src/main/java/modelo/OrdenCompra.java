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
public class OrdenCompra {

    private Long idOrdenCompra;
    private Presupuesto presupuesto;
    private PedidoCompra pedidoCompra;
    private Proveedor proveedor;
    private Sucursal sucursal;
    private Usuario usuario;
    private Date fecha;
    private String estado;
    private String condicionCompra;
    private String observacion;
    private String listaArticulos;
    private List<OrdenCompraDetalle> ordenCompraDetalles;

    public OrdenCompra() {
    }

    public OrdenCompra(Long idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public OrdenCompra(Long idOrdenCompra, Presupuesto presupuesto, PedidoCompra pedidoCompra,
            Proveedor proveedor, Sucursal sucursal, Usuario usuario, Date fecha,
            String estado, String condicionCompra, String observacion) {
        this.idOrdenCompra = idOrdenCompra;
        this.presupuesto = presupuesto;
        this.pedidoCompra = pedidoCompra;
        this.proveedor = proveedor;
        this.sucursal = sucursal;
        this.usuario = usuario;
        this.fecha = fecha;
        this.estado = estado;
        this.condicionCompra = condicionCompra;
        this.observacion = observacion;
    }

    public Long getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(Long idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public Presupuesto getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(Presupuesto presupuesto) {
        this.presupuesto = presupuesto;
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

    public String getCondicionCompra() {
        return condicionCompra;
    }

    public void setCondicionCompra(String condicionCompra) {
        this.condicionCompra = condicionCompra;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getListaArticulos() {
        return listaArticulos;
    }

    public void setListaArticulos(String listaArticulos) {
        this.listaArticulos = listaArticulos;
    }

    public List<OrdenCompraDetalle> getOrdenCompraDetalles() {
        return ordenCompraDetalles;
    }

    public void setOrdenCompraDetalles(List<OrdenCompraDetalle> ordenCompraDetalles) {
        this.ordenCompraDetalles = ordenCompraDetalles;
    }
}
