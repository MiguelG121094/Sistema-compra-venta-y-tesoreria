/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.sql.Timestamp;

/**
 *
 * @author Miguel
 */
public class AperturaCierreCaja {

    private Long idAperturaCierreCaja;
    private Timestamp fechaApertura;
    private Long montoInicial;
    private Timestamp fechaCierre;
    private Long efectivo;
    private Long cheque;
    private Long tarjeta;
    private Long montoCierre;
    private String estado;
    private Long idCaja;
    private Sucursal sucursal;
    private Usuario usuario;

    public AperturaCierreCaja() {
    }

    public AperturaCierreCaja(Long idAperturaCierreCaja) {
        this.idAperturaCierreCaja = idAperturaCierreCaja;
    }

    public AperturaCierreCaja(Long idAperturaCierreCaja, Timestamp fechaApertura, Long montoInicial,
            Timestamp fechaCierre, Long efectivo, Long cheque, Long tarjeta, Long montoCierre,
            String estado, Long idCaja, Sucursal sucursal, Usuario usuario) {
        this.idAperturaCierreCaja = idAperturaCierreCaja;
        this.fechaApertura = fechaApertura;
        this.montoInicial = montoInicial;
        this.fechaCierre = fechaCierre;
        this.efectivo = efectivo;
        this.cheque = cheque;
        this.tarjeta = tarjeta;
        this.montoCierre = montoCierre;
        this.estado = estado;
        this.idCaja = idCaja;
        this.sucursal = sucursal;
        this.usuario = usuario;
    }

    public Long getIdAperturaCierreCaja() {
        return idAperturaCierreCaja;
    }

    public void setIdAperturaCierreCaja(Long idAperturaCierreCaja) {
        this.idAperturaCierreCaja = idAperturaCierreCaja;
    }

    public Timestamp getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Timestamp fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public Long getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(Long montoInicial) {
        this.montoInicial = montoInicial;
    }

    public Timestamp getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Timestamp fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Long getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(Long efectivo) {
        this.efectivo = efectivo;
    }

    public Long getCheque() {
        return cheque;
    }

    public void setCheque(Long cheque) {
        this.cheque = cheque;
    }

    public Long getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(Long tarjeta) {
        this.tarjeta = tarjeta;
    }

    public Long getMontoCierre() {
        return montoCierre;
    }

    public void setMontoCierre(Long montoCierre) {
        this.montoCierre = montoCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Long idCaja) {
        this.idCaja = idCaja;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
