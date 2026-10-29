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
public class CuentaPagar implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCuentaPagar;
    private FacturaCompra facturaCompra;
    private Long monto;
    private String estado;
    private Date fechaVencimiento;
    private Long saldo;
    private Long plazo;
    /* Comprobante de la fila: FACTURA, NOTA_CRED o NOTA_DEBI. Las notas llevan en facturaCompra
       la factura que corrigen. Ver NOTA_CREDITO_DEBITO_PLAN.md §4.2. */
    private String origen;
    private NotaCreditoCompra notaCredito;
    private NotaDebitoCompra notaDebito;

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

    public Long getPlazo() {
        return plazo;
    }

    public void setPlazo(Long plazo) {
        this.plazo = plazo;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public NotaCreditoCompra getNotaCredito() {
        return notaCredito;
    }

    public void setNotaCredito(NotaCreditoCompra notaCredito) {
        this.notaCredito = notaCredito;
    }

    public NotaDebitoCompra getNotaDebito() {
        return notaDebito;
    }

    public void setNotaDebito(NotaDebitoCompra notaDebito) {
        this.notaDebito = notaDebito;
    }

    /** Una fila sin origen es de factura: es lo que habia antes de que las notas tuvieran fila. */
    public boolean isOrigenFactura() {
        return origen == null || CuentaPagarDAO.ORIGEN_FACTURA.equals(origen);
    }

    /**
     * Comprobante de la fila para los listados de provision, orden de pago y rendicion: el numero
     * de la factura, o el de la nota con su prefijo.
     */
    public String getComprobante() {
        if (CuentaPagarDAO.ORIGEN_NOTA_CREDITO.equals(origen) && notaCredito != null) {
            return "NC " + notaCredito.getNumero();
        }
        if (CuentaPagarDAO.ORIGEN_NOTA_DEBITO.equals(origen) && notaDebito != null) {
            return "ND " + notaDebito.getNumero();
        }
        return facturaCompra != null ? facturaCompra.getNumero() : null;
    }
}
