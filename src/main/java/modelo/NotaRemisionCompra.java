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
public class NotaRemisionCompra implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idNotaRemisionCompra;
    private Long numero;
    private Long timbrado;
    private Date fechaVencimientoTimbrado;
    private Date fechaEmision;
    private String vehiculo;
    private String conductor;
    private String emisor;
    private String receptor;
    private String estado;
    private String observacion;
    private Usuario usuario;
    private Deposito deposito;

    public NotaRemisionCompra() {
    }

    public NotaRemisionCompra(Long idNotaRemisionCompra) {
        this.idNotaRemisionCompra = idNotaRemisionCompra;
    }

    public NotaRemisionCompra(Long idNotaRemisionCompra, Long numero, Long timbrado,
            Date fechaVencimientoTimbrado, Date fechaEmision, String vehiculo, String conductor,
            String emisor, String receptor, String estado, String observacion, Usuario usuario,
            Deposito deposito) {
        this.idNotaRemisionCompra = idNotaRemisionCompra;
        this.numero = numero;
        this.timbrado = timbrado;
        this.fechaVencimientoTimbrado = fechaVencimientoTimbrado;
        this.fechaEmision = fechaEmision;
        this.vehiculo = vehiculo;
        this.conductor = conductor;
        this.emisor = emisor;
        this.receptor = receptor;
        this.estado = estado;
        this.observacion = observacion;
        this.usuario = usuario;
        this.deposito = deposito;
    }

    public Long getIdNotaRemisionCompra() {
        return idNotaRemisionCompra;
    }

    public void setIdNotaRemisionCompra(Long idNotaRemisionCompra) {
        this.idNotaRemisionCompra = idNotaRemisionCompra;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Long getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Long timbrado) {
        this.timbrado = timbrado;
    }

    public Date getFechaVencimientoTimbrado() {
        return fechaVencimientoTimbrado;
    }

    public void setFechaVencimientoTimbrado(Date fechaVencimientoTimbrado) {
        this.fechaVencimientoTimbrado = fechaVencimientoTimbrado;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public void setReceptor(String receptor) {
        this.receptor = receptor;
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

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }
}
