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
public class CuentaCobrar {

    private Long idCuentaCobrar;
    private FacturaVenta facturaVenta;
    private Long monto;
    private Date fecha;
    private Long saldo;
    private Date fechaVencimiento;
    private String estado;
    private Integer cantidadCuota;

    public CuentaCobrar() {
    }

    public CuentaCobrar(Long idCuentaCobrar) {
        this.idCuentaCobrar = idCuentaCobrar;
    }

    public CuentaCobrar(Long idCuentaCobrar, FacturaVenta facturaVenta, Long monto, Date fecha,
            Long saldo, Date fechaVencimiento, String estado, Integer cantidadCuota) {
        this.idCuentaCobrar = idCuentaCobrar;
        this.facturaVenta = facturaVenta;
        this.monto = monto;
        this.fecha = fecha;
        this.saldo = saldo;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.cantidadCuota = cantidadCuota;
    }

    public Long getIdCuentaCobrar() {
        return idCuentaCobrar;
    }

    public void setIdCuentaCobrar(Long idCuentaCobrar) {
        this.idCuentaCobrar = idCuentaCobrar;
    }

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Long getSaldo() {
        return saldo;
    }

    public void setSaldo(Long saldo) {
        this.saldo = saldo;
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

    public Integer getCantidadCuota() {
        return cantidadCuota;
    }

    public void setCantidadCuota(Integer cantidadCuota) {
        this.cantidadCuota = cantidadCuota;
    }
}
