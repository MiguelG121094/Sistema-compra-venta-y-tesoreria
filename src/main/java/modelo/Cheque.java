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
public class Cheque {

    private Long idCheque;
    private Long numero;
    private Date fechaEmision;
    private String estado;
    private Chequera chequera;
    private String aLaOrden;
    private String observacion;
    private TipoCheque tipoCheque;
    private Date fechaPago;
    private Date fechaVencimiento;
    private Usuario usuario;

    public Cheque() {
    }

    public Cheque(Long idCheque) {
        this.idCheque = idCheque;
    }

    public Cheque(Long idCheque, Long numero, Date fechaEmision, String estado, Chequera chequera,
            String aLaOrden, String observacion, TipoCheque tipoCheque, Date fechaPago,
            Date fechaVencimiento, Usuario usuario) {
        this.idCheque = idCheque;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.chequera = chequera;
        this.aLaOrden = aLaOrden;
        this.observacion = observacion;
        this.tipoCheque = tipoCheque;
        this.fechaPago = fechaPago;
        this.fechaVencimiento = fechaVencimiento;
        this.usuario = usuario;
    }

    public Long getIdCheque() {
        return idCheque;
    }

    public void setIdCheque(Long idCheque) {
        this.idCheque = idCheque;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Chequera getChequera() {
        return chequera;
    }

    public void setChequera(Chequera chequera) {
        this.chequera = chequera;
    }

    public String getaLaOrden() {
        return aLaOrden;
    }

    public void setaLaOrden(String aLaOrden) {
        this.aLaOrden = aLaOrden;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public TipoCheque getTipoCheque() {
        return tipoCheque;
    }

    public void setTipoCheque(TipoCheque tipoCheque) {
        this.tipoCheque = tipoCheque;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
