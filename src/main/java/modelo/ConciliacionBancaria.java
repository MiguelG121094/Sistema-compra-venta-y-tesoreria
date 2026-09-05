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
public class ConciliacionBancaria implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long idConciliacionBancaria;
    private Cuenta cuenta;
    private Date fechaDesde;
    private Date fecha;
    private Date fechaHasta;
    private Long saldoInicial;
    private Long saldoFinal;
    private Long saldoBanco;
    private String estado;

    public ConciliacionBancaria() {
    }

    public ConciliacionBancaria(Long idConciliacionBancaria) {
        this.idConciliacionBancaria = idConciliacionBancaria;
    }

    public ConciliacionBancaria(Long idConciliacionBancaria, Cuenta cuenta, Date fechaDesde,
            Date fecha, Date fechaHasta, Long saldoInicial, Long saldoFinal, Long saldoBanco,
            String estado) {
        this.idConciliacionBancaria = idConciliacionBancaria;
        this.cuenta = cuenta;
        this.fechaDesde = fechaDesde;
        this.fecha = fecha;
        this.fechaHasta = fechaHasta;
        this.saldoInicial = saldoInicial;
        this.saldoFinal = saldoFinal;
        this.saldoBanco = saldoBanco;
        this.estado = estado;
    }

    public Long getIdConciliacionBancaria() {
        return idConciliacionBancaria;
    }

    public void setIdConciliacionBancaria(Long idConciliacionBancaria) {
        this.idConciliacionBancaria = idConciliacionBancaria;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Long getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(Long saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public Long getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(Long saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public Long getSaldoBanco() {
        return saldoBanco;
    }

    public void setSaldoBanco(Long saldoBanco) {
        this.saldoBanco = saldoBanco;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
