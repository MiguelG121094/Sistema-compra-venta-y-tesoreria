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
public class NotaDebitoCompra implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idNotaDebitoCompra;
    private String numero;
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
    private List<NotaDebitoCompraDetalle> detalles;

    public NotaDebitoCompra() {
    }

    public NotaDebitoCompra(Long idNotaDebitoCompra) {
        this.idNotaDebitoCompra = idNotaDebitoCompra;
    }

    public NotaDebitoCompra(Long idNotaDebitoCompra, String numero, Integer timbrado, Date fechaVenciTimbrado,
            Date fechaEmision, Date fechaCarga, String estado, String observacion, Usuario usuario,
            Proveedor proveedor, FacturaCompra facturaCompra, String motivo) {
        this.idNotaDebitoCompra = idNotaDebitoCompra;
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

    public Long getIdNotaDebitoCompra() {
        return idNotaDebitoCompra;
    }

    public void setIdNotaDebitoCompra(Long idNotaDebitoCompra) {
        this.idNotaDebitoCompra = idNotaDebitoCompra;
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

    public List<NotaDebitoCompraDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<NotaDebitoCompraDetalle> detalles) {
        this.detalles = detalles;
    }
}
