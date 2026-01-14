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
public class NotaCreditoCompra {

    private Long idNotaCreditoCompra;
    private Integer numero;
    private Integer timbrado;
    private Date fechaVenciTimbrado;
    private Date fechaEmision;
    private Date fechaCarga;
    private String estado;
    private String observacion;
    private Usuario usuario;
    private Proveedor proveedor;
    private FacturaCompra facturaCompra;
    private String motivo;
    private List<NotaCreditoCompraDetalle> detalles;

    public NotaCreditoCompra() {
    }

    public NotaCreditoCompra(Long idNotaCreditoCompra) {
        this.idNotaCreditoCompra = idNotaCreditoCompra;
    }

    public NotaCreditoCompra(Long idNotaCreditoCompra, Integer numero, Integer timbrado, Date fechaVenciTimbrado,
            Date fechaEmision, Date fechaCarga, String estado, String observacion, Usuario usuario,
            Proveedor proveedor, FacturaCompra facturaCompra, String motivo) {
        this.idNotaCreditoCompra = idNotaCreditoCompra;
        this.numero = numero;
        this.timbrado = timbrado;
        this.fechaVenciTimbrado = fechaVenciTimbrado;
        this.fechaEmision = fechaEmision;
        this.fechaCarga = fechaCarga;
        this.estado = estado;
        this.observacion = observacion;
        this.usuario = usuario;
        this.proveedor = proveedor;
        this.facturaCompra = facturaCompra;
        this.motivo = motivo;
    }

    public Long getIdNotaCreditoCompra() {
        return idNotaCreditoCompra;
    }

    public void setIdNotaCreditoCompra(Long idNotaCreditoCompra) {
        this.idNotaCreditoCompra = idNotaCreditoCompra;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public List<NotaCreditoCompraDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<NotaCreditoCompraDetalle> detalles) {
        this.detalles = detalles;
    }
}
