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
public class Credito implements MovimientoBancario {

    private static final long serialVersionUID = 1L;

    private Long idCredito;
    private Long numeroComprobante;
    private Date fecha;
    private String detalle;
    private Cuenta cuenta;
    private Cobro cobro;
    private Long monto;
    private Double tipoCambio;
    private String estado;

    public Credito() {
    }

    public Credito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Credito(Long idCredito, Long numeroComprobante, Date fecha, String detalle,
            Cuenta cuenta, Cobro cobro, Long monto) {
        this.idCredito = idCredito;
        this.numeroComprobante = numeroComprobante;
        this.fecha = fecha;
        this.detalle = detalle;
        this.cuenta = cuenta;
        this.cobro = cobro;
        this.monto = monto;
    }

    /** Alias de getIdCredito() que pide MovimientoBancario, para compartir la vista. */
    @Override
    public Long getId() {
        return idCredito;
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Long getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(Long numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public Double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(Double tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
