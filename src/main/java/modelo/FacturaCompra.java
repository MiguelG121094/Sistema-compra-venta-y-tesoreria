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
public class FacturaCompra {

    private Long idFacturaCompra;
    private String numero;
    private Integer timbrado;
    private Date fechaVenciTimbrado;
    private Date fechaEmision;
    private Date fechaCarga;
    private String condicion;
    private Integer plazo;
    private Date fechaVencimiento;
    private String observacion;
    private String estado;
    private String tipoFactura;
    private Proveedor proveedor;
    private Sucursal sucursal;
    private Usuario usuario;
    private OrdenCompra ordenCompra;
    private String listaArticulos;
    private List<FacturaCompraDetalle> facturaCompraDetalles;

    public FacturaCompra() {
    }

    public FacturaCompra(Long idFacturaCompra) {
        this.idFacturaCompra = idFacturaCompra;
    }

    public FacturaCompra(Long idFacturaCompra, String numero, Integer timbrado, Date fechaVenciTimbrado,
            Date fechaEmision, Date fechaCarga, String condicion, Integer plazo, Date fechaVencimiento,
            String observacion, String estado, String tipoFactura, Proveedor proveedor, Sucursal sucursal,
            Usuario usuario, OrdenCompra ordenCompra) {
        this.idFacturaCompra = idFacturaCompra;
        this.numero = numero;
        this.timbrado = timbrado;
        this.fechaVenciTimbrado = fechaVenciTimbrado;
        this.fechaEmision = fechaEmision;
        this.fechaCarga = fechaCarga;
        this.condicion = condicion;
        this.plazo = plazo;
        this.fechaVencimiento = fechaVencimiento;
        this.observacion = observacion;
        this.estado = estado;
        this.tipoFactura = tipoFactura;
        this.proveedor = proveedor;
        this.sucursal = sucursal;
        this.usuario = usuario;
        this.ordenCompra = ordenCompra;
    }

    public Long getIdFacturaCompra() {
        return idFacturaCompra;
    }

    public void setIdFacturaCompra(Long idFacturaCompra) {
        this.idFacturaCompra = idFacturaCompra;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Integer timbrado) {
        this.timbrado = timbrado;
    }

    public Date getFechaVenciTimbrado() {
        return fechaVenciTimbrado;
    }

    public void setFechaVenciTimbrado(Date fechaVenciTimbrado) {
        this.fechaVenciTimbrado = fechaVenciTimbrado;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
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

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
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

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
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

    public OrdenCompra getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(OrdenCompra ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public String getListaArticulos() {
        return listaArticulos;
    }

    public void setListaArticulos(String listaArticulos) {
        this.listaArticulos = listaArticulos;
    }

    public List<FacturaCompraDetalle> getFacturaCompraDetalles() {
        return facturaCompraDetalles;
    }

    public void setFacturaCompraDetalles(List<FacturaCompraDetalle> facturaCompraDetalles) {
        this.facturaCompraDetalles = facturaCompraDetalles;
    }
}
