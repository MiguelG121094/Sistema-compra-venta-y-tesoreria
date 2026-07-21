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
public class FormaPagoDetalle implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFormaPagoDetalle;
    private FormaPagoCabecera formaPagoCabecera;
    private OrdenPago ordenPago;
    private Long monto;
    private String estado;
    private String referencia;
    private Cuenta cuenta;
    private Date fecha;
    private Cheque cheque;

    public FormaPagoDetalle() {
    }

    public FormaPagoDetalle(Long idFormaPagoDetalle) {
        this.idFormaPagoDetalle = idFormaPagoDetalle;
    }

    public FormaPagoDetalle(Long idFormaPagoDetalle, FormaPagoCabecera formaPagoCabecera,
            OrdenPago ordenPago, Long monto, String estado, String referencia, Cuenta cuenta,
            Date fecha, Cheque cheque) {
        this.idFormaPagoDetalle = idFormaPagoDetalle;
        this.formaPagoCabecera = formaPagoCabecera;
        this.ordenPago = ordenPago;
        this.monto = monto;
        this.estado = estado;
        this.referencia = referencia;
        this.cuenta = cuenta;
        this.fecha = fecha;
        this.cheque = cheque;
    }

    public Long getIdFormaPagoDetalle() {
        return idFormaPagoDetalle;
    }

    public void setIdFormaPagoDetalle(Long idFormaPagoDetalle) {
        this.idFormaPagoDetalle = idFormaPagoDetalle;
    }

    public FormaPagoCabecera getFormaPagoCabecera() {
        return formaPagoCabecera;
    }

    public void setFormaPagoCabecera(FormaPagoCabecera formaPagoCabecera) {
        this.formaPagoCabecera = formaPagoCabecera;
    }

    public OrdenPago getOrdenPago() {
        return ordenPago;
    }

    public void setOrdenPago(OrdenPago ordenPago) {
        this.ordenPago = ordenPago;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Cheque getCheque() {
        return cheque;
    }

    public void setCheque(Cheque cheque) {
        this.cheque = cheque;
    }
}
