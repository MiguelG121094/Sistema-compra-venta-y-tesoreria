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
public class NotaDebitoVenta implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idNotaDebitoVenta;
    private Integer numero;
    private Date fechaEmision;
    private String motivo;
    private String observacion;
    private String estado;
    private Usuario usuario;
    private Timbrado timbrado;

    public NotaDebitoVenta() {
    }

    public NotaDebitoVenta(Long idNotaDebitoVenta) {
        this.idNotaDebitoVenta = idNotaDebitoVenta;
    }

    public NotaDebitoVenta(Long idNotaDebitoVenta, Integer numero, Date fechaEmision,
            String motivo, String observacion, String estado, Usuario usuario, Timbrado timbrado) {
        this.idNotaDebitoVenta = idNotaDebitoVenta;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.motivo = motivo;
        this.observacion = observacion;
        this.estado = estado;
        this.usuario = usuario;
        this.timbrado = timbrado;
    }

    public Long getIdNotaDebitoVenta() {
        return idNotaDebitoVenta;
    }

    public void setIdNotaDebitoVenta(Long idNotaDebitoVenta) {
        this.idNotaDebitoVenta = idNotaDebitoVenta;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Timbrado getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Timbrado timbrado) {
        this.timbrado = timbrado;
    }
}
