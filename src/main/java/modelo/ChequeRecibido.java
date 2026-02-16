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
public class ChequeRecibido implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idChequeRecibido;
    private Long numero;
    private String serie;
    private Date fechaEmision;
    private Date fechaVencimiento;
    private Date fechaPago;
    private String estado;
    private String observacion;
    private TipoCheque tipoCheque;
    private Moneda moneda;
    private Titular titular;

    public ChequeRecibido() {
    }

    public ChequeRecibido(Long idChequeRecibido) {
        this.idChequeRecibido = idChequeRecibido;
    }

    public ChequeRecibido(Long idChequeRecibido, Long numero, String serie, Date fechaEmision,
            Date fechaVencimiento, Date fechaPago, String estado, String observacion,
            TipoCheque tipoCheque, Moneda moneda, Titular titular) {
        this.idChequeRecibido = idChequeRecibido;
        this.numero = numero;
        this.serie = serie;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaPago = fechaPago;
        this.estado = estado;
        this.observacion = observacion;
        this.tipoCheque = tipoCheque;
        this.moneda = moneda;
        this.titular = titular;
    }

    public Long getIdChequeRecibido() {
        return idChequeRecibido;
    }

    public void setIdChequeRecibido(Long idChequeRecibido) {
        this.idChequeRecibido = idChequeRecibido;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
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

    public TipoCheque getTipoCheque() {
        return tipoCheque;
    }

    public void setTipoCheque(TipoCheque tipoCheque) {
        this.tipoCheque = tipoCheque;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Titular getTitular() {
        return titular;
    }

    public void setTitular(Titular titular) {
        this.titular = titular;
    }
}
