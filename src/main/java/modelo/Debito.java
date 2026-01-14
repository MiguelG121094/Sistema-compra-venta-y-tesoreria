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
public class Debito {

    private Long idDebito;
    private Long numeroComprobante;
    private Date fecha;
    private String detalle;
    private Cuenta cuenta;
    private Long monto;

    public Debito() {
    }

    public Debito(Long idDebito) {
        this.idDebito = idDebito;
    }

    public Debito(Long idDebito, Long numeroComprobante, Date fecha, String detalle,
            Cuenta cuenta, Long monto) {
        this.idDebito = idDebito;
        this.numeroComprobante = numeroComprobante;
        this.fecha = fecha;
        this.detalle = detalle;
        this.cuenta = cuenta;
        this.monto = monto;
    }

    public Long getIdDebito() {
        return idDebito;
    }

    public void setIdDebito(Long idDebito) {
        this.idDebito = idDebito;
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

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
