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
public class FacturaVenta implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFacturaVenta;
    private Date fechaEmision;
    private Integer numero;
    private String condicion;
    private Integer plazo;
    private String observacion;
    private String estado;
    private PedidoVenta pedidoVenta;
    private Cliente cliente;
    private Usuario usuario;
    private Sucursal sucursal;
    private AperturaCierreCaja aperturaCierreCaja;
    private Timbrado timbrado;
    private String listaArticulos;

    public FacturaVenta() {
    }

    public FacturaVenta(Long idFacturaVenta) {
        this.idFacturaVenta = idFacturaVenta;
    }

    public FacturaVenta(Long idFacturaVenta, Date fechaEmision, Integer numero, String condicion,
            Integer plazo, String observacion, String estado, PedidoVenta pedidoVenta,
            Cliente cliente, Usuario usuario, Sucursal sucursal, AperturaCierreCaja aperturaCierreCaja,
            Timbrado timbrado, String listaArticulos) {
        this.idFacturaVenta = idFacturaVenta;
        this.fechaEmision = fechaEmision;
        this.numero = numero;
        this.condicion = condicion;
        this.plazo = plazo;
        this.observacion = observacion;
        this.estado = estado;
        this.pedidoVenta = pedidoVenta;
        this.cliente = cliente;
        this.usuario = usuario;
        this.sucursal = sucursal;
        this.aperturaCierreCaja = aperturaCierreCaja;
        this.timbrado = timbrado;
        this.listaArticulos = listaArticulos;
    }

    public Long getIdFacturaVenta() {
        return idFacturaVenta;
    }

    public void setIdFacturaVenta(Long idFacturaVenta) {
        this.idFacturaVenta = idFacturaVenta;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public Integer getPlazo() {
        return plazo;
    }

    public void setPlazo(Integer plazo) {
        this.plazo = plazo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public PedidoVenta getPedidoVenta() {
        return pedidoVenta;
    }

    public void setPedidoVenta(PedidoVenta pedidoVenta) {
        this.pedidoVenta = pedidoVenta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public AperturaCierreCaja getAperturaCierreCaja() {
        return aperturaCierreCaja;
    }

    public void setAperturaCierreCaja(AperturaCierreCaja aperturaCierreCaja) {
        this.aperturaCierreCaja = aperturaCierreCaja;
    }

    public Timbrado getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Timbrado timbrado) {
        this.timbrado = timbrado;
    }

    public String getListaArticulos() {
        return listaArticulos;
    }

    public void setListaArticulos(String listaArticulos) {
        this.listaArticulos = listaArticulos;
    }
}
