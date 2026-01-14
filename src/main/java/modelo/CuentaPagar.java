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
public class CuentaPagar {

    private Long idCuentaPagar;
    private FacturaCompra facturaCompra;
    private Long monto;
    private String estado;
    private Date fechaVencimiento;
    private Long saldo;

    public CuentaPagar() {
    }

    public CuentaPagar(Long idCuentaPagar) {
        this.idCuentaPagar = idCuentaPagar;
    }

    public CuentaPagar(Long idCuentaPagar, FacturaCompra facturaCompra, Long monto, String estado,
            Date fechaVencimiento, Long saldo) {
        this.idCuentaPagar = idCuentaPagar;
        this.facturaCompra = facturaCompra;
        this.monto = monto;
        this.estado = estado;
        this.fechaVencimiento = fechaVencimiento;
        this.saldo = saldo;
    }

    public Long getIdCuentaPagar() {
        return idCuentaPagar;
    }

    public void setIdCuentaPagar(Long idCuentaPagar) {
        this.idCuentaPagar = idCuentaPagar;
    }

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
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

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Long getSaldo() {
        return saldo;
    }

    public void setSaldo(Long saldo) {
        this.saldo = saldo;
    }
}
