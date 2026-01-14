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
public class FormaPagoDetalle {

    private Long idFormaPagoDetalle;
    private FormaPagoCabecera formaPagoCabecera;
    private OrdenPago ordenPago;
    private Long transferencia;
    private Long cheque;
    private Long monto;
    private String estado;
    private String referencia;
    private Cuenta cuenta;
    private Date fecha;

    public FormaPagoDetalle() {
    }

    public FormaPagoDetalle(Long idFormaPagoDetalle) {
        this.idFormaPagoDetalle = idFormaPagoDetalle;
    }

    public FormaPagoDetalle(Long idFormaPagoDetalle, FormaPagoCabecera formaPagoCabecera,
            OrdenPago ordenPago, Long transferencia, Long cheque, Long monto, String estado,
            String referencia, Cuenta cuenta, Date fecha) {
        this.idFormaPagoDetalle = idFormaPagoDetalle;
        this.formaPagoCabecera = formaPagoCabecera;
        this.ordenPago = ordenPago;
        this.transferencia = transferencia;
        this.cheque = cheque;
        this.monto = monto;
        this.estado = estado;
        this.referencia = referencia;
        this.cuenta = cuenta;
        this.fecha = fecha;
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

    public Long getTransferencia() {
        return transferencia;
    }

    public void setTransferencia(Long transferencia) {
        this.transferencia = transferencia;
    }

    public Long getCheque() {
        return cheque;
    }

    public void setCheque(Long cheque) {
        this.cheque = cheque;
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
}
