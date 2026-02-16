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
public class OrdenPago implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idOrdenPago;
    private Integer numero;
    private Date fechaEmision;
    private Long monto;
    private String estado;
    private Long idProvisionCtaPagar;
    private Integer numeroRecibo;
    private Long idMoneda;
    private Double tipoCambio;
    private Sucursal sucursal;
    private Long idCheque;
    private String tipoPago;
    private Proveedor proveedor;
    private Long idCuenta;

    public OrdenPago() {
    }

    public OrdenPago(Long idOrdenPago) {
        this.idOrdenPago = idOrdenPago;
    }

    public OrdenPago(Long idOrdenPago, Integer numero, Date fechaEmision, Long monto, String estado,
            Long idProvisionCtaPagar, Integer numeroRecibo, Long idMoneda, Double tipoCambio,
            Sucursal sucursal, Long idCheque, String tipoPago, Proveedor proveedor, Long idCuenta) {
        this.idOrdenPago = idOrdenPago;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.monto = monto;
        this.estado = estado;
        this.idProvisionCtaPagar = idProvisionCtaPagar;
        this.numeroRecibo = numeroRecibo;
        this.idMoneda = idMoneda;
        this.tipoCambio = tipoCambio;
        this.sucursal = sucursal;
        this.idCheque = idCheque;
        this.tipoPago = tipoPago;
        this.proveedor = proveedor;
        this.idCuenta = idCuenta;
    }

    public Long getIdOrdenPago() {
        return idOrdenPago;
    }

    public void setIdOrdenPago(Long idOrdenPago) {
        this.idOrdenPago = idOrdenPago;
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

    public Long getIdProvisionCtaPagar() {
        return idProvisionCtaPagar;
    }

    public void setIdProvisionCtaPagar(Long idProvisionCtaPagar) {
        this.idProvisionCtaPagar = idProvisionCtaPagar;
    }

    public Integer getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(Integer numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public Long getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(Long idMoneda) {
        this.idMoneda = idMoneda;
    }

    public Double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(Double tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Long getIdCheque() {
        return idCheque;
    }

    public void setIdCheque(Long idCheque) {
        this.idCheque = idCheque;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Long getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Long idCuenta) {
        this.idCuenta = idCuenta;
    }
}
