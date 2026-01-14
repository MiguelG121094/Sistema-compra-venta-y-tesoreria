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
public class Timbrado {

    private Long idTimbrado;
    private Integer numero;
    private Date fechaAutorizacion;
    private Date fechaVencimiento;
    private String estado;
    private Long idTipoComprobante;

    public Timbrado() {
    }

    public Timbrado(Long idTimbrado) {
        this.idTimbrado = idTimbrado;
    }

    public Timbrado(Long idTimbrado, Integer numero, Date fechaAutorizacion, Date fechaVencimiento,
            String estado, Long idTipoComprobante) {
        this.idTimbrado = idTimbrado;
        this.numero = numero;
        this.fechaAutorizacion = fechaAutorizacion;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.idTipoComprobante = idTipoComprobante;
    }

    public Long getIdTimbrado() {
        return idTimbrado;
    }

    public void setIdTimbrado(Long idTimbrado) {
        this.idTimbrado = idTimbrado;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Date getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(Date fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getIdTipoComprobante() {
        return idTipoComprobante;
    }

    public void setIdTipoComprobante(Long idTipoComprobante) {
        this.idTipoComprobante = idTipoComprobante;
    }
}
